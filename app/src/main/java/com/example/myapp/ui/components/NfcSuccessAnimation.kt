package com.example.myapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun NfcSuccessAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50)
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Canvas(modifier = modifier.size(100.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.7f)
            lineTo(size.width * 0.8f, size.height * 0.3f)
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        
        val segmentPath = Path()
        pathMeasure.getSegment(
            startDistance = 0f,
            stopDistance = progress.value * pathMeasure.length,
            destination = segmentPath
        )

        drawPath(
            path = segmentPath,
            color = color,
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round
            )
        )
    }
}
