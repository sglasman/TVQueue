package com.sglasman.tvqueue.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TVDBCredentials(val username: String,
                           @Json(name = "userkey")  val userKey: String,
                           @Json(name = "apikey") val apiKey: String)