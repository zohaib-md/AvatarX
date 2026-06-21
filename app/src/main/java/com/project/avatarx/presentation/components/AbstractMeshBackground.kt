package com.project.avatarx.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.project.avatarx.ui.theme.DeepNavy
import com.project.avatarx.ui.theme.ElectricBlue

@Composable
fun AbstractMeshBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Orb 1: Orange-Red moving top-left to center-right
        val cx1 = width * (0.2f + 0.4f * offset1)
        val cy1 = height * (0.15f + 0.25f * offset1)
        val radius1 = width * 0.85f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.12f), ElectricBlue.copy(alpha = 0f)),
                center = Offset(cx1, cy1),
                radius = radius1
            ),
            center = Offset(cx1, cy1),
            radius = radius1
        )

        // Orb 2: Deep Maroon moving bottom-right to center-left
        val cx2 = width * (0.8f - 0.4f * offset2)
        val cy2 = height * (0.85f - 0.25f * offset2)
        val radius2 = width * 0.9f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(DeepNavy.copy(alpha = 0.18f), DeepNavy.copy(alpha = 0f)),
                center = Offset(cx2, cy2),
                radius = radius2
            ),
            center = Offset(cx2, cy2),
            radius = radius2
        )
    }
}
