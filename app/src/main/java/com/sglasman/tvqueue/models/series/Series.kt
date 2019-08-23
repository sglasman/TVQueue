package com.sglasman.tvqueue.models.series

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Series(val id: Int,
                  val name: String,
                  val seasons: List<Season>,
                  val alertFutureSeasons: Boolean = true,
                  val latestSeason: Int) {
    fun getSeasonNumbers(): List<Int> = seasons.map { it.number }.sorted()
}