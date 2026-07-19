package com.example.myapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.data.DAYS_HE
import com.example.myapp.data.DAY_KEYS

@Composable
fun DaySelector(
    selectedDayKey: String,
    onDaySelected: (String) -> Unit
) {
    val selectedIndex = DAY_KEYS.indexOf(selectedDayKey)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DAY_KEYS.forEachIndexed { index, dayKey ->
                    val isSelected = selectedIndex == index
                    
                    if (index > 0) {
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                else Color.Transparent
                            )
                            .clickable { onDaySelected(dayKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = DAYS_HE[dayKey] ?: "",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
