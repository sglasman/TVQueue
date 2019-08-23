package com.sglasman.tvqueue.models

sealed class DialogScreen {
    object NotShown: DialogScreen()
    object AddSeries: DialogScreen()
    object Confirmation: DialogScreen()
}