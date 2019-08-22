package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.models.LoginResponse
import com.sglasman.tvqueue.models.TVDBCredentials
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {

    @POST("login")
    suspend fun login(@Body creds: TVDBCredentials): Response<LoginResponse>

}