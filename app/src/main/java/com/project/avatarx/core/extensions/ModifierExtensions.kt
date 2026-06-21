package com.project.avatarx.core.extensions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.avatarx.ui.theme.SlateGlass
import com.project.avatarx.ui.theme.SlateGlassBorder

/**
 * Applies a glassmorphism effect with a translucent background and subtle border.
 */
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier = this
    .clip(shape)
    .background(color = SlateGlass, shape = shape)
    .border(width = 1.dp, color = SlateGlassBorder, shape = shape)

/**
 * Draws a gradient border around the composable using the specified brush and shape.
 */
fun Modifier.gradientBorder(
    brush: Brush,
    shape: Shape,
    width: Dp = 1.dp
): Modifier = this.composed {
    val cornerRadius = when (shape) {
        is RoundedCornerShape -> 20f
        else -> 0f
    }
    this.drawBehind {
        val strokeWidth = width.toPx()
        drawRoundRect(
            brush = brush,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth)
        )
    }
}

/**
 * Creates a pulsing glow effect by animating the alpha of a radial gradient drawn behind
 * the composable.
 */
fun Modifier.pulsingGlow(
    color: Color,
    radius: Dp = 16.dp
): Modifier = this.composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGlow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingGlowAlpha"
    )
    this.drawBehind {
        val glowRadius = radius.toPx()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    color.copy(alpha = alpha * 0.5f),
                    color.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = maxOf(size.width, size.height) / 2f + glowRadius
            ),
            radius = maxOf(size.width, size.height) / 2f + glowRadius,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Applies a shimmer effect using an animated linear gradient that sweeps across the composable.
 */
fun Modifier.shimmer(): Modifier = this.composed {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.0f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslateX"
    )
    this.graphicsLayer { }
        .drawBehind {
            val shimmerBrush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateX, 0f),
                end = Offset(translateX + size.width, size.height)
            )
            drawRect(
                brush = shimmerBrush,
                size = size,
                blendMode = BlendMode.SrcAtop
            )
        }
}
