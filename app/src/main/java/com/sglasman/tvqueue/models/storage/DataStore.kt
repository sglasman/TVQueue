package com.sglasman.tvqueue.models.storage

import com.sglasman.tvqueue.decode
import com.sglasman.tvqueue.encode
import com.sglasman.tvqueue.models.series.Series
import com.sglasman.tvqueue.moshi
import com.sglasman.tvqueue.storage
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataStore(val watchingSeries: List<Series> = listOf())

private var dataStoreBacking: DataStore? = null

private const val DATA_STORE = "DATA_STORE"
private fun getStoreFromStorage(): DataStore =
    storage.retrieveString(DATA_STORE)
        ?.let { moshi.decode<DataStore>(it) }
        ?: run {
            val newDataStore = DataStore()
            storage.saveString(DATA_STORE, moshi.encode(newDataStore))
            newDataStore
        }

var dataStore: DataStore
    get() = dataStoreBacking ?: run {
        dataStoreBacking = getStoreFromStorage()
        dataStoreBacking!!
    }
    set(value) {
        dataStoreBacking = value
        storage.saveString(DATA_STORE, moshi.encode(value))
    }