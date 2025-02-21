package com.plcoding.androidinternals

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCircle(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val color by transition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Green,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    // View: Measurement -> Layout -> Drawing
    // Compose: Composition -> (Measurement + Layout) -> Drawing
    Box(
        modifier = modifier
            .drawBehind {
                drawOval(
                    color = color,
                    topLeft = Offset(
                        x = size.width * 0.25f,
                        y = size.height * 0.25f
                    ),
                    size = Size(
                        width = size.width * 0.5f,
                        height = size.height * 0.5f
                    )
                )
            }
    )
}