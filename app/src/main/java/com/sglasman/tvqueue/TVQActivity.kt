package com.sglasman.tvqueue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.DialogMode
import com.sglasman.tvqueue.views.AddSeriesView
import com.sglasman.tvqueue.views.SearchView
import com.sglasman.tvqueue.views.addViewIdempotent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

@ExperimentalCoroutinesApi
class TVQActivity : AppCompatActivity() {

    private val searchView by lazy { SearchView(this) }
    private val addSeriesView by lazy { AddSeriesView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_content.addView(searchView)
        launch { appModel.openSubscription().consumeEach { update(it) } }
    }

    private fun update(model: AppModel) {
        dialog_content.visibility = VISIBLE
        dialog_content.bringToFront()
        when (model.dialogMode) {
            is DialogMode.NotShown -> dialog_content.visibility = GONE
            is DialogMode.AddSeries -> {
                dialog_content.addViewIdempotent(addSeriesView)
                addSeriesView.update(model.addSeriesModel)
            }
        }
        searchView.update(model.searchModel)
    }
}
