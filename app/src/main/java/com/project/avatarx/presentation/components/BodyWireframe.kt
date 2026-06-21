package com.project.avatarx.presentation.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.ElectricBlue

@Composable
fun BodyWireframe(
    modifier: Modifier = Modifier,
    glowColor: Color = ElectricBlue,
    shoulderWidth: Float = 46f,
    hipWidth: Float = 42f,
    heightCm: Float = 174f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = CloudGray, fontSize = 11.sp)

    // Pre-measure text layouts outside of Canvas so drawText uses TextLayoutResult overload
    val shoulderTextLayout = textMeasurer.measure("${shoulderWidth.toInt()} cm", labelStyle)
    val hipTextLayout = textMeasurer.measure("${hipWidth.toInt()} cm", labelStyle)
    val heightTextLayout = textMeasurer.measure("${heightCm.toInt()} cm", labelStyle)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Skip drawing if canvas has no valid size yet
        if (width <= 0f || height <= 0f) return@Canvas
        
        // Define base points based on proportions
        val centerX = width / 2f
        val topY = height * 0.1f
        val bottomY = height * 0.9f
        
        val headRadius = width * 0.12f
        val headCenterY = topY + headRadius
        
        val neckY = headCenterY + headRadius
        val shoulderY = neckY + height * 0.05f
        
        // Scale proportions (arbitrary mapping for visual representation)
        val shoulderCanvasWidth = width * (shoulderWidth / 60f)
        val hipCanvasWidth = width * (hipWidth / 60f)
        
        val shoulderLeft = centerX - shoulderCanvasWidth / 2f
        val shoulderRight = centerX + shoulderCanvasWidth / 2f
        
        val hipY = shoulderY + height * 0.35f
        val hipLeft = centerX - hipCanvasWidth / 2f
        val hipRight = centerX + hipCanvasWidth / 2f

        val kneeY = hipY + height * 0.2f
        
        scale(scalePulse, pivot = Offset(centerX, height / 2f)) {
            // Helper function to draw wireframe
            fun drawSkeleton(strokeWidth: Float, alpha: Float) {
                val color = glowColor.copy(alpha = alpha)
                val stroke = Stroke(width = strokeWidth)
                
                // Head
                drawCircle(color, radius = headRadius, center = Offset(centerX, headCenterY), style = stroke)
                
                // Neck
                drawLine(color, Offset(centerX, headCenterY + headRadius), Offset(centerX, shoulderY), strokeWidth = strokeWidth)
                
                // Shoulders
                drawLine(color, Offset(shoulderLeft, shoulderY), Offset(shoulderRight, shoulderY), strokeWidth = strokeWidth)
                
                // Torso trapezoid
                drawLine(color, Offset(shoulderLeft, shoulderY), Offset(hipLeft, hipY), strokeWidth = strokeWidth)
                drawLine(color, Offset(shoulderRight, shoulderY), Offset(hipRight, hipY), strokeWidth = strokeWidth)
                
                // Hips
                drawLine(color, Offset(hipLeft, hipY), Offset(hipRight, hipY), strokeWidth = strokeWidth)
                
                // Legs
                drawLine(color, Offset(hipLeft, hipY), Offset(hipLeft, kneeY), strokeWidth = strokeWidth)
                drawLine(color, Offset(hipRight, hipY), Offset(hipRight, kneeY), strokeWidth = strokeWidth)
                drawLine(color, Offset(hipLeft, kneeY), Offset(hipLeft, bottomY), strokeWidth = strokeWidth)
                drawLine(color, Offset(hipRight, kneeY), Offset(hipRight, bottomY), strokeWidth = strokeWidth)
            }
            
            // Draw glow
            drawSkeleton(6.dp.toPx(), 0.2f)
            // Draw core
            drawSkeleton(2.dp.toPx(), 1.0f)
        }

        // Draw measurement annotations
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        
        // Shoulder annotation
        val shoulderLabelY = shoulderY - 20.dp.toPx()
        drawLine(CloudGray.copy(alpha = 0.5f), Offset(shoulderLeft, shoulderLabelY), Offset(shoulderRight, shoulderLabelY), strokeWidth = 1.dp.toPx(), pathEffect = dashPathEffect)
        val shoulderTextX = (centerX - shoulderTextLayout.size.width / 2f).coerceIn(0f, (width - shoulderTextLayout.size.width).coerceAtLeast(0f))
        val shoulderTextY = (shoulderLabelY - 15.dp.toPx()).coerceIn(0f, (height - shoulderTextLayout.size.height).coerceAtLeast(0f))
        drawText(shoulderTextLayout, topLeft = Offset(shoulderTextX, shoulderTextY))
        
        // Hip annotation
        val hipLabelY = hipY + 10.dp.toPx()
        drawLine(CloudGray.copy(alpha = 0.5f), Offset(hipLeft, hipLabelY), Offset(hipRight, hipLabelY), strokeWidth = 1.dp.toPx(), pathEffect = dashPathEffect)
        val hipTextX = (centerX - hipTextLayout.size.width / 2f).coerceIn(0f, (width - hipTextLayout.size.width).coerceAtLeast(0f))
        val hipTextY = (hipLabelY + 5.dp.toPx()).coerceIn(0f, (height - hipTextLayout.size.height).coerceAtLeast(0f))
        drawText(hipTextLayout, topLeft = Offset(hipTextX, hipTextY))
        
        // Height annotation
        val heightLineX = (shoulderRight + 30.dp.toPx()).coerceAtMost(width - 1f)
        drawLine(CloudGray.copy(alpha = 0.5f), Offset(heightLineX, topY), Offset(heightLineX, bottomY), strokeWidth = 1.dp.toPx(), pathEffect = dashPathEffect)
        val heightTextX = (heightLineX + 5.dp.toPx()).coerceIn(0f, (width - heightTextLayout.size.width).coerceAtLeast(0f))
        val heightTextY = (height / 2f).coerceIn(0f, (height - heightTextLayout.size.height).coerceAtLeast(0f))
        drawText(heightTextLayout, topLeft = Offset(heightTextX, heightTextY))
    }
}
