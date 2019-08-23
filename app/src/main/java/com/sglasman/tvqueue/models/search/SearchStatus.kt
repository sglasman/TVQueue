package com.sglasman.tvqueue.models.search

sealed class SearchStatus {
    data class Results(val results: List<SearchResult>): SearchStatus()
    object Loading: SearchStatus()
    object Error: SearchStatus()
}