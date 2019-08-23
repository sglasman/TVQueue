package com.sglasman.tvqueue.views

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun View.setSuspendingOnClickListener(block: suspend (View) -> Unit) = setOnClickListener {
    CoroutineScope(Dispatchers.Main).launch { block(this@setSuspendingOnClickListener) }
}

fun Boolean.toVisibility(): Int = if (this) VISIBLE else GONE

fun ViewGroup.addViewIdempotent(child: View) {
    if ((child.parent as? ViewGroup) != this) {
        (child.parent as? ViewGroup)?.removeView(child)
        addView(child)
    }
}

fun ViewGroup.getChildren(): List<View> = (0 until childCount).map { getChildAt(it) }