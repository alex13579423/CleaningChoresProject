package com.example.myapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.R
import com.example.myapp.data.*
import com.example.myapp.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChoreItem(
    chore: Chore,
    assignedNames: List<String>,
    isEditing: Boolean,
    activePeople: List<Person>,
    priorityEnabled: Boolean = true,
    onTogglePerson: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chore.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (priorityEnabled) {
                        val currentPriority = chore.priority ?: Priority.MEDIUM
                        val (priorityTextRes, priorityColor) = when(currentPriority) {
                            Priority.HIGH -> R.string.priority_hard to PriorityHigh
                            Priority.MEDIUM -> R.string.priority_medium to PriorityMedium
                            Priority.LOW -> R.string.priority_easy to PriorityLow
                        }
                        
                        Surface(
                            color = priorityColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(priorityTextRes),
                                color = priorityColor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!isEditing) {
                    val currentPriority = chore.priority ?: Priority.MEDIUM
                    if (assignedNames.isEmpty()) {
                        val missingText = when {
                            chore.id == "toilet_m" -> stringResource(R.string.missing_male)
                            chore.id == "toilet_f" -> stringResource(R.string.missing_female)
                            currentPriority == Priority.LOW -> stringResource(R.string.not_required_today)
                            else -> stringResource(R.string.missing_personnel)
                        }
                        Text(
                            text = missingText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentPriority == Priority.LOW) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
                        ) {
                            assignedNames.forEach { name ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                                    shape = MaterialTheme.shapes.small
                                )
                            }
                        }
                    }
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                
                val pool = when(chore.genderConstraint) {
                    Gender.MALE -> activePeople.filter { it.gender == Gender.MALE }
                    Gender.FEMALE -> activePeople.filter { it.gender == Gender.FEMALE }
                    else -> activePeople
                }
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pool.forEach { person ->
                        val isSelected = person.name in assignedNames
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTogglePerson(person.name) },
                            label = { Text(person.name) },
                            shape = MaterialTheme.shapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
