package com.sglasman.tvqueue

import android.util.Log
import com.sglasman.tvqueue.models.*
import com.sglasman.tvqueue.models.addseries.Stage
import com.sglasman.tvqueue.models.confirmation.ConfirmationModel
import com.sglasman.tvqueue.models.queue.addSeparators
import com.sglasman.tvqueue.models.search.SearchStatus
import com.sglasman.tvqueue.models.storage.dataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.*

@ExperimentalCoroutinesApi
suspend fun sendAction(
    action: AppAction,
    options: AppActionOptions? = null,
    addPreviousToBackstack: Boolean = false
) {
    if (addPreviousToBackstack) {
        Log.d("Engine", "Pushing previous model to backstack")
        backstack.push(appModel.value)
    }
    if (options is AppActionOptions.CancelSearch &&
        currentAction is AppAction.SearchAction.SearchNow
    ) {
        Log.d("Engine", "Cancelling search")
        nextModel.complete(appModel.value.copy())
    }
    if (options is AppActionOptions.CancelAllPending) {
        actionQueue = Channel(Channel.UNLIMITED)
        nextModel.complete(appModel.value.copy())
    }
    Log.d("Engine", "Sending action $action")
    actionQueue.send(action)
}

private var actionQueue: Channel<AppAction> = Channel(Channel.UNLIMITED)
private var currentAction: AppAction? = null
private var nextModel: CompletableDeferred<AppModel> = CompletableDeferred()

private val backstack: Stack<AppModel> = Stack()

private fun <T> Stack<T>.popOrNull(): T? = try {
    pop()
} catch (e: EmptyStackException) {
    null
}

private fun <T> Stack<T>.peekOrNull(): T? = try {
    peek()
} catch (e: EmptyStackException) {
    null
}

private fun <T> Stack<T>.popUntil(pred: (T) -> Boolean) {
    while (peekOrNull()?.let { pred(it) } == false) {
        pop()
    }
}

@ExperimentalCoroutinesApi
suspend fun startEngine() {
    appModel.openSubscription().consumeEach {
        Log.d("Engine", "Started loop")
        currentAction = actionQueue.receive()
        nextModel = CompletableDeferred()
        Log.d("Engine", "Received action: $currentAction")
        launch { nextModel.complete(doTransition(it, currentAction!!)) }
        appModel.send(nextModel.await())
        currentAction = null
    }
}

