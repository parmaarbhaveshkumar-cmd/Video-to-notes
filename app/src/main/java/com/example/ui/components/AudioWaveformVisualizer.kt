package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseHeight"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 28
        val displayList = if (amplitudes.isNotEmpty()) {
            val padded = amplitudes.takeLast(barCount)
            if (padded.size < barCount) {
                List(barCount - padded.size) { 0.1f } + padded
            } else {
                padded
            }
        } else {
            List(barCount) { idx ->
                if (isRecording) ((idx % 5 + 1) * 0.15f * pulseAnim) else 0.1f
            }
        }

        displayList.forEachIndexed { index, amp ->
            val heightFraction = if (isRecording) {
                amp.coerceIn(0.12f, 0.95f)
            } else {
                0.12f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((64 * heightFraction).dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isRecording) {
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
