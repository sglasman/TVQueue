package com.sglasman.tvqueue

import android.util.Log
import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.DialogMode
import com.sglasman.tvqueue.models.TVDBCredentials
import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.addseries.Stage
import com.sglasman.tvqueue.models.search.SearchStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

@ExperimentalCoroutinesApi
suspend fun sendAction(action: AppAction, options: AppActionOptions? = null) {
    if (options is AppActionOptions.CancelSearch &&
        currentAction is AppAction.SearchAction.SearchNow
    ) {
        Log.d("Engine", "Cancelling search")
        nextModel.complete(appModel.value.copy())
    }
    Log.d("Engine", "Sending action $action")
    actionQueue.send(action)
}

private var actionQueue: Channel<AppAction> = Channel(Channel.UNLIMITED)
private var currentAction: AppAction? = null
private var nextModel: CompletableDeferred<AppModel> = CompletableDeferred()

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
            is TVQResponse.Success -> model.addSeriesModel.copy(
                series = response.value,
                stage = Stage.SelectSeason
            )
            is TVQResponse.Error -> model.addSeriesModel.copy(
                stage = Stage.Error
            )
        }
    )
}

private fun getApiCredentials()
        : TVDBCredentials =
    moshi.decode<TVDBCredentials>(
        appContext.resources.openRawResource(R.raw.creds).bufferedReader().use {
            it.readText()
        })!!