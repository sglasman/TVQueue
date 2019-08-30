package com.sglasman.tvqueue

import android.app.Application
import android.util.Log
import com.sglasman.tvqueue.api.APIService
import com.sglasman.tvqueue.api.APIWrapper
import com.sglasman.tvqueue.api.TVQRetrofit
import com.sglasman.tvqueue.models.storage.SharedPrefsStorage
import com.sglasman.tvqueue.models.storage.synchronizeEpisodeIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

@ExperimentalCoroutinesApi
class TVQApplication : Application() {

    override fun onCreate() {
        appContext = this
        storage = SharedPrefsStorage(this)
        apiWrapper = APIWrapper(TVQRetrofit.create(APIService::class.java))
        super.onCreate()
        launch { synchronizeEpisodeIds() }
        launch {
            sendAction(AppAction.Login)
            sendAction(AppAction.GetQueue)
            appModel.openSubscription().consumeEach {
                it.apiToken?.let { Log.d("TOKRON", it) }
            }

        }
        launch { startEngine() }
    }
}