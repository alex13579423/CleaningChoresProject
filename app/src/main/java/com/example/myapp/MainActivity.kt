package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.data.*

class MainActivity : ComponentActivity() {
    private lateinit var choreManager: ChoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        choreManager = ChoreManager(this)
        
        setContent {
            MaterialTheme {
                MainScreen(choreManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(manager: ChoreManager) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var people by remember { mutableStateOf(manager.getPeople()) }
    var schedule by remember { mutableStateOf(manager.getSchedule()) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("לוח תורנויות 📅", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("לוח") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("הגדרות") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                ScheduleScreen(
                    schedule = schedule,
                    people = people,
                    onGenerate = {
                        val newSchedule = manager.generateWeek(people)
                        schedule = newSchedule
                        manager.saveSchedule(newSchedule)
                    },
                    onShare = {
                        schedule?.let { manager.shareSchedule(it) }
                    },
                    onUpdateSchedule = { updated ->
                        schedule = updated
                        manager.saveSchedule(updated)
                    }
                )
            } else {
                SettingsScreen(
                    people = people,
                    onUpdatePeople = { updated ->
                        people = updated
                        manager.savePeople(updated)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    schedule: Map<String, Map<String, List<String>>>?,
    people: List<Person>,
    onGenerate: () -> Unit,
    onShare: () -> Unit,
    onUpdateSchedule: (Map<String, Map<String, List<String>>>) -> Unit
) {
    var currentDayKey by remember { mutableStateOf("sun") }
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Day Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DAY_KEYS.forEach { dayKey ->
                FilterChip(
                    selected = currentDayKey == dayKey,
                    onClick = { 
                        currentDayKey = dayKey 
                        isEditing = false
                    },
                    label = { Text(DAYS_HE[dayKey] ?: "") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (schedule == null || schedule.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("אין סידור כרגע. הגרל שבוע חדש בהגדרות.")
            }
        } else {
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "יום ${DAYS_HE[currentDayKey]}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF2980B9),
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { isEditing = !isEditing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditing) Color(0xFF27AE60) else Color(0xFFE74C3C)
                            )
                        ) {
                            Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (isEditing) "שמור" else "ערוך")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(CHORES) { chore ->
                            ChoreItem(
                                chore = chore,
                                assignedNames = schedule[currentDayKey]?.get(chore.id) ?: emptyList(),
                                isEditing = isEditing,
                                activePeople = people.filter { it.active },
                                onTogglePerson = { name ->
                                    val newDaySchedule = schedule[currentDayKey]?.toMutableMap() ?: mutableMapOf()
                                    val list = newDaySchedule[chore.id]?.toMutableList() ?: mutableListOf()
                                    if (name in list) list.remove(name) else list.add(name)
                                    newDaySchedule[chore.id] = list
                                    
                                    val newSchedule = schedule.toMutableMap()
                                    newSchedule[currentDayKey] = newDaySchedule
                                    onUpdateSchedule(newSchedule)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("שתף סידור שבועי")
        }
    }
}

@Composable
fun ChoreItem(
    chore: Chore,
    assignedNames: List<String>,
    isEditing: Boolean,
    activePeople: List<Person>,
    onTogglePerson: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chore.label, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                val priorityColor = when(chore.priority) {
                    Priority.HIGH -> Color(0xFFE74C3C)
                    Priority.MEDIUM -> Color(0xFFF39C12)
                    Priority.LOW -> Color(0xFF27AE60)
                }
                val priorityLabel = when(chore.priority) {
                    Priority.HIGH -> "(קשה)"
                    Priority.MEDIUM -> "(בינוני)"
                    Priority.LOW -> "(קל)"
                }
                Text(priorityLabel, color = priorityColor, fontSize = 12.sp)
            }
            
            if (!isEditing) {
                if (assignedNames.isEmpty()) {
                    val missingText = when {
                        chore.id == "toilet_m" -> "אין בן פנוי"
                        chore.id == "toilet_f" -> "אין בת פנויה"
                        chore.priority == Priority.LOW -> "לא נדרש היום"
                        else -> "חסר כוח אדם"
                    }
                    val missingColor = if (chore.priority == Priority.LOW) Color.Gray else Color.Red
                    Text(missingText, color = missingColor, fontSize = 14.sp)
                } else {
                    Text(assignedNames.joinToString(", "), color = Color(0xFF27AE60), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isEditing) {
            Spacer(Modifier.height(8.dp))
            val pool = when(chore.genderConstraint) {
                Gender.MALE -> activePeople.filter { it.gender == Gender.MALE }
                Gender.FEMALE -> activePeople.filter { it.gender == Gender.FEMALE }
                else -> activePeople
            }
            
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                pool.forEach { person ->
                    val isSelected = person.name in assignedNames
                    Surface(
                        modifier = Modifier.clickable { onTogglePerson(person.name) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF27AE60) else Color(0xFFEEEEEE),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(
                            person.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isSelected) Color.White else Color.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}

@Composable
fun SettingsScreen(
    people: List<Person>,
    onUpdatePeople: (List<Person>) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newGender by remember { mutableStateOf(Gender.MALE) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("הוספת משתתף", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            placeholder = { Text("שם") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = newGender == Gender.MALE, onClick = { newGender = Gender.MALE })
                                Text("בן", fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = newGender == Gender.FEMALE, onClick = { newGender = Gender.FEMALE })
                                Text("בת", fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = {
                            if (newName.isNotBlank()) {
                                val newList = people + Person(System.currentTimeMillis(), newName, newGender)
                                onUpdatePeople(newList)
                                newName = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF27AE60))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* This is handled by a refresh in parent state via onGenerate call usually */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // UI only button to match original layout but we generate in schedule tab
            ) {
                Text("🎲 הגרל סידור (בצע בלשונית הלוח)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("רשימת משתתפים", fontWeight = FontWeight.Bold)
        }

        items(people) { person ->
            PersonItem(
                person = person,
                onUpdate = { updated ->
                    val newList = people.map { if (it.id == updated.id) updated else it }
                    onUpdatePeople(newList)
                },
                onDelete = {
                    val newList = people.filter { it.id != person.id }
                    onUpdatePeople(newList)
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun PersonItem(
    person: Person,
    onUpdate: (Person) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (person.active) Color.White else Color(0xFFF9F9F9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = person.active, onCheckedChange = { onUpdate(person.copy(active = it)) })
                Text(
                    person.name,
                    fontWeight = FontWeight.Bold,
                    color = if (person.active) Color.Black else Color.Gray,
                    textDecoration = if (person.active) null else androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
                Spacer(Modifier.width(8.dp))
                val genderText = if (person.gender == Gender.MALE) "(בן ♂️)" else "(בת ♀️)"
                val genderColor = if (person.gender == Gender.MALE) Color(0xFF2980B9) else Color(0xFF8E44AD)
                Text(genderText, color = genderColor, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE74C3C))
                }
            }
            
            Text("ימי היעדרות (לחץ לסימון):", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 12.dp))
            Row(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                DAY_KEYS.forEach { dayKey ->
                    val isUnavailable = dayKey in person.unavailableDays
                    Surface(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                val newList = if (isUnavailable) {
                                    person.unavailableDays - dayKey
                                } else {
                                    person.unavailableDays + dayKey
                                }
                                onUpdate(person.copy(unavailableDays = newList))
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUnavailable) Color(0xFFF8D7DA) else Color(0xFFEEEEEE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnavailable) Color(0xFFF5C6CB) else Color.LightGray)
                    ) {
                        Text(
                            (DAYS_HE[dayKey] ?: "").take(1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = if (isUnavailable) Color(0xFF721C24) else Color.DarkGray,
                            textDecoration = if (isUnavailable) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
    }
}
