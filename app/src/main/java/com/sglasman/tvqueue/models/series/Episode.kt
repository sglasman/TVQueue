package com.sglasman.tvqueue.models.series

import com.sglasman.tvqueue.models.queue.QueueItem
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Episode(
    var watched: Boolean = false,
    val seriesTitle: String,
    val title: String,
    val seasonNumber: Int?,
    val numberInSeason: Int,
    val airDate: Date,
    val dateToWatch: Date,
    val internalID: String
) {
    fun merge(newEpisode: Episode, overwriteDateToWatch: Boolean = false): Episode =
        newEpisode.copy(watched = watched || newEpisode.watched,
            dateToWatch = if (overwriteDateToWatch) newEpisode.dateToWatch else dateToWatch)

    fun toQueueItem(): QueueItem.Episode =
        QueueItem.Episode(
            watched = watched,
            seriesTitle = seriesTitle,
            episodeTitle = title,
            seasonNumber = seasonNumber,
            episodeNumber = numberInSeason,
            dateToWatch = dateToWatch,
            internalID = internalID
        )
}