package com.sglasman.tvqueue.models.storage

import com.sglasman.tvqueue.*
import com.sglasman.tvqueue.models.series.Episode
import com.sglasman.tvqueue.models.series.Series
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class DataStore(val watchingSeries: List<Series> = listOf(),
                     val episodesById: MutableMap<String, Episode> = mutableMapOf()) {
    fun mergeOrAddSeries(newSeries: Series): DataStore = copy(
        watchingSeries = watchingSeries.mergeOrAdd(
            newSeries,
            matcher = { series1, series2 -> series1.id == series2.id },
            merge = {
                    series -> series.copy( seasons = this.mergeOrAddSeasons(series.seasons).seasons)
            })
    )
}

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
        dataStoreBacking = value.apply {
            watchingSeries.flatMap { it.seasons }.flatMap { it.episodes }.forEach {
                episodesById[it.internalID] = it
            }
        }
        storage.saveString(DATA_STORE, moshi.encode(dataStoreBacking))
    }

fun synchronizeEpisodeIds() { dataStore = dataStore }