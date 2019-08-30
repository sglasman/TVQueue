package com.sglasman.tvqueue.models

import java.text.SimpleDateFormat
import java.util.*

fun Date.roundToDay(): Date = SimpleDateFormat("yyyy-MM-dd", Locale.US).run {
    parse(format(this@roundToDay))
}!!

fun Date.addDays(days: Int): Date = Calendar.getInstance().apply {
    time = this@addDays
    add(Calendar.DAY_OF_MONTH, days)
}.time