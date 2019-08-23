package com.sglasman.tvqueue

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun launch(context: CoroutineContext = mainContext, block: suspend CoroutineScope.() -> Unit)
  = CoroutineScope(context).launch { block() }
fun<T> async(context: CoroutineContext = mainContext, block: suspend CoroutineScope.() -> T):
        Deferred<T> = CoroutineScope(context).async { block() }