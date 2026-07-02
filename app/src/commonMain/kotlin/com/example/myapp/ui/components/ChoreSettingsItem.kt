package com.example.myapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.data.*
import com.example.myapp.ui.AppStrings
import com.example.myapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreSettingsItem(
    chore: Chore,
    onUpdate: (Chore) -> Unit,
    onDelete: (Chore) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chore.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chore.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = if (chore.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (chore.id.startsWith("custom_")) {
                    IconButton(onClick = { onDelete(chore) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Switch(
                    checked = chore.isActive,
                    onCheckedChange = { onUpdate(chore.copy(isActive = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            if (chore.isActive) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppStrings.priority_label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Priority.entries.forEach { p ->
                        val isSelected = chore.priority == p
                        val color = when(p) {
                            Priority.LOW -> PriorityLow
                            Priority.MEDIUM -> PriorityMedium
                            Priority.HIGH -> PriorityHigh
                        }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdate(chore.copy(priority = p)) },
                            label = { 
                                Text(
                                    text = when(p) {
                                        Priority.LOW -> "קל"
                                        Priority.MEDIUM -> "בינוני"
                                        Priority.HIGH -> "קשה"
                                    },
                                    fontSize = 10.sp
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = color,
                                selectedLeadingIconColor = color
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        }
    }
}
