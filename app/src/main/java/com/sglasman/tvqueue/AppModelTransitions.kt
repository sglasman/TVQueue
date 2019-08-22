package com.sglasman.tvqueue

import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.TVDBCredentials
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
suspend fun sendAction(action: AppAction) {
    appModel.send(getTransition(appModel.value, action))
}

@ExperimentalCoroutinesApi
private suspend fun getTransition(model: AppModel, action: AppAction): AppModel = when (action) {
    is AppAction.Login -> when (val response = apiWrapper.login(getApiCredentials())) {
        is TVQResponse.Success -> model.copy(apiToken = response.value)
        is TVQResponse.Error -> model
    }
}

private fun getApiCredentials(): TVDBCredentials = moshi.decode<TVDBCredentials>(
    appContext.resources.openRawResource(R.raw.creds).bufferedReader().use {
        it.readText()
    })!!