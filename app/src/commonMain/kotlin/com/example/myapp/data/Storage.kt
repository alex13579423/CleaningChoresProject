package com.example.myapp.data

interface Storage {
    fun getString(key: String): String?
    fun saveString(key: String, value: String)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun saveBoolean(key: String, value: Boolean)
}
