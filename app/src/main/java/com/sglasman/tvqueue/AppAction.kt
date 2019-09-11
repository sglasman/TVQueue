package com.sglasman.tvqueue

import com.sglasman.tvqueue.models.queue.QueueItem
import com.sglasman.tvqueue.models.search.SearchResult
import java.util.*

sealed class AppAction {

    object Login : AppAction()
    object BackPressed : AppAction()
    data class SeriesTextClicked(val searchText: String) : AppAction()
    object QueueTextClicked : AppAction()
    object GetQueue : AppAction()
    object RunUpdates : AppAction()
    object GetReadyToRestart: AppAction()

    sealed class QueueAction: AppAction() {
        class QueueItemClicked(val item: QueueItem.Episode): QueueAction()
        class MarkWatchedConfirmed(val item: QueueItem.Episode): QueueAction()
    }

    sealed class SearchAction: AppAction() {
        object Back : SearchAction()
        data class SearchTextChanged(val text: String): SearchAction()
        data class SearchNow(val text: String): SearchAction()
        data class ResultClicked(val item: SearchResult): SearchAction()
        data class ResultClickedAlreadyWatching(val item: SearchResult): SearchAction()
        data class GetSeriesFromResult(val item: SearchResult): SearchAction()
        data class DropClicked(val item: SearchResult): SearchAction()
        data class DropConfirmed(val item: SearchResult): SearchAction()
    }

    sealed class AddSeriesAction: AppAction() {
        object SeasonUpClicked: AddSeriesAction()
        object SeasonDownClicked: AddSeriesAction()
        object JustAddFutureSeasonsClicked: AddSeriesAction()
        object JustAddFutureSeasonsConfirmed: AddSeriesAction()
        object SeasonNextClicked: AddSeriesAction()
        object EpisodeUpClicked: AddSeriesAction()
        object EpisodeDownClicked: AddSeriesAction()
        object FinishClickedAfterEpisodeSelect: AddSeriesAction()
        object FinishConfirmedAfterEpisodeSelect: AddSeriesAction()
        object NextClickedAfterEpisodeSelect: AddSeriesAction()
        object SeparationUpClicked: AddSeriesAction()
        object SeparationDownClicked: AddSeriesAction()
        object ScheduleClicked: AddSeriesAction()
        class ScheduleSeason(val startDate: Date?): AddSeriesAction()
        object UseOriginalClicked: AddSeriesAction()
    }
}