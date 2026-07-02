package com.example.myapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.data.*
import com.example.myapp.ui.AppStrings
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
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
                        val (priorityText, priorityColor) = when(chore.priority) {
                            Priority.HIGH -> "קשה" to PriorityHigh
                            Priority.MEDIUM -> "בינוני" to PriorityMedium
                            Priority.LOW -> "קל" to PriorityLow
                        }
                        
                        Surface(
                            color = priorityColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = priorityText,
                                color = priorityColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (!isEditing) {
                    if (assignedNames.isEmpty()) {
                        val missingText = when {
                            chore.id == "toilet_m" -> "אין בן פנוי"
                            chore.id == "toilet_f" -> "אין בת פנויה"
                            chore.priority == Priority.LOW -> "לא נדרש היום"
                            else -> "חסר כוח אדם"
                        }
                        Text(
                            text = missingText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (chore.priority == Priority.LOW) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                        )
                    } else {
                        // Display assigned people as integrated badges
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
                        ) {
                            assignedNames.forEach { name ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(name, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(12.dp)
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
                            shape = RoundedCornerShape(12.dp),
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
