package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.myapp.ui.components.ChoreSettingsItem
import com.example.myapp.ui.components.PersonItem
import com.example.myapp.ui.theme.*

@Composable
fun SettingsScreen(
    people: List<Person>,
    chores: List<Chore>,
    onUpdatePerson: (Person) -> Unit,
    onDeletePerson: (Person) -> Unit,
    onEditPerson: (Person) -> Unit,
    onUpdateChore: (Chore) -> Unit,
    onDeleteChore: (Chore) -> Unit,
    priorityEnabled: Boolean,
    onTogglePriority: (Boolean) -> Unit,
    onAddChoreClick: () -> Unit,
    onGenerate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.people_list_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.manage_people_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.lottery_button), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (people.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.empty_people_msg), color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            items(people) { person ->
                PersonItem(
                    person = person,
                    onUpdate = onUpdatePerson,
                    onDelete = { onDeletePerson(person) },
                    onClick = { onEditPerson(person) }
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.chores_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.chores_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Button(
                        onClick = onAddChoreClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_chore_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.use_priorities_label),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.use_priorities_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = priorityEnabled,
                            onCheckedChange = onTogglePriority
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            items(chores) { chore ->
                ChoreSettingsItem(
                    chore = chore,
                    onUpdate = onUpdateChore,
                    onDelete = onDeleteChore
                )
            }
        }
    }
}
