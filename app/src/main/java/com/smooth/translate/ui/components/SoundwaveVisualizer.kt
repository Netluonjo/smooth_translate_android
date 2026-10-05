package com.smooth.translate.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smooth.translate.ui.theme.YellowDark

@Composable
fun SoundwaveVisualizer(
    isListening: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 21,
    barColor: Color = YellowDark.copy(alpha = 0.8f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveHeight"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pattern = listOf(
            0.3f, 0.4f, 0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 1.0f, 0.6f, 0.8f,
            0.4f, 0.9f, 0.7f, 0.5f, 0.8f, 0.6f, 0.4f, 0.7f, 0.5f, 0.3f, 0.2f
        )

        for (i in 0 until barCount) {
            val baseHeight = pattern[i % pattern.size]
            val finalHeight = if (isListening) {
                // Dynamic oscillating height
                val factor = ((i * 13) % 10) / 10f
                val mult = (baseHeight * (0.4f + 0.6f * (animProgress * factor + 0.3f))).coerceIn(0.15f, 1f)
                (mult * 44f).dp
            } else {
                (baseHeight * 0.35f * 44f).dp
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(finalHeight)
                    .background(barColor, RoundedCornerShape(2.dp))
            )
            if (i < barCount - 1) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(3.dp))
            }
        }
    }
}
