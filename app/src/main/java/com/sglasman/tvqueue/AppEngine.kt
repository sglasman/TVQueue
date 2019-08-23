package com.sglasman.tvqueue

import android.util.Log
import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.DialogMode
import com.sglasman.tvqueue.models.Screen
import com.sglasman.tvqueue.models.TVDBCredentials
import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.addseries.Stage
import com.sglasman.tvqueue.models.search.SearchStatus
import kotlinx.coroutines.*
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

    is AppAction.Login -> when (val response = apiWrapper.login(getApiCredentials())) {
        is TVQResponse.Success -> model.copy(apiToken = response.value)
        is TVQResponse.Error -> model
    }

    is AppAction.BackPressed -> backstack.popOrNull()?.let {
        Log.d("Engine", "Popping model from backstack")
        it
    } ?: model.copy(currentScreen = Screen.Finishing)

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
            dialogMode = DialogMode.AddSeries,
            addSeriesModel = model.addSeriesModel.copy(stage = Stage.Loading)
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
                    selectedSeason = defaultSeason
                )
            }
            is TVQResponse.Error -> model.addSeriesModel.copy(
                stage = Stage.Error
            )
        }
    )

    AppAction.AddSeriesAction.SeasonUpClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeason = model.addSeriesModel.seasonNumbers
                .filter { it > model.addSeriesModel.selectedSeason }
                .min() ?: model.addSeriesModel.selectedSeason
        )
    )

    AppAction.AddSeriesAction.SeasonDownClicked -> model.copy(
        addSeriesModel = model.addSeriesModel.copy(
            selectedSeason = model.addSeriesModel.seasonNumbers
                .filter { it < model.addSeriesModel.selectedSeason }
                .max() ?: model.addSeriesModel.selectedSeason
        )
    )
}

private fun getApiCredentials()
        : TVDBCredentials =
    moshi.decode<TVDBCredentials>(
        appContext.resources.openRawResource(R.raw.creds).bufferedReader().use {
            it.readText()
        })!!