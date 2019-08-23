package com.sglasman.tvqueue

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun launch(context: CoroutineContext = Dispatchers.Main, block: suspend CoroutineScope.() -> Unit)
  = CoroutineScope(context).launch { block() }
fun<T> async(context: CoroutineContext = Dispatchers.Main, block: suspend CoroutineScope.() -> T):
        Deferred<T> = CoroutineScope(context).async { block() }