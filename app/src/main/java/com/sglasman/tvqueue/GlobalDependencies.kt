package com.sglasman.tvqueue

import com.sglasman.tvqueue.api.APIService
import com.sglasman.tvqueue.api.APIWrapper
import com.sglasman.tvqueue.api.TVQRetrofit
import com.sglasman.tvqueue.models.AppModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@ExperimentalCoroutinesApi
val appModel: ConflatedBroadcastChannel<AppModel> = ConflatedBroadcastChannel(AppModel())
@ExperimentalCoroutinesApi
val apiWrapper = APIWrapper(TVQRetrofit.create(APIService::class.java))