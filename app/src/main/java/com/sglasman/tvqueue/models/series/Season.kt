package com.sglasman.tvqueue.models.series

import android.util.Log
import com.sglasman.tvqueue.mergeOrAdd
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Season(
    val number: Int,
    val episodes: List<Episode>,
    val dump: Boolean
) {
    val episodeNumbers = episodes.map { it.numberInSeason }

    private fun mergeOrAddEpisode(newEpisode: Episode): Season {
        Log.d("Merge", "Merging $newEpisode into $episodes")
        return copy(episodes =
            episodes.mergeOrAdd(
                newEpisode,
                matcher = { episode1, episode2 ->
                    episode1.seasonNumber == episode2.seasonNumber
                            && episode1.numberInSeason == episode2.numberInSeason
                },
                merge = Episode::merge
            )
        )
    }

    fun mergeOrAddEpisodes(newEpisodes: List<Episode>): Season = newEpisodes.firstOrNull()?.let {
        mergeOrAddEpisode(it).mergeOrAddEpisodes(newEpisodes.drop(1))
    } ?: this
}