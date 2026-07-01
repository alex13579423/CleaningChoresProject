package com.example.myapp.data

// Note: This file is intended for the Kotlin/Wasm target
// It uses Browser APIs to implement Storage and Sharing

/*
class WebStorage : Storage {
    override fun getString(key: String): String? = 
        kotlinx.browser.window.localStorage.getItem(key)

    override fun saveString(key: String, value: String) {
        kotlinx.browser.window.localStorage.setItem(key, value)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        kotlinx.browser.window.localStorage.getItem(key)?.toBoolean() ?: default

    override fun saveBoolean(key: String, value: Boolean) {
        kotlinx.browser.window.localStorage.setItem(key, value.toString())
    }
}

class WebSharer : Sharer {
    override fun shareSchedule(text: String) {
        // Use the Web Share API if available, otherwise fallback to WhatsApp link
        val navigator = kotlinx.browser.window.navigator
        if (navigator.asDynamic().share != null) {
            navigator.asDynamic().share(js("{text: text}"))
        } else {
            val encodedText = kotlinx.browser.window.encodeURIComponent(text)
            kotlinx.browser.window.open("https://wa.me/?text=$encodedText", "_blank")
        }
    }
}
*/
