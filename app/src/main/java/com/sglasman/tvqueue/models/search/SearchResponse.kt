package com.sglasman.tvqueue.models.search

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponse(val data: List<SearchResultResponse>)