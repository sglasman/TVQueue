package com.sglasman.tvqueue.models.addseries

import com.sglasman.tvqueue.models.series.Series

data class AddSeriesModel(
    val series: Series? = null,
    val stage: Stage = Stage.Loading,
    val selectedSeason: Int = 1,
    val selectedStartingEpisode: Int = 1
) {
    val seasonNumbers: List<Int> = series?.getSeasonNumbers().orEmpty()

    val shouldShowSeasonUpArrow: Boolean = selectedSeason != seasonNumbers.max()

    val shouldShowSeasonDownArrow: Boolean = selectedSeason != seasonNumbers.min()
}

sealed class Stage {
    object Loading: Stage()
    object Error: Stage()
    object SelectSeason: Stage()
    object SeasonAlreadyAiredOrDump: Stage()
    object SelectStartingEpisode: Stage()
}