package com.sglasman.tvqueue

import android.app.Application
import com.sglasman.tvqueue.api.APIService
import com.sglasman.tvqueue.api.APIWrapper
import com.sglasman.tvqueue.api.TVQRetrofit
import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.storage.Storage
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.util.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
val appModel: ConflatedBroadcastChannel<AppModel> = ConflatedBroadcastChannel(AppModel())

@ExperimentalCoroutinesApi
lateinit var apiWrapper: APIWrapper

lateinit var appContext: Application

lateinit var storage: Storage

var mainContext: CoroutineContext = Dispatchers.Main
var ioContext: CoroutineContext = Dispatchers.IO

val moshi: Moshi = Moshi.Builder()
    .add(Date::class.java, Rfc3339DateJsonAdapter())
    .build()

var getCurrentDate: () -> Date = { Date() }

val refreshChannel = Channel<Unit>(Channel.UNLIMITED)
val imeChannel = Channel<Boolean>(Channel.UNLIMITED)