package com.sglasman.tvqueue.models.storage

import com.sglasman.tvqueue.decode
import com.sglasman.tvqueue.encode
import com.sglasman.tvqueue.models.addDays
import com.sglasman.tvqueue.models.roundToDay
import com.sglasman.tvqueue.models.series.Episode
import com.sglasman.tvqueue.models.series.Series
import com.sglasman.tvqueue.models.series.mergeOrAddSeries
import com.sglasman.tvqueue.moshi
import com.sglasman.tvqueue.storage
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class DataStore(val watchingSeries: List<Series> = listOf(),
                     val episodesById: MutableMap<String, Episode> = mutableMapOf()) {
    fun mergeOrAddSeries(newSeries: Series): DataStore = copy(
        watchingSeries = watchingSeries.mergeOrAddSeries(newSeries)
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
        dataStoreBacking = value
            .apply {
            episodesById.clear()
            watchingSeries.flatMap { it.seasons }.forEach {
                it.episodes.forEach {
                    episodesById[it.internalID] = it
                }
            }
        }
        storage.saveString(DATA_STORE, moshi.encode(dataStoreBacking))
    }

fun DataStore.markEpisodeWatched(internalId: String): DataStore =
    (episodesById[internalId]?.let { episode ->
        if (episode.dateToWatch == null || episode.dateToWatch <= Date().roundToDay()) this else
            copy(
                watchingSeries = watchingSeries.map {
                    if (it.name != episode.seriesTitle) it else it.copy(
                        seasons = it.seasons.map {
                            if (it.number != episode.seasonNumber) it else it.copy(
                                episodes = it.episodes.map {
                                    if (it.numberInSeason <= episode.numberInSeason) it
                                    else it.copy(
                                        dateToWatch = Date().roundToDay()
                                            .addDays(7 * (it.numberInSeason - episode.numberInSeason))
                                    )
                                }
                            )
                        }
                    )
                }
            )
    } ?: this).apply { episodesById[internalId]?.watched = true }

fun synchronizeEpisodeIds() { dataStore = dataStore }