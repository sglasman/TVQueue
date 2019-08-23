package com.sglasman.tvqueue.models

sealed class DialogMode {
    object NotShown: DialogMode()
    object AddSeries: DialogMode()
}