package com.example.myapp.data

interface Sharer {
    fun shareSchedule(text: String)
}

// We'll keep the Android implementation for now to keep the Android app working
class AndroidSharer(private val context: android.content.Context) : Sharer {
    override fun shareSchedule(text: String) {
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Schedule")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
