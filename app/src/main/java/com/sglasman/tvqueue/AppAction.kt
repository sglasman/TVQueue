package com.sglasman.tvqueue

import com.sglasman.tvqueue.models.search.SearchResult

sealed class AppAction {

    object Login : AppAction()
    object BackPressed : AppAction()

    sealed class SearchAction: AppAction() {
        object Back : SearchAction()
        data class SearchTextChanged(val text: String): SearchAction()
        data class SearchNow(val text: String): SearchAction()
        data class ResultClicked(val item: SearchResult): SearchAction()
        data class GetSeriesFromResult(val item: SearchResult): SearchAction()
    }

    sealed class AddSeriesAction: AppAction() {
        object SeasonUpClicked: AddSeriesAction()
        object SeasonDownClicked: AddSeriesAction()
    }
}