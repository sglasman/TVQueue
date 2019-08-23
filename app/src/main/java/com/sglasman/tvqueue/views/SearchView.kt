package com.sglasman.tvqueue.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.sglasman.tvqueue.*
import com.sglasman.tvqueue.models.search.SearchModel
import com.sglasman.tvqueue.models.search.SearchStatus
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val searchTextWatcher: TextWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            showOnlyViews(progress_bar)
            launch {
                sendAction(AppAction.SearchAction.SearchTextChanged(text.toString()),
                    AppActionOptions.CancelSearch)
            }
        }
    }

    private val searchAdapter: SearchAdapter by lazy { SearchAdapter() }

    init {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true)
        back_button.run {
            setImageResource(R.drawable.ic_arrow_back_black_24dp)
            setSuspendingOnClickListener { sendAction(AppAction.SearchAction.Back) }
        }
        search_icon.setImageResource(R.drawable.ic_search_black_24dp)
        search_edit_text.addTextChangedListener(searchTextWatcher)
        search_recycler.run {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun update(model: SearchModel) {
        when(val status = model.status) {
            is SearchStatus.Results ->
                if (status.results.isEmpty()) showOnlyViews(no_results_text)
                else {
                    searchAdapter.submitList(status.results)
                    search_recycler.scrollToPosition(0)
                    showOnlyViews(search_recycler)
                }
            is SearchStatus.Loading -> showOnlyViews(progress_bar)
        }
    }

    private fun showOnlyViews(vararg views: View) {
        listOf(search_recycler, error_text, progress_bar, no_results_text)
            .forEach { it.visibility = GONE }
        views.forEach { it.visibility = View.VISIBLE }
    }

    fun refreshData() {
        searchAdapter.notifyDataSetChanged()
    }
}