package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SeriesResponse(val data: List<EpisodeResponse>)