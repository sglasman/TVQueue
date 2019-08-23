package com.sglasman.tvqueue.models

import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.confirmation.ConfirmationModel
import com.sglasman.tvqueue.models.search.SearchModel

data class AppModel(
    val currentScreen: Screen = Screen.SearchScreen,
    val apiToken: String? = null,
    val searchModel: SearchModel = SearchModel(),
    val dialogScreen: DialogScreen = DialogScreen.NotShown,
    val addSeriesModel: AddSeriesModel = AddSeriesModel(),
    val confirmationModel: ConfirmationModel = ConfirmationModel()
)