package com.sglasman.tvqueue.api

import android.util.Log
import com.sglasman.tvqueue.TVQResponse
import com.sglasman.tvqueue.apiWrapper
import com.sglasman.tvqueue.models.series.Series
import com.sglasman.tvqueue.models.series.mergeOrAddSeriesMultiple
import com.sglasman.tvqueue.models.storage.dataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
suspend fun Series.checkForNewSeasonsInBackgroundAndMerge(): Series? =
    apiWrapper.getSeries(id, name).let {
        if (it is TVQResponse.Success) {
            Log.d("Updater","Pulled updated series $name")
            it.value.copy(
                seasons = it.value.seasons.filter {
                    it.number in this.seasons.map { it.number } ||
                            (this.alertFutureSeasons &&
                                    it.number > this.seasons.map { it.number }.max() ?: 0)
                }.map {season ->
                    season.copy(episodes = season.episodes.filter {
                        it.numberInSeason >= this.seasons.find { it.number == season.number}
                            ?.startingEpisode
                                ?: Int.MIN_VALUE
                    })
                }
            )
        } else null
    }

@ExperimentalCoroutinesApi
suspend fun runUpdates() {
    dataStore = dataStore.copy(
        watchingSeries = dataStore.watchingSeries.mergeOrAddSeriesMultiple(
            dataStore.watchingSeries.mapNotNull { it.checkForNewSeasonsInBackgroundAndMerge() }
        ))
}