package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Episode(
    val watched: Boolean = false,
    val seriesTitle: String,
    val title: String,
    val numberInSeason: Int,
    val airDate: Date,
    val dateToWatch: Date
)