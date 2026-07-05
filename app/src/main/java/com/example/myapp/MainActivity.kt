package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapp.data.ChoreRepository
import com.example.myapp.data.Person
import com.example.myapp.ui.screens.ChoreFormBottomSheet
import com.example.myapp.ui.screens.PersonFormBottomSheet
import com.example.myapp.ui.screens.ScheduleScreen
import com.example.myapp.ui.screens.SettingsScreen
import com.example.myapp.ui.screens.StatsScreen
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
    val chores by viewModel.chores
    val priorityEnabled by viewModel.priorityEnabled
    val schedule by viewModel.schedule
    
    var showPersonForm by remember { mutableStateOf(false) }
    var showChoreForm by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val fabRotation by animateFloatAsState(
        targetValue = if (isFabExpanded) 135f else 0f,
        label = "fabRotation"
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0 
                        isFabExpanded = false
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_schedule)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1 
                        isFabExpanded = false
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_stats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2 
                        isFabExpanded = false
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isFabExpanded) {
                        SmallFloatingActionButton(
                            onClick = { 
                                editingPerson = null
                                showPersonForm = true
                                isFabExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.add_person_fab))
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = { 
                                showChoreForm = true
                                isFabExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.add_chore_fab))
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add, 
                            contentDescription = null,
                            modifier = Modifier.rotate(fabRotation)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding).fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> ScheduleScreen(
                    schedule = schedule,
                    people = people,
                    chores = chores,
                    priorityEnabled = priorityEnabled,
                    onGenerate = { viewModel.generateSchedule() },
                    onShare = { viewModel.shareSchedule() },
                    onUpdateSchedule = { viewModel.updateSchedule(it) }
                )
                1 -> StatsScreen(
                    workloadStats = viewModel.getWorkloadStats()
                )
                2 -> SettingsScreen(
                    people = people,
                    chores = chores,
                    onUpdatePerson = { viewModel.updatePerson(it) },
                    onDeletePerson = { viewModel.deletePerson(it) },
                    onEditPerson = { 
                        editingPerson = it
                        showPersonForm = true
                        isFabExpanded = false
                    },
                    onUpdateChore = { viewModel.updateChore(it) },
                    onDeleteChore = { viewModel.deleteChore(it) },
                    priorityEnabled = priorityEnabled,
                    onTogglePriority = { viewModel.togglePriorityEnabled(it) },
                    onAddChoreClick = { 
                        showChoreForm = true
                        isFabExpanded = false
                    },
                    onAddPersonClick = {
                        editingPerson = null
                        showPersonForm = true
                        isFabExpanded = false
                    },
                    onGenerate = {
                        viewModel.generateSchedule()
                        selectedTab = 0
                        isFabExpanded = false
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

        if (showChoreForm) {
            ChoreFormBottomSheet(
                onSave = { label, priority ->
                    viewModel.addCustomChore(label, priority)
                    showChoreForm = false
                },
                onDismiss = { showChoreForm = false }
            )
        }
    }
}
