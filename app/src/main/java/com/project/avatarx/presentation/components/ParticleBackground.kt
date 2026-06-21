package com.project.avatarx.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.project.avatarx.ui.theme.ElectricBlue
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val speedX: Float,
    val speedY: Float
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
    particleColor: Color = ElectricBlue
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 2f,
                alpha = Random.nextFloat() * 0.3f + 0.1f,
                speedX = (Random.nextFloat() - 0.5f) * 0.001f,
                speedY = (Random.nextFloat() - 0.5f) * 0.001f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Use time to offset position, wrap around using modulo
            val offsetX = (p.x * width + p.speedX * time * width) % width
            val offsetY = (p.y * height + p.speedY * time * height) % height

            // Ensure positive coordinates after modulo
            val finalX = if (offsetX < 0) offsetX + width else offsetX
            val finalY = if (offsetY < 0) offsetY + height else offsetY

            // Draw particle glow
            drawCircle(
                color = particleColor.copy(alpha = p.alpha * 0.5f),
                radius = p.radius * 2f,
                center = androidx.compose.ui.geometry.Offset(finalX, finalY)
            )

            // Draw core
            drawCircle(
                color = particleColor.copy(alpha = p.alpha),
                radius = p.radius,
                center = androidx.compose.ui.geometry.Offset(finalX, finalY)
            )
        }
    }
}
