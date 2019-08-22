package com.sglasman.tvqueue

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class TVQApplication: Application() {

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        appContext = this
        super.onCreate()
        CoroutineScope(Dispatchers.Main).launch {
            sendAction(AppAction.Login)
            appModel.openSubscription().consumeEach {
                it.apiToken?.let { Log.d("TOKRON", it) }
            }}
    }
}