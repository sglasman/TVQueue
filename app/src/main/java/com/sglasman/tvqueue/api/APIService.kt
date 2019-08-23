package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.models.LoginResponse
import com.sglasman.tvqueue.models.TVDBCredentials
import com.sglasman.tvqueue.models.search.SearchResponse
import com.sglasman.tvqueue.models.series.EpisodeResponse
import com.sglasman.tvqueue.models.series.SeriesResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Response
import retrofit2.http.*

interface APIService {

    @POST("login")
    suspend fun login(@Body creds: TVDBCredentials): Response<LoginResponse>

    @GET("search/series")
    suspend fun search(@Query("name") name: String): Response<SearchResponse>

    @GET("series/{id}")
    suspend fun getSeries(@Path("id") id: Int): Response<SeriesResponse>
}