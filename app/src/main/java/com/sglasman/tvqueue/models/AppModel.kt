package com.sglasman.tvqueue.models

import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.confirmation.ConfirmationModel
import com.sglasman.tvqueue.models.queue.QueueModel
import com.sglasman.tvqueue.models.search.SearchModel

data class AppModel(
    val currentScreen: Screen = Screen.QueueScreen,
    val apiToken: String? = null,
    val queueModel: QueueModel = QueueModel(),
    val searchModel: SearchModel = SearchModel(),
    val dialogScreen: DialogScreen = DialogScreen.NotShown,
    val addSeriesModel: AddSeriesModel = AddSeriesModel(),
    val confirmationModel: ConfirmationModel = ConfirmationModel()
)