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

    fun update(model: AddSeriesModel) {
        when (model.stage) {
            is Stage.Loading -> {
                showOnlyViews(progress_bar)
            }
            is Stage.SelectSeason -> {
                showOnlyViews(
                    add_season_text,
                    title_text,
                    season_number_select,
                    just_future_text,
                    season_next_text
                )
                just_future_text.setSuspendingOnClickListener {
                    sendAction(
                        AppAction.AddSeriesAction.JustAddFutureSeasonsClicked,
                        addPreviousToBackstack = true
                    )
                }
                season_next_text.setSuspendingOnClickListener {
                    sendAction(
                        AppAction.AddSeriesAction.SeasonNextClicked,
                        addPreviousToBackstack = true
                    )
                }
                future_seasons_text.visibility = (!model.maxSeasonNotSelected).toVisibility()
                title_text.text = model.series?.name.orEmpty()
                season_number_select.run {
                    setNumber(model.selectedSeasonNumber)
                    setLabelText(context.getString(R.string.season))
                    setArrowVisibilities(
                        upVisible = model.maxSeasonNotSelected,
                        downVisible = model.minSeasonNotSelected
                    )
                    setUpArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeasonUpClicked)
                    }
                    setDownArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeasonDownClicked)
                    }
                }
            }
            is Stage.SelectStartingEpisode -> {
                showOnlyViews(
                    title_text, season_number_select, start_at_episode_text,
                    episode_number_select, episode_next_text
                )
                future_seasons_text.visibility = (!model.maxSeasonNotSelected).toVisibility()
                season_number_select.setArrowVisibilities(false, false)
                episode_number_select.run {
                    setNumber(model.selectedStartingEpisodeNumber)
                    setLabelText(context.getString(R.string.starting_at_episode))
                    setArrowVisibilities(
                        upVisible = model.maxEpisodeNotSelected,
                        downVisible = model.minEpisodeNotSelected
                    )
                    setUpArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.EpisodeUpClicked)
                    }
                    setDownArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.EpisodeDownClicked)
                    }
                }
                episode_next_text.text = context.getString(
                    if (model.shouldShowNextAfterEpisodeSelect) R.string.next
                    else R.string.finish
                )
                episode_next_text.setSuspendingOnClickListener {
                    sendAction(
                        if (model.shouldShowNextAfterEpisodeSelect)
                            AppAction.AddSeriesAction.NextClickedAfterEpisodeSelect
                        else AppAction.AddSeriesAction.FinishClickedAfterEpisodeSelect,
                        addPreviousToBackstack = true
                    )
                }
            }
            is Stage.Schedule -> {
                showOnlyViews(
                    title_text, episode_number_select, season_number_select,
                    explanation, schedule_text, use_original_text, separation_select, days_text
                )
                future_seasons_text.visibility = (!model.maxSeasonNotSelected).toVisibility()
                episode_number_select.setArrowVisibilities(false, false)
                separation_select.run {
                    setNumber(model.selectedSeparation)
                    setArrowVisibilities(
                        upVisible = true,
                        downVisible = model.selectedSeparation != 0
                    )
                    setLabelText(context.getString(R.string.separate_episodes_by))
                    setDownArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeparationDownClicked)
                    }
                    setUpArrowClickedListener {
                        sendAction(AppAction.AddSeriesAction.SeparationUpClicked)
                    }
                }
                explanation.text = context.getString(
                    when {
                        model.selectedSeason?.futureDump == true -> R.string.explanation_future_dump
                        model.dump -> R.string.explanation_past_dump
                        model.selectedSeason?.finishedAiring == true -> R.string.explanation_already_aired
                        else -> R.string.explanation_blank
                    }
                )
                days_text.text = context.resources.getQuantityText(
                    R.plurals.days,
                    model.selectedSeparation
                )
                schedule_text.run {
                    if (model.selectedSeason?.pastDump == true) text = context.getString(R.string.start_from_today)
                    setSuspendingOnClickListener {
                        sendAction(AppAction.AddSeriesAction.ScheduleClicked)
                    }
                }
                use_original_text.run {
                    if (model.selectedSeason?.pastDump == true) text = context.getString(R.string.start_from_original)
                    setSuspendingOnClickListener {
                        sendAction(AppAction.AddSeriesAction.UseOriginalClicked)
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