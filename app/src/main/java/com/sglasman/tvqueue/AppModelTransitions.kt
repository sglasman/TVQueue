package com.sglasman.tvqueue

import com.sglasman.tvqueue.models.AppModel

private suspend fun getTransition(model: AppModel, action: AppAction): AppModel = when (action) {
    is AppAction.Login -> if (model.apiToken != null) model
    else model.copy(

    )
}