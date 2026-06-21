package com.project.avatarx.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.project.avatarx.domain.model.NormalizedLandmark
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.IceBlue

@Composable
fun PoseLandmarkOverlay(
    landmarks: List<NormalizedLandmark>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Filter and map to canvas coordinates
        val points = landmarks.map { lm ->
            if (lm.visibility > 0.5f) {
                Offset(lm.x * width, lm.y * height)
            } else {
                null
            }
        }

        fun drawConnection(startIdx: Int, endIdx: Int) {
            val start = points.getOrNull(startIdx)
            val end = points.getOrNull(endIdx)
            if (start != null && end != null) {
                drawLine(
                    color = IceBlue.copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Draw connections (Skeleton)
        // Torso
        drawConnection(11, 12)
        drawConnection(11, 23)
        drawConnection(12, 24)
        drawConnection(23, 24)
        
        // Arms
        drawConnection(11, 13)
        drawConnection(13, 15)
        drawConnection(12, 14)
        drawConnection(14, 16)
        
        // Legs
        drawConnection(23, 25)
        drawConnection(25, 27)
        drawConnection(24, 26)
        drawConnection(26, 28)
        
        // Head to shoulders
        drawConnection(0, 11)
        drawConnection(0, 12)

        // Draw points
        points.forEach { offset ->
            if (offset != null) {
                // Glow
                drawCircle(
                    color = IceBlue.copy(alpha = 0.2f),
                    radius = 8.dp.toPx(),
                    center = offset
                )
                // Core dot
                drawCircle(
                    color = ElectricBlue,
                    radius = 4.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}
