package com.sglasman.tvqueue.models

sealed class Screen {
    object SearchScreen: Screen()
    object Finishing: Screen()
}