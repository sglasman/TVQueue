package com.sglasman.tvqueue.models.search

data class SearchResult(val name: String,
                        val preview: String,
                        val id: Int,
                        val status: SeriesStatus,
                        val year: Int?)

sealed class SeriesStatus {
    object Continuing: SeriesStatus()
    object Ended: SeriesStatus()
    object Unknown: SeriesStatus()
}