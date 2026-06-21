package com.project.avatarx.presentation.components

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.project.avatarx.ml.overlay.OverlayTransform

@Composable
fun GarmentOverlayView(
    garmentBitmap: Bitmap?,
    overlayTransform: OverlayTransform?,
    userScale: Float = 1f,
    userOffsetX: Float = 0f,
    userOffsetY: Float = 0f,
    onGestureTransform: (scaleChange: Float, panX: Float, panY: Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val paint = remember {
        Paint().apply {
            alpha = 240 // 94% opacity for slight blending into the environment
            isAntiAlias = true
            isFilterBitmap = true
            // Subtle drop shadow for depth and realistic integration
            setShadowLayer(
                20f, // radius
                0f,  // dx
                15f, // dy
                android.graphics.Color.argb(90, 0, 0, 0) // shadow color
            )
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onGestureTransform(zoom, pan.x, pan.y)
                }
            }
    ) {
        if (garmentBitmap != null && overlayTransform != null) {
            val matrix = Matrix()
            val bitmapWidth = garmentBitmap.width.toFloat()
            val bitmapHeight = garmentBitmap.height.toFloat()

            // Calculate base scale from shoulder distance
            // 2.5x shoulder distance covers the full torso width realistically
            val targetWidth = overlayTransform.shoulderDistance * 2.5f
            val baseScale = targetWidth / bitmapWidth

            // Apply user's pinch-to-zoom adjustment on top
            val finalScale = baseScale * userScale
            val targetHeight = bitmapHeight * finalScale

            // Translate bitmap center to origin for scaling/rotation
            matrix.postTranslate(-bitmapWidth / 2f, -bitmapHeight / 2f)
            
            // Apply final scale (base + user adjustment)
            matrix.postScale(finalScale, finalScale)
            
            // Apply rotation aligned to shoulders
            matrix.postRotate(overlayTransform.rotation)
            
            // Position: collar (top ~12%) aligns with shoulder anchor, 
            // then apply user's drag offset
            val collarOffset = targetHeight * 0.12f
            val centerY = overlayTransform.anchorY + (targetHeight / 2f) - collarOffset

            matrix.postTranslate(
                overlayTransform.anchorX + userOffsetX,
                centerY + userOffsetY
            )

            drawContext.canvas.nativeCanvas.drawBitmap(garmentBitmap, matrix, paint)
        }
    }
}
