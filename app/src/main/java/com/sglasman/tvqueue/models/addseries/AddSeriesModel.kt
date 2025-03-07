package com.sglasman.tvqueue.models.addseries

import com.sglasman.tvqueue.models.series.Season
import com.sglasman.tvqueue.models.series.Series

data class AddSeriesModel(
    val series: Series? = null,
    val stage: Stage = Stage.Loading,
    val selectedSeasonNumber: Int = 1,
    val selectedStartingEpisodeNumber: Int = 1,
    val selectedSeparation: Int = 7
) {
    val seasonNumbers: List<Int> = series?.getSeasonNumbers().orEmpty()

    val maxSeasonNotSelected: Boolean = selectedSeasonNumber != seasonNumbers.max()

    val minSeasonNotSelected: Boolean = selectedSeasonNumber != seasonNumbers.min()

    val selectedSeason: Season? = series?.seasons?.find { it.number == selectedSeasonNumber }

    val episodeNumbersInSelectedSeason = selectedSeason?.episodeNumbers.orEmpty()

    val maxEpisodeNotSelected: Boolean =
        selectedStartingEpisodeNumber != episodeNumbersInSelectedSeason.max()

    val minEpisodeNotSelected: Boolean =
        selectedStartingEpisodeNumber != episodeNumbersInSelectedSeason.min()

    val dump = selectedSeason?.dump == true
//  ID-0-6979000027355

    val shouldShowNextAfterEpisodeSelect: Boolean by lazy {
        dump || selectedSeason?.finishedAiring == true
    }
}

sealed class Stage {
    object Loading : Stage()
    object Error : Stage()
    object SelectSeason : Stage()
    object SelectStartingEpisode : Stage()
    object Schedule : Stage()
}