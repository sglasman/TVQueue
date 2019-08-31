package com.sglasman.tvqueue.models.series

import android.util.Log
import com.sglasman.tvqueue.getCurrentDate
import com.sglasman.tvqueue.mergeOrAdd
import com.sglasman.tvqueue.models.addDays
import com.sglasman.tvqueue.models.roundToDay
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Season(
    val number: Int,
    val episodes: List<Episode>,
    val dump: Boolean,
    val useOriginalAirdates: Boolean?,
    val startDate: Date?,
    val intervalDays: Int?,
    val startingEpisode: Int?
) {
    val episodeNumbers = episodes.map { it.numberInSeason }

    private fun mergeOrAddEpisode(newEpisode: Episode): Season {
        Log.d("Merge", "Merging $newEpisode into $episodes")
        return copy(episodes =
            episodes.mergeOrAdd(
                newEpisode.copy(
                    dateToWatch = if (useOriginalAirdates != false) newEpisode.dateToWatch
                    else startDate?.addDays((intervalDays ?: 7) * (newEpisode.numberInSeason - 1))
                        ?: newEpisode.dateToWatch
                ),
                matcher = { episode1, episode2 ->
                    episode1.seasonNumber == episode2.seasonNumber
                            && episode1.numberInSeason == episode2.numberInSeason
                },
                merge = {
                    merge(it, overwriteDateToWatch = useOriginalAirdates == true)
                }
            )
        )
    }

    fun mergeOrAddEpisodes(newEpisodes: List<Episode>): Season = newEpisodes.firstOrNull()?.let {
        mergeOrAddEpisode(it).mergeOrAddEpisodes(newEpisodes.drop(1))
    } ?: this

    val earliestAirdate: Date by lazy {
        episodes.map { it.airDate }.min() ?: getCurrentDate().roundToDay()
    }

    val latestAirdate: Date by lazy {
        episodes.map { it.airDate }.max() ?: getCurrentDate().roundToDay()
    }

    val finishedAiring: Boolean by lazy {
        latestAirdate <= getCurrentDate()
    }

    val pastDump: Boolean by lazy { dump && finishedAiring }

    val futureDump: Boolean by lazy { dump && !pastDump }
}