package com.example.myapp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.example.myapp.data.ChoreRepository
import com.example.myapp.data.WebSharer
import com.example.myapp.data.WebStorage
import com.example.myapp.viewmodel.ChoreViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val storage = WebStorage()
    val sharer = WebSharer()
    val repository = ChoreRepository(storage, sharer)
    val viewModel = ChoreViewModel(repository)

    CanvasBasedWindow(title = "Cleaning Chores", canvasElementId = "app") {
        MainApp(viewModel)
    }
}
