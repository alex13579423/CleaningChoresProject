package com.example.myapp.data

import kotlinx.browser.window

// External JS functions for browser features not available in standard stdlib
private fun encodeURIComponent(s: String): String = js("encodeURIComponent(s)")

class WebStorage : Storage {
    override fun getString(key: String): String? = 
        window.localStorage.getItem(key)

    override fun saveString(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        window.localStorage.getItem(key)?.toBoolean() ?: default

    override fun saveBoolean(key: String, value: Boolean) {
        window.localStorage.setItem(key, value.toString())
    }
}

class WebSharer : Sharer {
    override fun shareSchedule(text: String) {
        // Use standard browser open for WhatsApp fallback
        val encodedText = encodeURIComponent(text)
        window.open("https://wa.me/?text=$encodedText", "_blank")
    }
}
