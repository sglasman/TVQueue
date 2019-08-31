package com.sglasman.tvqueue.models.series

import android.util.Log
import com.sglasman.tvqueue.mergeOrAdd
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Series(
    val id: Int,
    val name: String,
    val seasons: List<Season>,
    val alertFutureSeasons: Boolean = true,
    val latestSeason: Int? = null
) {
    fun getSeasonNumbers(): List<Int> = seasons.map { it.number }.sorted()
    fun getSeasonByNumber(number: Int): Season? = seasons.find { it.number == number }
    private fun mergeOrAddSeason(newSeason: Season): Series = copy(
        seasons = seasons.mergeOrAdd(newSeason,
            matcher = { season1, season2 -> season1.number == season2.number },
            merge = { season ->
                Log.d("Merge", "Merging $season into $this")
                mergeOrAddEpisodes(season.episodes)
            })
    )

    fun mergeOrAddSeasons(newSeasons: List<Season>): Series = newSeasons.firstOrNull()?.let {
        mergeOrAddSeason(it).mergeOrAddSeasons(newSeasons.drop(1))
    } ?: this
}

fun List<Series>.mergeOrAddSeries(newSeries: Series): List<Series> = mergeOrAdd(
    newSeries,
    matcher = { series1, series2 -> series1.id == series2.id },
    merge = {
            series -> series.copy( seasons = this.mergeOrAddSeasons(series.seasons).seasons)
    })

fun List<Series>.mergeOrAddSeriesMultiple(newSeries: List<Series>): List<Series> =
    if (newSeries.isEmpty()) this
    else mergeOrAddSeries(newSeries[0]).mergeOrAddSeriesMultiple(newSeries.drop(1))