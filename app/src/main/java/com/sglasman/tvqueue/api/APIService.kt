package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.models.LoginResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {

    @POST("login")
    suspend fun login(@Query("apikey") apiKey: String,
                      @Query("username") username: String,
                      @Query("userkey") userKey: String): Response<LoginResponse>

}