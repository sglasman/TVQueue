package com.sglasman.tvqueue.models

import com.sglasman.tvqueue.models.search.SearchResult

sealed class DialogScreen {
    object NotShown: DialogScreen()
    object AddSeries: DialogScreen()
    object Confirmation: DialogScreen()
    object DropOrAdd: DialogScreen()
}