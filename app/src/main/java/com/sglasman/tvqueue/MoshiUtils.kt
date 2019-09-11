package com.sglasman.tvqueue

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.io.IOException
import java.lang.Exception
import java.util.*

inline fun <reified T> Moshi.decode(s: String): T? = try {
    adapter(T::class.java).fromJson(s)
} catch (e: Exception) {
    null
}

inline fun <reified T> Moshi.encode(t: T): String = adapter(T::class.java).toJson(t)

//class Rfc3339DateJsonAdapterNullable : JsonAdapter<Date>() {
//
//    private val rfc3339DateJsonAdapter: Rfc3339DateJsonAdapter = Rfc3339DateJsonAdapter()
//
//    @Synchronized
//    @Throws(IOException::class)
//    override fun fromJson(reader: JsonReader): Date? {
//        val string = reader.nextString()
//        return rfc3339DateJsonAdapter.fromJson(string)
//    }
//
//    @Synchronized
//    @Throws(IOException::class)
//    override fun toJson(writer: JsonWriter, value: Date?) {
//        value?.let { rfc3339DateJsonAdapter.toJson(it) } ?: writer.value(null as String?)
//    }
//}