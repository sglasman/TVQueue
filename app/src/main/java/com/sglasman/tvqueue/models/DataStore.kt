package com.sglasman.tvqueue.models

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.sglasman.tvqueue.appContext
import com.sglasman.tvqueue.decode
import com.sglasman.tvqueue.encode
import com.sglasman.tvqueue.models.series.Series
import com.sglasman.tvqueue.moshi
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataStore(val watchingSeries: List<Series> = listOf())

private var dataStoreBacking: DataStore? = null

private const val DATA_STORE = "DATA_STORE"
private fun getStoreFromStorage(): DataStore =
    appContext.getSharedPreferences(DATA_STORE, MODE_PRIVATE)
        .getString(DATA_STORE, null)
        ?.let { moshi.decode<DataStore>(it) }
        ?: run {
            val newDataStore = DataStore()
            appContext.getSharedPreferences(DATA_STORE, MODE_PRIVATE)
                .edit()
                .putString(DATA_STORE, moshi.encode(newDataStore))
                .apply()
            newDataStore
        }

var dataStore: DataStore
    get() = dataStoreBacking ?: run {
        dataStoreBacking = getStoreFromStorage()
        dataStoreBacking!!
    }
    set(value) {
        dataStoreBacking = value
        appContext.getSharedPreferences(DATA_STORE, MODE_PRIVATE)
            .edit()
            .putString(DATA_STORE, moshi.encode(value))
            .apply()
    }