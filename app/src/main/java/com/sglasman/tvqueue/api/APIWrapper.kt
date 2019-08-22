package com.sglasman.tvqueue.api

import com.sglasman.tvqueue.TVQResponse
import com.sglasman.tvqueue.getTVQResponse
import com.sglasman.tvqueue.models.TVDBCredentials

class APIWrapper(private val service: APIService) {

    suspend fun login(creds: TVDBCredentials): TVQResponse<String> =
        service.login(creds).getTVQResponse().map { it.token }
}