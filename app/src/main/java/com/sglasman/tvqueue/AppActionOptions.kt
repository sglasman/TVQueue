package com.sglasman.tvqueue

import kotlin.reflect.KClass

sealed class AppActionOptions {
    object CancelSearch: AppActionOptions()
    object CancelAllPending: AppActionOptions()
}