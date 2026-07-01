package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapp.data.ChoreRepository
import com.example.myapp.data.Person
import com.example.myapp.ui.screens.PersonFormBottomSheet
import com.example.myapp.ui.screens.ScheduleScreen
import com.example.myapp.ui.screens.SettingsScreen
import com.example.myapp.ui.theme.CleaningChoresTheme
import com.example.myapp.viewmodel.ChoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = ChoreRepository(applicationContext)
        val viewModel = ChoreViewModel(repository)
        
        setContent {
            CleaningChoresTheme {
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ChoreViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val people by viewModel.people
    val schedule by viewModel.schedule
    
    var showPersonForm by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_schedule)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { 
                        editingPerson = null
                        showPersonForm = true 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding).fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (selectedTab == 0) {
                ScheduleScreen(
                    schedule = schedule,
                    people = people,
                    onGenerate = { viewModel.generateSchedule() },
                    onShare = { viewModel.shareSchedule() },
                    onUpdateSchedule = { viewModel.updateSchedule(it) }
                )
            } else {
                SettingsScreen(
                    people = people,
                    onUpdatePerson = { viewModel.updatePerson(it) },
                    onDeletePerson = { viewModel.deletePerson(it) },
                    onEditPerson = { 
                        editingPerson = it
                        showPersonForm = true
                    },
                    onGenerate = {
                        viewModel.generateSchedule()
                        selectedTab = 0
                    }
                )
            }
        }

        if (showPersonForm) {
            PersonFormBottomSheet(
                person = editingPerson,
                onSave = { name, gender, days ->
                    if (editingPerson != null) {
                        viewModel.updatePerson(editingPerson!!.copy(name = name, gender = gender, unavailableDays = days))
                    } else {
                        viewModel.addPerson(name, gender, days)
                    }
                    showPersonForm = false
                },
                onDismiss = { showPersonForm = false }
            )
        }
    }
}
