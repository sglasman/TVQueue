package com.sglasman.tvqueue.models.storage

import android.content.Context
import android.content.Context.MODE_PRIVATE

interface Storage {

    fun saveString(key: String, value: String)

    fun retrieveString(key: String): String?
}

class SharedPrefsStorage(private val context: Context): Storage {

    override fun saveString(key: String, value: String) {
        context.getSharedPreferences(TVQ_STORAGE, MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    override fun retrieveString(key: String): String? =
        context.getSharedPreferences(TVQ_STORAGE, MODE_PRIVATE).getString(key, null)

    companion object {
        private const val TVQ_STORAGE = "TVQ_STORAGE"
    }
}