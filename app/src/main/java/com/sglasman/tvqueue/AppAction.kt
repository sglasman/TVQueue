package com.sglasman.tvqueue

import com.sglasman.tvqueue.models.search.SearchResult

sealed class AppAction {

    object Login : AppAction()

    sealed class SearchAction: AppAction() {
        object Back : SearchAction()
        data class SearchTextChanged(val text: String): SearchAction()
        data class SearchNow(val text: String): SearchAction()
        data class ResultClicked(val item: SearchResult): SearchAction()
        data class GetSeriesFromResult(val item: SearchResult): SearchAction()
    }
}