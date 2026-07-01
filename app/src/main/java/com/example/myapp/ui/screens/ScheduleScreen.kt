package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.R
import com.example.myapp.data.*
import com.example.myapp.ui.components.ChoreItem
import com.example.myapp.ui.components.DaySelector
import com.example.myapp.ui.theme.*

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

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            DaySelector(
                selectedDayKey = currentDayKey,
                onDaySelected = { 
                    currentDayKey = it
                    isEditing = false
                }
            )
        }

        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            if (schedule == null || schedule.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_schedule_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_schedule_subtitle),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.day_prefix, DAYS_HE[currentDayKey] ?: ""),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    TextButton(
                        onClick = { isEditing = !isEditing },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditing) stringResource(R.string.save_assignment) else stringResource(R.string.edit_assignment))
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.share_whatsapp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
