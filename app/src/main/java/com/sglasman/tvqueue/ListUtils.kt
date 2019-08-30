package com.sglasman.tvqueue

fun <T> List<T>.mergeOrAdd(
    newElement: T,
    matcher: (T, T) -> Boolean,
    merge: T.(T) -> T = { it }
): List<T> = find {
    matcher(it, newElement)
}?.let { map { if (matcher(it, newElement)) it.merge(newElement) else it } }
    ?: plus(newElement)