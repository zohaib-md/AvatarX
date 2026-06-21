package com.project.avatarx.presentation.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.avatarx.ui.theme.AccentGradient
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.ConfidenceGradient
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.SilverHighlight

@Composable
fun ScanAccuracyRing(
    targetValue: Float, // 0f to 1f
    label: String = "Scan Accuracy",
    size: Dp = 140.dp,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedValue by animateFloatAsState(
        targetValue = if (animationPlayed) targetValue else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "ring_animation"
    )

    LaunchedEffect(targetValue) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            val gradient = if (targetValue > 0.8f) ConfidenceGradient else AccentGradient
            
            drawArc(
                color = DimGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animatedValue,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedValue * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = SilverHighlight
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CloudGray
            )
        }
    }
}
