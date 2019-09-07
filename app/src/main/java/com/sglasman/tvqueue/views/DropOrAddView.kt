package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sglasman.tvqueue.AppAction
import com.sglasman.tvqueue.R
import com.sglasman.tvqueue.models.DropOrAddModel
import com.sglasman.tvqueue.sendAction
import kotlinx.android.synthetic.main.drop_or_add_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DropOrAddView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.drop_or_add_view, this, true)
    }

    fun update(model: DropOrAddModel) {
        add_another_text.setSuspendingOnClickListener {
            model.item?.let {
                sendAction(AppAction.SearchAction.ResultClicked(it), addPreviousToBackstack = true)
            }
        }
        drop_text.setSuspendingOnClickListener {
            model.item?.let {
                sendAction(AppAction.SearchAction.DropClicked(it), addPreviousToBackstack = true)
            }
        }
    }
}