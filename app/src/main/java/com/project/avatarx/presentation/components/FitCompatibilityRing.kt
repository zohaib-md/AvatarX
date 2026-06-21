package com.project.avatarx.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import com.project.avatarx.ui.theme.AmberWarn
import com.project.avatarx.ui.theme.CoralAlert
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.MintSuccess
import com.project.avatarx.ui.theme.SilverHighlight

@Composable
fun FitCompatibilityRing(
    score: Float, // 0f to 100f
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedScore by animateFloatAsState(
        targetValue = if (animationPlayed) score else 0f,
        animationSpec = tween(1000),
        label = "fit_ring_animation"
    )

    LaunchedEffect(score) {
        animationPlayed = true
    }

    val color = when {
        score > 85f -> MintSuccess
        score >= 70f -> AmberWarn
        else -> CoralAlert
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawArc(
                color = DimGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (animatedScore / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = animatedScore.toInt().toString(),
            style = MaterialTheme.typography.labelLarge,
            color = SilverHighlight
        )
    }
}
