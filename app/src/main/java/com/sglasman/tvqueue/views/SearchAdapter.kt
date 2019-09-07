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
import com.sglasman.tvqueue.models.search.SearchResult
import com.sglasman.tvqueue.models.search.SeriesStatus
import com.sglasman.tvqueue.models.storage.dataStore
import com.sglasman.tvqueue.sendAction
import kotlinx.android.synthetic.main.search_result_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SearchAdapter :
    ListAdapter<SearchResult, SearchAdapter.SearchResultHolder>(SearchDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultHolder =
        SearchResultHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.search_result_view, parent, false
            )
        )

    override fun onBindViewHolder(holder: SearchResultHolder, position: Int) {
        holder.itemView.run {
            val item = getItem(position)
            title_text.text = item.name
            preview_text.text = item.preview
            ended_or_ongoing_text.run {
                when (item.status) {
                    is SeriesStatus.Continuing -> {
                        text = context.getString(R.string.ongoing)
                        setTextColor(ContextCompat.getColor(context, R.color.green))
                    }
                    is SeriesStatus.Ended -> {
                        text = context.getString(R.string.ended)
                        setTextColor(ContextCompat.getColor(context, R.color.red))
                    }
                    is SeriesStatus.Unknown -> visibility = GONE
                }
            }
            date_text.run {
                item.year?.let { text = it.toString() } ?: run {
                    visibility = GONE
                    holder.itemView.buffer.visibility = VISIBLE
                }
            }
            tick_image.setImageResource(R.drawable.ic_check_black_24dp)
            val watching = dataStore.watchingSeries.any { it.id == item.id }
            listOf(watching_bar, tick_container, tick_image).forEach {
                it.visibility = watching.toVisibility()
            }
            setSuspendingOnClickListener {
                sendAction(
                    if (watching) AppAction.SearchAction.ResultClickedAlreadyWatching(item)
                    else AppAction.SearchAction.ResultClicked(item),
                    addPreviousToBackstack = true
                )
            }
        }
    }

    class SearchResultHolder(view: View) : RecyclerView.ViewHolder(view)

    class SearchDiffUtil : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) =
            oldItem == newItem
    }
}