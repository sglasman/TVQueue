package com.sglasman.tvqueue

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sglasman.tvqueue.models.AppModel
import com.sglasman.tvqueue.models.DialogScreen
import com.sglasman.tvqueue.models.Screen
import com.sglasman.tvqueue.views.*
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

@ExperimentalCoroutinesApi
class TVQActivity : AppCompatActivity() {

    private val queueView by lazy { QueueView(this) }
    private val searchView by lazy { SearchView(this) }
    private val addSeriesView by lazy { AddSeriesView(this) }
    private val confirmationView by lazy { ConfirmationView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)
        click_shield.setOnClickListener {
            Log.d("ClickShield", "Clicked")
        }
        launch { appModel.openSubscription().consumeEach { update(it) } }
        launch { refreshChannel.consumeEach { refreshData() }}
        launch { imeChannel.consumeEach {
                if (!it) forceHideKeyboard(searchView)
            }}
        series_text.setSuspendingOnClickListener {
            sendAction(AppAction.SeriesTextClicked(searchView.getSearchText()))
        }
        queue_text.setSuspendingOnClickListener {
            sendAction(AppAction.QueueTextClicked, options = AppActionOptions.CancelSearch)
        }
    }

    override fun onBackPressed() {
        launch { sendAction(AppAction.BackPressed) }
    }

    private fun update(model: AppModel) {
        when (model.currentScreen) {
            is Screen.QueueScreen ->{
                main_content.swapViewIdempotent(queueView)
                queue_text.run {
                    setTextColor(ContextCompat.getColor(context, R.color.blue))
                    setTypeface(typeface, Typeface.BOLD)
                }
                series_text.run {
                    setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text))
                    setTypeface(typeface, Typeface.NORMAL)
                }
                queueView.update(model.queueModel)
            }
            is Screen.SearchScreen -> {
                main_content.swapViewIdempotent(searchView)
                series_text.run {
                    setTextColor(ContextCompat.getColor(context, R.color.blue))
                    setTypeface(typeface, Typeface.BOLD)
                }
                queue_text.run {
                    setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text))
                    setTypeface(typeface, Typeface.NORMAL)
                }
                searchView.update(model.searchModel)
            }
            is Screen.Finishing -> finish()
        }
        dialog_content.visibility = VISIBLE
        click_shield.visibility = VISIBLE
        click_shield.bringToFront()
        dialog_content.bringToFront()
        when (model.dialogScreen) {
            is DialogScreen.NotShown -> {
                dialog_content.visibility = GONE
                click_shield.visibility = GONE
            }
            is DialogScreen.AddSeries -> {
                dialog_content.swapViewIdempotent(addSeriesView)
                addSeriesView.update(model.addSeriesModel)
            }
            is DialogScreen.Confirmation -> {
                dialog_content.swapViewIdempotent(confirmationView)
                confirmationView.update(model.confirmationModel)
            }
        }
    }

    private fun refreshData() {
        searchView.refreshData()
        queueView.refreshData()
    }
}
