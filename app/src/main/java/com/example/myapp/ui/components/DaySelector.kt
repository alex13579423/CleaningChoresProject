package com.example.myapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = { 
            // Default indicator is fine, but we use TabRow instead of ScrollableTabRow to show all days
        }
    ) {
        DAY_KEYS.forEachIndexed { index, dayKey ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onDaySelected(dayKey) },
                text = {
                    Text(
                        text = DAYS_HE[dayKey] ?: "",
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            )
        }
    }
}
