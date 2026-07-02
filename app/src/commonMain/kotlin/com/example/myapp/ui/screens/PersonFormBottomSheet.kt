package com.example.myapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.data.*
import com.example.myapp.ui.AppStrings
import com.example.myapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormBottomSheet(
    person: Person? = null,
    onSave: (String, Gender, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(person?.name ?: "") }
    var gender by remember { mutableStateOf(person?.gender ?: Gender.MALE) }
    var unavailableDays by remember { mutableStateOf(person?.unavailableDays ?: emptyList<String>()) }

    ModalBottomSheet(
        onDismissRequest = {
            if (name.isNotBlank()) onSave(name, gender, unavailableDays)
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (person == null) AppStrings.add_person_title else AppStrings.edit_person_title,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(AppStrings.person_name_hint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Text(AppStrings.gender_label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GenderButton(
                    label = AppStrings.gender_male,
                    isSelected = gender == Gender.MALE,
                    onClick = { gender = Gender.MALE },
                    color = MaleColor,
                    modifier = Modifier.weight(1f)
                )
                GenderButton(
                    label = AppStrings.gender_female,
                    isSelected = gender == Gender.FEMALE,
                    onClick = { gender = Gender.FEMALE },
                    color = FemaleColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(AppStrings.availability_hint, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DAY_KEYS.forEach { dayKey ->
                        val isSelected = dayKey in unavailableDays
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                unavailableDays = if (isSelected) unavailableDays - dayKey else unavailableDays + dayKey
                            },
                            label = { Text(DAYS_HE_SHORT[dayKey] ?: "", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = UnavailableBackground,
                                selectedLabelColor = UnavailableText
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { if (name.isNotBlank()) onSave(name, gender, unavailableDays) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(AppStrings.save_button, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun GenderButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
