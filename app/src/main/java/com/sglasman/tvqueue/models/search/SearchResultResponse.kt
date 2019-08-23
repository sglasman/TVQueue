package com.sglasman.tvqueue.models.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResultResponse(val seriesName: String?,
                                val overview: String?,
                                val firstAired: String?,
                                val id: Int,
                                val status: String,
                                val aliases: List<String>) {

    fun toSearchResult(): SearchResult? = (seriesName ?: aliases.getOrNull(0))?.let {
        SearchResult(
            name = it,
            preview = overview.orEmpty(),
            id = id,
            status = when(status) {
                "Continuing" -> SeriesStatus.Continuing
                "Ended" -> SeriesStatus.Ended
                else -> SeriesStatus.Unknown
            },
            year = firstAired?.let {
                it.takeWhile { it != '-' }.toIntOrNull()
            }
        )
    }
}