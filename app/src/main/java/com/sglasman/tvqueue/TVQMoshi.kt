package com.sglasman.tvqueue

import com.squareup.moshi.Moshi
import java.lang.Exception

inline fun <reified T> Moshi.decode(s: String): T? = try {
    adapter(T::class.java).fromJson(s)
} catch (e: Exception) {
    null
}