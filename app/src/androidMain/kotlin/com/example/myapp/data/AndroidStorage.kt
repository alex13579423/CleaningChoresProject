package com.example.myapp.data

import android.content.Context
import androidx.core.content.edit

class AndroidStorage(context: Context) : Storage {
    private val prefs = context.getSharedPreferences("duty_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun saveString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun saveBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }
}
