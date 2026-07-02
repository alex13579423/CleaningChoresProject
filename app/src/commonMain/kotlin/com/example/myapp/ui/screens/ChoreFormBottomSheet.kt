package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.data.Priority
import com.example.myapp.ui.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreFormBottomSheet(
    onSave: (String, Priority) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }

    ModalBottomSheet(
        onDismissRequest = {
            if (label.isNotBlank()) onSave(label, priority)
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                AppStrings.add_chore_title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(AppStrings.chore_name_hint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(AppStrings.priority_label, style = MaterialTheme.typography.labelLarge)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach { p ->
                    val pLabel = when(p) {
                        Priority.LOW -> "קל"
                        Priority.MEDIUM -> "בינוני"
                        Priority.HIGH -> "קשה"
                    }
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(pLabel) }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { if (label.isNotBlank()) onSave(label, priority) },
                modifier = Modifier.fillMaxWidth(),
                enabled = label.isNotBlank()
            ) {
                Text(AppStrings.save_chore)
            }
        }
    }
}
