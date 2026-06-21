package com.project.avatarx.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.BodyType
import com.project.avatarx.domain.model.FashionDNA
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.GraphiteSurface
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlassBorder
import com.project.avatarx.ui.theme.SpaceMonoFontFamily
import com.project.avatarx.ui.theme.SteelBlue
import com.project.avatarx.ui.theme.StoneGray

@Composable
fun DigitalFitModelView(
    measurements: BodyMeasurements,
    fashionDNA: FashionDNA,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fit_model")

    // Breathing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Scan line sweep
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    // Draw-in animation
    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drawProgress.animateTo(1f, tween(1200))
    }

    // Normalize body proportions for the mannequin
    val shoulderRatio = ((measurements.shoulderWidthCm - 35f) / 25f).coerceIn(0.2f, 1f)
    val hipRatio = ((measurements.hipWidthCm - 30f) / 25f).coerceIn(0.2f, 1f)
    val heightRatio = ((measurements.heightCm - 150f) / 50f).coerceIn(0.2f, 1f)

    val accentColor = ElectricBlue
    val glowColor = SteelBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(GraphiteSurface, MaterialTheme.shapes.large)
            .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.large),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val progress = drawProgress.value

            // === Grid background ===
            drawGrid(w, h)

            // === Body proportions ===
            val headRadius = w * 0.055f
            val neckY = h * 0.13f
            val shoulderY = h * 0.20f
            val shoulderHalf = w * (0.12f + shoulderRatio * 0.14f)
            val waistY = h * 0.48f
            val waistHalf = w * 0.07f
            val hipY = h * 0.55f
            val hipHalf = w * (0.08f + hipRatio * 0.10f)
            val kneeY = h * 0.73f
            val ankleY = h * (0.85f + heightRatio * 0.08f)
            val footY = ankleY + h * 0.03f

            // Adapt torso curvature by body type
            val torsoFlare = when (measurements.bodyType) {
                BodyType.ATHLETIC -> 0.85f
                BodyType.SLIM -> 0.95f
                BodyType.BROAD -> 0.75f
                BodyType.REGULAR -> 0.90f
            }

            // === HEAD ===
            if (progress > 0.05f) {
                // Outer glow
                drawCircle(
                    color = accentColor.copy(alpha = glowAlpha * 0.2f),
                    radius = headRadius * 1.5f,
                    center = Offset(cx, h * 0.07f)
                )
                drawCircle(
                    color = accentColor.copy(alpha = glowAlpha * 0.7f),
                    radius = headRadius,
                    center = Offset(cx, h * 0.07f),
                    style = Stroke(width = 2.5f)
                )
                // Face cross
                drawLine(
                    color = accentColor.copy(alpha = 0.3f),
                    start = Offset(cx - headRadius * 0.5f, h * 0.07f),
                    end = Offset(cx + headRadius * 0.5f, h * 0.07f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.3f),
                    start = Offset(cx, h * 0.07f - headRadius * 0.5f),
                    end = Offset(cx, h * 0.07f + headRadius * 0.5f),
                    strokeWidth = 1f
                )
            }

            // === NECK ===
            if (progress > 0.1f) {
                drawLine(
                    color = accentColor.copy(alpha = 0.6f),
                    start = Offset(cx, h * 0.07f + headRadius),
                    end = Offset(cx, neckY),
                    strokeWidth = 2f
                )
            }

            // === TORSO (left side) ===
            if (progress > 0.2f) {
                val leftTorso = Path().apply {
                    moveTo(cx, neckY)
                    lineTo(cx - shoulderHalf, shoulderY)
                    // Arm stub
                    lineTo(cx - shoulderHalf - w * 0.04f, shoulderY + h * 0.15f)
                }
                drawPath(
                    leftTorso,
                    color = accentColor.copy(alpha = 0.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Torso side with curve for body type
                val leftSide = Path().apply {
                    moveTo(cx - shoulderHalf, shoulderY)
                    cubicTo(
                        cx - shoulderHalf * torsoFlare, waistY * 0.7f,
                        cx - waistHalf * 1.2f, waistY * 0.9f,
                        cx - waistHalf, waistY
                    )
                    cubicTo(
                        cx - waistHalf * 0.9f, (waistY + hipY) / 2f,
                        cx - hipHalf * 0.9f, hipY * 0.95f,
                        cx - hipHalf, hipY
                    )
                }
                drawPath(
                    leftSide,
                    color = accentColor.copy(alpha = 0.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // === TORSO (right side, mirrored) ===
            if (progress > 0.3f) {
                val rightTorso = Path().apply {
                    moveTo(cx, neckY)
                    lineTo(cx + shoulderHalf, shoulderY)
                    lineTo(cx + shoulderHalf + w * 0.04f, shoulderY + h * 0.15f)
                }
                drawPath(
                    rightTorso,
                    color = accentColor.copy(alpha = 0.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val rightSide = Path().apply {
                    moveTo(cx + shoulderHalf, shoulderY)
                    cubicTo(
                        cx + shoulderHalf * torsoFlare, waistY * 0.7f,
                        cx + waistHalf * 1.2f, waistY * 0.9f,
                        cx + waistHalf, waistY
                    )
                    cubicTo(
                        cx + waistHalf * 0.9f, (waistY + hipY) / 2f,
                        cx + hipHalf * 0.9f, hipY * 0.95f,
                        cx + hipHalf, hipY
                    )
                }
                drawPath(
                    rightSide,
                    color = accentColor.copy(alpha = 0.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // === LEGS ===
            if (progress > 0.5f) {
                val legInset = w * 0.02f

                // Left leg
                val leftLeg = Path().apply {
                    moveTo(cx - hipHalf, hipY)
                    cubicTo(
                        cx - hipHalf * 0.8f, (hipY + kneeY) / 2f,
                        cx - legInset * 3f, kneeY * 0.95f,
                        cx - legInset * 2.5f, kneeY
                    )
                    lineTo(cx - legInset * 2f, ankleY)
                    // Foot
                    lineTo(cx - legInset * 4f, footY)
                }
                drawPath(
                    leftLeg,
                    color = accentColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Right leg
                val rightLeg = Path().apply {
                    moveTo(cx + hipHalf, hipY)
                    cubicTo(
                        cx + hipHalf * 0.8f, (hipY + kneeY) / 2f,
                        cx + legInset * 3f, kneeY * 0.95f,
                        cx + legInset * 2.5f, kneeY
                    )
                    lineTo(cx + legInset * 2f, ankleY)
                    lineTo(cx + legInset * 4f, footY)
                }
                drawPath(
                    rightLeg,
                    color = accentColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // === MEASUREMENT ANNOTATION LINES ===
            if (progress > 0.7f) {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                // Shoulder width annotation
                drawLine(
                    color = glowColor.copy(alpha = 0.5f),
                    start = Offset(cx - shoulderHalf, shoulderY),
                    end = Offset(cx + shoulderHalf, shoulderY),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
                // Small ticks
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(cx - shoulderHalf, shoulderY - 6f), end = Offset(cx - shoulderHalf, shoulderY + 6f), strokeWidth = 1.5f)
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(cx + shoulderHalf, shoulderY - 6f), end = Offset(cx + shoulderHalf, shoulderY + 6f), strokeWidth = 1.5f)

                // Hip width annotation
                drawLine(
                    color = glowColor.copy(alpha = 0.5f),
                    start = Offset(cx - hipHalf, hipY),
                    end = Offset(cx + hipHalf, hipY),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(cx - hipHalf, hipY - 6f), end = Offset(cx - hipHalf, hipY + 6f), strokeWidth = 1.5f)
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(cx + hipHalf, hipY - 6f), end = Offset(cx + hipHalf, hipY + 6f), strokeWidth = 1.5f)

                // Height annotation (vertical right side)
                val annotX = cx + shoulderHalf + w * 0.12f
                drawLine(
                    color = glowColor.copy(alpha = 0.4f),
                    start = Offset(annotX, h * 0.07f - headRadius),
                    end = Offset(annotX, footY),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(annotX - 6f, h * 0.07f - headRadius), end = Offset(annotX + 6f, h * 0.07f - headRadius), strokeWidth = 1.5f)
                drawLine(color = glowColor.copy(alpha = 0.6f), start = Offset(annotX - 6f, footY), end = Offset(annotX + 6f, footY), strokeWidth = 1.5f)
            }

            // === JOINT NODES (glowing dots) ===
            if (progress > 0.6f) {
                val joints = listOf(
                    Offset(cx, neckY),
                    Offset(cx - shoulderHalf, shoulderY),
                    Offset(cx + shoulderHalf, shoulderY),
                    Offset(cx - waistHalf, waistY),
                    Offset(cx + waistHalf, waistY),
                    Offset(cx - hipHalf, hipY),
                    Offset(cx + hipHalf, hipY),
                    Offset(cx - w * 0.02f * 2.5f, kneeY),
                    Offset(cx + w * 0.02f * 2.5f, kneeY)
                )
                joints.forEach { joint ->
                    // Outer glow
                    drawCircle(
                        color = accentColor.copy(alpha = glowAlpha * 0.3f),
                        radius = 8f,
                        center = joint
                    )
                    // Inner dot
                    drawCircle(
                        color = accentColor,
                        radius = 3.5f,
                        center = joint
                    )
                }
            }

            // === SCANNING LINE ===
            val scanY = h * scanLineY
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.3f),
                        accentColor.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(w, scanY),
                strokeWidth = 2f
            )
        }

        // === MEASUREMENT LABELS overlaid on the canvas ===
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            FitModelLabel("HEIGHT", "${measurements.heightCm.toInt()} cm")
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp)
        ) {
            FitModelLabel("BODY TYPE", measurements.bodyType.name)
        }

        // Bottom left: Shoulder
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            FitModelLabel("SHOULDERS", "${measurements.shoulderWidthCm.toInt()} cm")
            Spacer(modifier = Modifier.height(8.dp))
            FitModelLabel("HIPS", "${measurements.hipWidthCm.toInt()} cm")
        }

        // Bottom right: Size + Accuracy
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            FitModelLabel("SIZE", fashionDNA.recommendedSize.name)
            Spacer(modifier = Modifier.height(8.dp))
            FitModelLabel("ACCURACY", "${(fashionDNA.trackingAccuracy * 100).toInt()}%")
        }
    }
}

@Composable
private fun FitModelLabel(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                color = StoneGray
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SilverHighlight
            )
        )
    }
}

private fun DrawScope.drawGrid(w: Float, h: Float) {
    val gridColor = Color.White.copy(alpha = 0.03f)
    val step = 40f
    var x = 0f
    while (x <= w) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), 1f)
        x += step
    }
    var y = 0f
    while (y <= h) {
        drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        y += step
    }
}
