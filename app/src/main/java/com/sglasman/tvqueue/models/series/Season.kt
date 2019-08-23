package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Season(
    val number: Int,
    val episodes: List<Episode>,
    val dump: Boolean,
    val watchStrategy: WatchStrategy = WatchStrategy(true)
)

@JsonClass(generateAdapter = true)
data class WatchStrategy(val realTime: Boolean,
                         val startDate: Date? = null,
                         val intervalDays: Int? = null)