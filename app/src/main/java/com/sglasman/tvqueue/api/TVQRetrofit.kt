package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.appModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@ExperimentalCoroutinesApi
private val addApiTokenInterceptor: Interceptor = Interceptor { chain ->
    chain.proceed(
    appModel.openSubscription().poll()?.apiToken?.let {
        chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $it")
            .build()}
        ?: chain.request())
    }

@ExperimentalCoroutinesApi
private val TVQClient: OkHttpClient = OkHttpClient
    .Builder()
    .addInterceptor(addApiTokenInterceptor)
    .build()

@ExperimentalCoroutinesApi
val TVQRetrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl("https://api.thetvdb.com/")
    .client(TVQClient)
    .build()