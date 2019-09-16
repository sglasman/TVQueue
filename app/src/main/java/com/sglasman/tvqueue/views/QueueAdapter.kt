package com.sglasman.tvqueue.views

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sglasman.tvqueue.AppAction
import com.sglasman.tvqueue.R
import com.sglasman.tvqueue.models.queue.QueueItem
import com.sglasman.tvqueue.sendAction
import kotlinx.android.synthetic.main.queue_item_view.view.*
import kotlinx.android.synthetic.main.queue_separator.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class QueueAdapter : ListAdapter<QueueItem, QueueAdapter.QueueHolder>(QueueItemDiff()) {

    @ExperimentalCoroutinesApi
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueHolder =
        if (viewType == EPISODE) QueueHolder.EpisodeHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.queue_item_view,
                parent,
                false
            )
        ) else QueueHolder.SeparatorHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.queue_separator,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: QueueHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is QueueItem.Episode -> EPISODE
        is QueueItem.Separator -> SEPARATOR
    }

    sealed class QueueHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(item: QueueItem)

        @ExperimentalCoroutinesApi
        class EpisodeHolder(itemView: View) : QueueHolder(itemView) {
            override fun bind(item: QueueItem) {
                item as QueueItem.Episode
                itemView.run {
                    series_title_text.text = item.seriesTitle
                    season_episode_text.text = item.seasonNumber?.let {
                        "S${it}E${item.episodeNumber}"
                    } ?: "E${item.episodeNumber}"
                    episode_title_text.visibility =
                        if (item.episodeTitle.isBlank()) GONE else VISIBLE
                    episode_title_text.text = item.episodeTitle
                    setSuspendingOnClickListener {
                        sendAction(AppAction.QueueAction.QueueItemClicked(item),
                            addPreviousToBackstack = true)
                    }
                }
            }
        }

        class SeparatorHolder(itemView: View) : QueueHolder(itemView) {
            override fun bind(item: QueueItem) {
                item as QueueItem.Separator
                itemView.run {
                    separator_layout.setBackgroundColor(
                        ContextCompat.getColor(
                            context, when (item) {
                                is QueueItem.Separator.Dark -> R.color.darkGrey
                                is QueueItem.Separator.Light -> R.color.midGrey
                            }
                        )
                    )
                    separator_text.text = item.text
                }
            }
        }
    }
    companion object {
        const val EPISODE = 0
        const val SEPARATOR = 1
    }
}

class QueueItemDiff : DiffUtil.ItemCallback<QueueItem>() {
    override fun areItemsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean =
        (oldItem is QueueItem.Episode &&
                newItem is QueueItem.Episode &&
                oldItem.seriesTitle == newItem.seriesTitle &&
                oldItem.episodeNumber == newItem.episodeNumber &&
                oldItem.seasonNumber == newItem.seasonNumber) ||
                (oldItem !is QueueItem.Episode &&
                        newItem !is QueueItem.Episode &&
                        oldItem == newItem)

    override fun areContentsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean =
        oldItem == newItem
}