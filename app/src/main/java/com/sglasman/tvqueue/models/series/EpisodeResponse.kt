package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpisodeResponse(val airedSeason: Int?,
                           val episodeName: String?,
                           val firstAired: String?,
                           val airedEpisodeNumber: Int?)