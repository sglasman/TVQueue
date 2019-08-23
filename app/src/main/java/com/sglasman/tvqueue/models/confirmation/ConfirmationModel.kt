package com.sglasman.tvqueue.models.confirmation

import com.sglasman.tvqueue.AppAction

data class ConfirmationModel(val actionToConfirm: AppAction? = null,
                             val confirmationText: String = "")