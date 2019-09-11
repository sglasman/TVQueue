package com.sglasman.tvqueue.models.queue

import com.sglasman.tvqueue.getCurrentDate
import java.text.SimpleDateFormat
import java.util.*

sealed class QueueItem {
    data class Episode(
        val watched: Boolean,
        val seriesTitle: String,
        val episodeTitle: String,
        val seasonNumber: Int?,
        val episodeNumber: Int,
        val dateToWatch: Date?,
        val internalID: String
    ) : QueueItem(), Comparable<Episode> {

        override fun compareTo(other: Episode): Int =
            if (dateToWatch == other.dateToWatch) {
                if (episodeNumber == other.episodeNumber) {
                    episodeTitle.compareTo(other.episodeTitle)
                } else episodeNumber.compareTo(other.episodeNumber)
            } else if (dateToWatch == null) 1
            else if (other.dateToWatch == null) -1
            else dateToWatch.compareTo(other.dateToWatch)
    }

    sealed class Separator(open val text: String) : QueueItem() {
        data class Dark(override val text: String) : Separator(text)
        data class Light(override val text: String) : Separator(text)
    }

    override fun equals(other: Any?): Boolean {
        return (this is Episode && other is Episode && this as Episode == other as Episode) ||
                (this is Separator.Dark && other is Separator.Dark && this as Separator.Dark == other as Separator.Dark) ||
                (this is Separator.Light && other is Separator.Light && this as Separator.Light == other as Separator.Light)
    }
}

fun List<QueueItem.Episode>.addSeparators(): List<QueueItem> {

    if (isEmpty()) return listOf()

    val datesToItems = groupBy { it.dateToWatch }
    val dates: List<Date?> = datesToItems.keys.toList()
    val groups: List<List<QueueItem.Episode>> = datesToItems.values.toList()

    val earliestDateIfBeforeToday: Date? = this[0].dateToWatch?.let {
        if (it <= getCurrentDate()) it else null
    }

    val earliestDateAfterToday: Date? = dates.filterNotNull().filter { it > getCurrentDate() }.min()

    val returnValue =  groups.map {
        (when {
            it[0].dateToWatch == null -> listOf(QueueItem.Separator.Dark("Date TBA"))
            it[0].dateToWatch == earliestDateIfBeforeToday -> listOf(QueueItem.Separator.Dark("Queued"))
            it[0].dateToWatch == earliestDateAfterToday -> listOf(QueueItem.Separator.Dark("Upcoming"))
            else -> listOf<QueueItem>()
        }) +
                (it[0].dateToWatch?.let { date ->
                    listOf(
                        QueueItem.Separator.Light(
                            SimpleDateFormat("EEE, d MMMM yyyy", Locale.getDefault())
                                .format(date)
                        )
                    )
                }.orEmpty()) +
                it
    }.flatten()
    return returnValue
}
