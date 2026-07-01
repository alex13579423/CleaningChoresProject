package com.example.myapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.myapp.R
import com.example.myapp.ui.theme.*

@Composable
fun WorkloadGraph(stats: Map<String, Float>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (stats.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.stats_no_data), color = MaterialTheme.colorScheme.outline)
            }
        } else {
            stats.entries.sortedByDescending { it.value }.forEach { (name, percent) ->
                WorkloadBar(name, percent)
            }
        }
    }
}

@Composable
fun WorkloadBar(name: String, percent: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 1000)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        when {
                            percent > 0.4f -> PriorityHigh
                            percent > 0.25f -> PriorityMedium
                            else -> PriorityLow
                        }
                    )
            )
        }
    }
}
