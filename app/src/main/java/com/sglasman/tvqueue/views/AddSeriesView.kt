package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sglasman.tvqueue.R
import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.addseries.Stage
import kotlinx.android.synthetic.main.add_series_view.view.*

class AddSeriesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.add_series_view, this, true)
    }

    fun update(addSeriesModel: AddSeriesModel) {
        when(addSeriesModel.stage) {
            is Stage.Loading -> {
                add_series_root.getChildren().forEach { it.visibility = GONE }
                progress_bar.visibility = VISIBLE
            }
        }
    }
}