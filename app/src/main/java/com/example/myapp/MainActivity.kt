package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapp.data.ChoreRepository
import com.example.myapp.data.UserRole
import com.example.myapp.ui.screens.ChoreFormBottomSheet
import com.example.myapp.ui.screens.LoginScreen
import com.example.myapp.ui.screens.PersonFormBottomSheet
import com.example.myapp.ui.screens.ScheduleScreen
import com.example.myapp.ui.screens.SettingsScreen
import com.example.myapp.ui.screens.StatsScreen
import com.example.myapp.ui.theme.CleaningChoresTheme
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.viewmodel.ChoreViewModel
import com.example.myapp.viewmodel.MainViewModel
import com.example.myapp.viewmodel.ViewModelFactory
import com.example.myapp.ui.components.NfcSharingDialog
import com.example.myapp.ui.components.NfcScannerDialog
import com.example.myapp.ui.components.QrSharingDialog
import com.example.myapp.ui.components.QrScannerDialog
import com.example.myapp.qr.data.manager.QrCryptoManager
import com.example.myapp.qr.data.manager.QrImageAnalyzer
import com.example.myapp.qr.presentation.util.QrGenerator
import android.content.Intent
import android.provider.Settings
import android.nfc.NfcAdapter
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = ChoreRepository.getInstance(applicationContext)
        val factory = ViewModelFactory(repository)
        val choreViewModel = ViewModelProvider(this, factory)[ChoreViewModel::class.java]
        val mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        
        val qrCryptoManager = QrCryptoManager()
        val qrImageAnalyzer = QrImageAnalyzer()
        val qrGenerator = QrGenerator()
        
        setContent {
            val isDarkMode by choreViewModel.isDarkMode
            CleaningChoresTheme(darkTheme = isDarkMode) {
                val userRole by choreViewModel.userRole
                if (userRole == null) {
                    LoginScreen(onRoleSelected = { choreViewModel.setUserRole(it) })
                } else {
                    MainApp(choreViewModel, mainViewModel, qrCryptoManager, qrImageAnalyzer, qrGenerator)
                }
            }
        }
    }

    fun checkNfcEnabled(): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    fun openNfcSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun MainApp(
    viewModel: ChoreViewModel,
    mainViewModel: MainViewModel,
    qrCryptoManager: QrCryptoManager,
    qrImageAnalyzer: QrImageAnalyzer,
    qrGenerator: QrGenerator
) {
    val selectedTab by mainViewModel.selectedTab
    val people by viewModel.people
    val chores by viewModel.chores
    val priorityEnabled by viewModel.priorityEnabled
    val schedule by viewModel.schedule
    val userRole by viewModel.userRole
    val isDarkMode by viewModel.isDarkMode
    val isManager = userRole == UserRole.MANAGER
    
    val showPersonForm by mainViewModel.showPersonForm
    val showChoreForm by mainViewModel.showChoreForm
    val editingPerson by mainViewModel.editingPerson
    val isFabExpanded by mainViewModel.isFabExpanded

    val showNfcSharing by mainViewModel.showNfcSharing
    val showNfcScanner by mainViewModel.showNfcScanner
    val showQrSharing by mainViewModel.showQrSharing
    val showQrScanner by mainViewModel.showQrScanner

    val context = LocalContext.current as MainActivity

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
                    onClick = { mainViewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_schedule)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { mainViewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_stats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { mainViewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 2 && isManager,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isFabExpanded) {
                        SmallFloatingActionButton(
                            onClick = { mainViewModel.setShowPersonForm(true, null) },
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
                            onClick = { mainViewModel.setShowChoreForm(true) },
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
                        onClick = { mainViewModel.setFabExpanded(!isFabExpanded) },
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
                    isManager = isManager,
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
                    onEditPerson = { mainViewModel.setShowPersonForm(true, it) },
                    onUpdateChore = { viewModel.updateChore(it) },
                    onDeleteChore = { viewModel.deleteChore(it) },
                    priorityEnabled = priorityEnabled,
                    onTogglePriority = { viewModel.togglePriorityEnabled(it) },
                    onAddChoreClick = { mainViewModel.setShowChoreForm(true) },
                    onAddPersonClick = { mainViewModel.setShowPersonForm(true, null) },
                    onGenerate = {
                        viewModel.generateSchedule()
                        mainViewModel.setSelectedTab(0)
                    },
                    isManager = isManager,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onLogout = { viewModel.setUserRole(null) },
                    onScanQr = { mainViewModel.setShowQrScanner(true) },
                    onGenerateQr = { mainViewModel.setShowQrSharing(true) },
                    onScanNfc = {
                        if (context.checkNfcEnabled()) {
                            mainViewModel.setShowNfcScanner(true)
                        } else {
                            context.openNfcSettings()
                        }
                    },
                    onGenerateNfc = {
                        if (context.checkNfcEnabled()) {
                            mainViewModel.setShowNfcSharing(true)
                        } else {
                            context.openNfcSettings()
                        }
                    }
                )
            }
        }

        if (showNfcSharing) {
            NfcSharingDialog(
                onDismiss = { mainViewModel.setShowNfcSharing(false) }
            )
        }

        if (showNfcScanner) {
            NfcScannerDialog(
                onResult = { json ->
                    viewModel.applySyncJson(json)
                    mainViewModel.setShowNfcScanner(false)
                    Toast.makeText(context, context.getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                },
                onDismiss = { mainViewModel.setShowNfcScanner(false) }
            )
        }

        if (showQrSharing) {
            QrSharingDialog(
                data = viewModel.getSyncJson(),
                onDismiss = { mainViewModel.setShowQrSharing(false) },
                qrCryptoManager = qrCryptoManager,
                qrGenerator = qrGenerator
            )
        }

        if (showQrScanner) {
            QrScannerDialog(
                onResult = { json ->
                    viewModel.applySyncJson(json)
                    mainViewModel.setShowQrScanner(false)
                    Toast.makeText(context, context.getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                },
                onDismiss = { mainViewModel.setShowQrScanner(false) },
                qrImageAnalyzer = qrImageAnalyzer,
                qrCryptoManager = qrCryptoManager
            )
        }

        if (showPersonForm) {
            PersonFormBottomSheet(
                person = editingPerson,
                onSave = { name, gender, days ->
                    editingPerson?.let { person ->
                        viewModel.updatePerson(person.copy(name = name, gender = gender, unavailableDays = days))
                    } ?: run {
                        viewModel.addPerson(name, gender, days)
                    }
                    mainViewModel.setShowPersonForm(false)
                },
                onDismiss = { mainViewModel.setShowPersonForm(false) }
            )
        }

        if (showChoreForm) {
            ChoreFormBottomSheet(
                onSave = { label, priority ->
                    viewModel.addCustomChore(label, priority)
                    mainViewModel.setShowChoreForm(false)
                },
                onDismiss = { mainViewModel.setShowChoreForm(false) }
            )
        }
    }
}
