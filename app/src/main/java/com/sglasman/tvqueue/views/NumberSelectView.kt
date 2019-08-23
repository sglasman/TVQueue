package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.sglasman.tvqueue.R
import kotlinx.android.synthetic.main.number_select.view.*

class NumberSelectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.number_select, this, true)
        up_arrow_image.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
        down_arrow_image.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
    }

    fun setNumber(number: Int) {
        number_text.text = number.toString()
    }

    fun setArrowVisibilities(upVisible: Boolean, downVisible: Boolean) {
        up_arrow_image.visibility = upVisible.toVisibility()
        up_arrow_clickable_area.isEnabled = upVisible
        down_arrow_image.visibility = downVisible.toVisibility()
        down_arrow_clickable_area.isEnabled = downVisible
    }

    fun setUpArrowClickedListener(listener: suspend (View) -> Unit) {
        up_arrow_clickable_area.setSuspendingOnClickListener(listener)
    }

    fun setDownArrowClickedListener(listener: suspend (View) -> Unit) {
        down_arrow_clickable_area.setSuspendingOnClickListener(listener)
    }
}