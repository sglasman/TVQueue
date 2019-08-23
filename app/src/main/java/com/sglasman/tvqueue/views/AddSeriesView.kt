package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.sglasman.tvqueue.AppAction
import com.sglasman.tvqueue.R
import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.addseries.Stage
import com.sglasman.tvqueue.sendAction
import kotlinx.android.synthetic.main.add_series_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddSeriesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.add_series_view, this, true)
    }

    fun update(addSeriesModel: AddSeriesModel) {
        when(addSeriesModel.stage) {
            is Stage.Loading -> {
                showOnlyViews(progress_bar)
            }
            is Stage.SelectSeason -> {
                showOnlyViews(add_season_text, title_text, season_number_select_ll)
                title_text.text = addSeriesModel.series?.name.orEmpty()
                season_number_select.run {
                    setNumber(addSeriesModel.selectedSeason)
                    setArrowVisibilities(
                        upVisible = addSeriesModel.shouldShowSeasonUpArrow,
                        downVisible = addSeriesModel.shouldShowSeasonDownArrow
                    )
                    setUpArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeasonUpClicked)
                    }
                    setDownArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeasonDownClicked)
                    }
                }
            }
        }
    }

    private fun showOnlyViews(vararg views: View) {
        add_series_root.getChildren().forEach { it.visibility = GONE }
        views.forEach { it.visibility = VISIBLE }
    }
}