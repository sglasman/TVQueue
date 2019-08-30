package com.sglasman.tvqueue.models

sealed class Screen {
    object QueueScreen: Screen()
    object SearchScreen: Screen()
    object Finishing: Screen()
}