package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.appModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
private val addApiTokenInterceptor: Interceptor = Interceptor { chain ->
    chain.proceed(
    appModel.value.apiToken?.let {
        chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $it")
            .build()}
        ?: chain.request())
    }

@ExperimentalCoroutinesApi
private val TVQClient: OkHttpClient = OkHttpClient
    .Builder()
    .addInterceptor(addApiTokenInterceptor)
    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY})
    .connectTimeout(60000, TimeUnit.MILLISECONDS)
    .readTimeout(60000, TimeUnit.MILLISECONDS)
    .build()

@ExperimentalCoroutinesApi
val TVQRetrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl("https://api.thetvdb.com/")
    .client(TVQClient)
    .build()