package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Series(val id: Int,
                  val name: String,
                  val seasons: List<Season>)