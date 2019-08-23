package com.sglasman.tvqueue

import android.app.Application
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

class TVQApplication : Application() {

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        appContext = this
        super.onCreate()
        launch { startEngine() }
        launch {
            sendAction(AppAction.Login)
            appModel.openSubscription().consumeEach {
                it.apiToken?.let { Log.d("TOKRON", it) }
            }
        }
    }
}