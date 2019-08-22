package com.sglasman.tvqueue

import android.app.Application
import com.sglasman.tvqueue.api.APIService
import com.sglasman.tvqueue.api.APIWrapper
import com.sglasman.tvqueue.api.TVQRetrofit
import com.sglasman.tvqueue.models.AppModel
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@ExperimentalCoroutinesApi
val appModel: ConflatedBroadcastChannel<AppModel> = ConflatedBroadcastChannel(AppModel())

@ExperimentalCoroutinesApi
val apiWrapper = APIWrapper(TVQRetrofit.create(APIService::class.java))

lateinit var appContext: Application

val moshi = Moshi.Builder().build()