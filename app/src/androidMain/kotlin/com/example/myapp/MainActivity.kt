package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapp.data.AndroidSharer
import com.example.myapp.data.AndroidStorage
import com.example.myapp.data.ChoreRepository
import com.example.myapp.ui.theme.CleaningChoresTheme
import com.example.myapp.viewmodel.ChoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val storage = AndroidStorage(applicationContext)
        val sharer = AndroidSharer(applicationContext)
        val repository = ChoreRepository(storage, sharer)
        val viewModel = ChoreViewModel(repository)
        
        setContent {
            CleaningChoresTheme {
                MainApp(viewModel)
            }
        }
    }
}