@ExperimentalCoroutinesApi
private suspend fun doTransition(model: AppModel, action: AppAction): AppModel = when (action) {

    is AppAction.Login -> model.copy(
        currentScreen = Screen.QueueScreen,
        apiToken = when (val response = apiWrapper.login(getApiCredentials())) {
            is TVQResponse.Success -> response.value
            is TVQResponse.Error -> model.apiToken
        }
    )

    is AppAction.BackPressed -> backstack.popOrNull()?.let {
        Log.d("Engine", "Popping model from backstack")
        if (it.dialogScreen is DialogScreen.NotShown) refreshChannel.send(Unit)
        it
    } ?: run {
        launch { sendAction(AppAction.GetReadyToRestart) }
        model.copy(currentScreen = Screen.Finishing)
    }

    is AppAction.GetReadyToRestart -> model.copy(currentScreen = Screen.QueueScreen)

    is AppAction.QueueTextClicked -> {
        refreshChannel.send(Unit)
        model.copy(currentScreen = Screen.QueueScreen)
    }

    is AppAction.SeriesTextClicked -> {
        refreshChannel.send(Unit)
        sendAction(AppAction.SearchAction.SearchTextChanged(action.searchText))
        model.copy(currentScreen = Screen.SearchScreen)
    }

    is AppAction.GetQueue -> {
        Log.d("Queue", "Getting queue")
        model.copy(
            queueModel = model.queueModel.copy(
                queue = dataStore.watchingSeries
                    .flatMap { it.seasons }
                    .flatMap { it.episodes }
                    .filter { !it.watched }
                    .map { it.toQueueItem() }
                    .sorted()
                    .addSeparators()
            ))
    }

    is AppAction.QueueAction.QueueItemClicked -> {
        model.copy(
            dialogScreen = DialogScreen.Confirmation,
            confirmationModel = ConfirmationModel(
                actionToConfirm = AppAction.QueueAction.MarkWatchedConfirmed(action.item),
                confirmationText = "Mark episode watched?"
            )
        )
    }

    is AppAction.QueueAction.MarkWatchedConfirmed -> {
        dataStore = dataStore.apply { episodesById[action.item.internalID]?.watched = true }
        model.backOutOfDialog()
    }

    is AppAction.SearchAction.Back -> model

    is AppAction.SearchAction.SearchTextChanged -> {
        model.copy(
            searchModel = model.searchModel.copy(
                status =
                if (action.text.isBlank()) SearchStatus.Results(listOf())
                else {
                    launch { sendAction(AppAction.SearchAction.SearchNow(action.text)) }
                    SearchStatus.Loading
                }
            )
        )
    }

    is AppAction.SearchAction.SearchNow -> model.copy(
        searchModel = model.searchModel.copy(
            status = when (val response = apiWrapper.search(action.text)) {
                is TVQResponse.Success -> SearchStatus.Results(response.value)
                is TVQResponse.Error -> when (response.errorCode) {
                    404 -> SearchStatus.Results(listOf())
                    else -> SearchStatus.Error
                }
            }
        )
    )

    is AppAction.SearchAction.ResultClicked -> {
        launch { sendAction(AppAction.SearchAction.GetSeriesFromResult(action.item)) }
        model.copy(
            dialogScreen = DialogScreen.AddSeries,
            addSeriesModel = model.addSeriesModel.copy(
                stage = Stage.Loading,
                selectedSeparation = 7
            )
        )
    }

    is AppAction.SearchAction.GetSeriesFromResult -> model.copy(
        addSeriesModel =
        when (val response = apiWrapper.getSeries(action.item.id, action.item.name)) {
            is TVQResponse.Success -> {

                val series = response.value
                val seasonNumbers = series.getSeasonNumbers()
                val defaultSeason = if (1 in seasonNumbers) 1 else seasonNumbers.min()!!

                model.addSeriesModel.copy(
                    series = series,
                    stage = Stage.SelectSeason,
                    selectedSeasonNumber = defaultSeason
                )
            }
            is TVQResponse.Error -> model.addSeriesModel.copy(
                stage = Stage.Error
            )
        }
    )

    is AppAction.AddSeriesAction.SeasonUpClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeasonNumber = model.addSeriesModel.seasonNumbers
                .filter { it > model.addSeriesModel.selectedSeasonNumber }
                .min() ?: model.addSeriesModel.selectedSeasonNumber
        )
    )

    is AppAction.AddSeriesAction.SeasonDownClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeasonNumber = model.addSeriesModel.seasonNumbers
                .filter { it < model.addSeriesModel.selectedSeasonNumber }
                .max() ?: model.addSeriesModel.selectedSeasonNumber
        )
    )

    is AppAction.AddSeriesAction.JustAddFutureSeasonsClicked -> {
        model.copy(
            dialogScreen = DialogScreen.Confirmation,
            confirmationModel = model.confirmationModel.copy(
                actionToConfirm = AppAction.AddSeriesAction.JustAddFutureSeasonsConfirmed,
                confirmationText = appContext.getString(R.string.get_future_seasons)
            )
        )
    }

    is AppAction.AddSeriesAction.JustAddFutureSeasonsConfirmed -> {
        model.addSeriesModel.series?.let { series ->
            dataStore = dataStore.copy(
                watchingSeries = dataStore.watchingSeries + (series.copy(
                    seasons = listOf(),
                    alertFutureSeasons = true,
                    latestSeason = model.addSeriesModel.seasonNumbers.max()!!
                ))
            )
        }
        model.backOutOfDialog()
    }

    is AppAction.AddSeriesAction.SeasonNextClicked -> {
        model.copy(
            addSeriesModel = model.addSeriesModel.copy(
                stage = Stage.SelectStartingEpisode,
                selectedStartingEpisodeNumber =
                if (1 in model.addSeriesModel.episodeNumbersInSelectedSeason) 1
                else model.addSeriesModel.episodeNumbersInSelectedSeason.min()!!
            )
        )
    }

    is AppAction.AddSeriesAction.EpisodeUpClicked -> {
        Log.d("Engine", "Working on episode up clicked")
        model.copy(
            addSeriesModel = model.addSeriesModel.copy(
                selectedStartingEpisodeNumber = model.addSeriesModel.episodeNumbersInSelectedSeason
                    .filter { it > model.addSeriesModel.selectedStartingEpisodeNumber }
                    .min() ?: model.addSeriesModel.selectedStartingEpisodeNumber
            )
        )
    }

    is AppAction.AddSeriesAction.EpisodeDownClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedStartingEpisodeNumber =
            model.addSeriesModel.episodeNumbersInSelectedSeason
                .filter { it < model.addSeriesModel.selectedStartingEpisodeNumber }
                .max() ?: model.addSeriesModel.selectedStartingEpisodeNumber
        )
    )

    is AppAction.AddSeriesAction.FinishClickedAfterEpisodeSelect ->
        model.copy(
            dialogScreen = DialogScreen.Confirmation,
            confirmationModel = model.confirmationModel.copy(
                actionToConfirm = AppAction.AddSeriesAction.FinishConfirmedAfterEpisodeSelect,
                confirmationText = model.addSeriesModel.run {
                    "Adding season $selectedSeasonNumber${series?.let { "of ${it.name}" }}${
                    if (selectedStartingEpisodeNumber != episodeNumbersInSelectedSeason.min())
                        ", starting from episode $selectedStartingEpisodeNumber,"
                    else ""} ${if (!maxSeasonNotSelected) " and future seasons "
                    else ""}to your queue."
                }
            ))

    is AppAction.AddSeriesAction.FinishConfirmedAfterEpisodeSelect -> {
        model.addSeriesModel.run {
            selectedSeason?.let { selectedSeason ->
                val seasonToAdd = selectedSeason.copy(
                    episodes = selectedSeason.episodes.filter {
                        it.numberInSeason >= selectedStartingEpisodeNumber
                    }
                )
                series?.let {
                    val seriesToAdd = it.copy(seasons = listOf(seasonToAdd))
                    dataStore = dataStore.mergeOrAddSeries(seriesToAdd)
                }
            }
        }
        model.backOutOfDialog()
    }

    is AppAction.AddSeriesAction.NextClickedAfterEpisodeSelect -> {
        model.copy(addSeriesModel = model.addSeriesModel.copy(stage = Stage.Schedule))
    }

    is AppAction.AddSeriesAction.SeparationUpClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeparation = model.addSeriesModel.selectedSeparation + 1
        )
    )

    is AppAction.AddSeriesAction.SeparationDownClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeparation = model.addSeriesModel.selectedSeparation - 1
        )
    )

    is AppAction.AddSeriesAction.ScheduleClicked -> model.copy(
        dialogScreen = DialogScreen.Confirmation,
        confirmationModel = ConfirmationModel(
            actionToConfirm = model.addSeriesModel.run {
                AppAction.AddSeriesAction.ScheduleSeason(
                    startDate = if (futureDump) selectedSeason?.episodes?.map { it.airDate }
                        ?.min() ?: getCurrentDate().roundToDay()
                    else getCurrentDate().roundToDay()
                )
            },
            confirmationText = model.addSeriesModel.run {
                "Scheduling season $selectedSeasonNumber ${
                series?.let { "of ${it.name}" }
                    ?: ""} starting  ${if (futureDump) "on release date" else "today"}, with episodes separated by $selectedSeparation days."
            }
        )
    )

    is AppAction.AddSeriesAction.ScheduleSeason -> {
        model.addSeriesModel.run {
            selectedSeason?.let { season ->

                val seasonToAdd = season.copy(
                    episodes = season.episodes
                        .filter { it.numberInSeason >= selectedStartingEpisodeNumber }
                        .sortedBy { it.airDate }
                        .map { episode ->
                            episode.copy(
                                dateToWatch = action.startDate.addDays(selectedSeparation * (episode.numberInSeason - 1))
                            )
                        })

                dataStore = dataStore.mergeOrAddSeries(series!!.copy(seasons = listOf(seasonToAdd)))
            }
        }
        model.backOutOfDialog()
    }

    AppAction.AddSeriesAction.UseOriginalClicked -> model.copy(
        dialogScreen = DialogScreen.Confirmation,
        confirmationModel = ConfirmationModel(
            actionToConfirm = model.addSeriesModel.run {
                if (pastDump) AppAction.AddSeriesAction.ScheduleSeason(
                    selectedSeason?.episodes?.firstOrNull()?.airDate
                        ?: getCurrentDate().roundToDay()
                )
                else AppAction.AddSeriesAction.FinishConfirmedAfterEpisodeSelect
            },
            confirmationText = model.addSeriesModel.run {
                "Scheduling season $selectedSeasonNumber ${
                series?.let { "of ${it.name}" }
                    ?: ""} using original airdates."
            }
        )
    )
}

@ExperimentalCoroutinesApi
private suspend fun AppModel.backOutOfDialog(): AppModel {
    backstack.popUntil { it.dialogScreen is DialogScreen.NotShown }
    sendAction(AppAction.BackPressed)
    return this
}

private fun getApiCredentials()
        : TVDBCredentials =
    appContext.resources.openRawResource(R.raw.credss).bufferedReader().use {
        it.readText()
    }.let {
        moshi.decode<TVDBCredentials>(it)!!
    }