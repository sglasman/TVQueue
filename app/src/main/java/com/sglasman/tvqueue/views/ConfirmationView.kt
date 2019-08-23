package com.sglasman.tvqueue.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sglasman.tvqueue.R
import com.sglasman.tvqueue.models.confirmation.ConfirmationModel
import kotlinx.android.synthetic.main.confirmation_view.view.*

class ConfirmationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.confirmation_view, this, true)
    }

    fun update(model: ConfirmationModel) {
        confirmation_text.text = model.confirmationText
    }
}