package com.sglasman.tvqueue.models

import com.sglasman.tvqueue.models.addseries.AddSeriesModel
import com.sglasman.tvqueue.models.search.SearchModel

data class AppModel(
    val currentScreen: Screen = Screen.SearchScreen,
    val apiToken: String? = null,
    val searchModel: SearchModel = SearchModel(),
    val dialogMode: DialogMode = DialogMode.NotShown,
    val addSeriesModel: AddSeriesModel = AddSeriesModel()
)