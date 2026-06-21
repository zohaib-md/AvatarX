package com.project.avatarx.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.SilverHighlight
import kotlinx.coroutines.delay

@Composable
fun AnimatedScoreBar(
    label: String,
    score: Float, // 0f to 1f
    barColor: Color = ElectricBlue,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedScore by animateFloatAsState(
        targetValue = if (animationPlayed) score else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "bar_animation"
    )

    LaunchedEffect(score) {
        delay(animationDelay.toLong())
        animationPlayed = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = CloudGray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(animatedScore * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = SilverHighlight
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(DimGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedScore)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}
