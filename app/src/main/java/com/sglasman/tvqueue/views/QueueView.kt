package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.sglasman.tvqueue.*
import com.sglasman.tvqueue.models.queue.QueueModel
import kotlinx.android.synthetic.main.queue_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class QueueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val queueAdapter = QueueAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.queue_view, this, true)
        queue_recycler.run {
            adapter = queueAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun update(model: QueueModel) {
        queueAdapter.submitList(model.queue)
    }

    fun refreshData() {
        launch {
            sendAction(AppAction.GetQueue)
        }
    }
}