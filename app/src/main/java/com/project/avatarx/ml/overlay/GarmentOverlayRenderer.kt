package com.project.avatarx.ml.overlay

import android.graphics.Bitmap
import android.graphics.Matrix
import com.project.avatarx.domain.model.NormalizedLandmark
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2

data class OverlayTransform(
    val anchorX: Float, // Midpoint of shoulders X
    val anchorY: Float, // Midpoint of shoulders Y
    val shoulderDistance: Float,
    val rotation: Float
)

@Singleton
class GarmentOverlayRenderer @Inject constructor() {

    companion object {
        private const val POSITION_SMOOTHING_FACTOR = 0.3f
        private const val GARMENT_REFERENCE_WIDTH = 200f
        private const val GARMENT_REFERENCE_HEIGHT = 300f
        private const val SCALE_PADDING_FACTOR = 1.4f
    }

    // Smoothed values for exponential moving average
    private var smoothedCenterX: Float? = null
    private var smoothedCenterY: Float? = null
    private var smoothedShoulderDistance: Float? = null
    private var smoothedRotation: Float? = null

    fun calculateTransform(
        leftShoulder: NormalizedLandmark,
        rightShoulder: NormalizedLandmark,
        leftHip: NormalizedLandmark,
        rightHip: NormalizedLandmark,
        canvasWidth: Float,
        canvasHeight: Float
    ): OverlayTransform {
        // Calculate anchor point exactly at the midpoint of shoulders (the collar area)
        val rawAnchorX = ((leftShoulder.x + rightShoulder.x) / 2f) * canvasWidth
        val rawAnchorY = ((leftShoulder.y + rightShoulder.y) / 2f) * canvasHeight

        // Calculate shoulder distance in canvas space
        val shoulderDistX = (rightShoulder.x - leftShoulder.x) * canvasWidth
        val shoulderDistY = (rightShoulder.y - leftShoulder.y) * canvasHeight
        val rawShoulderDistance = kotlin.math.sqrt(shoulderDistX * shoulderDistX + shoulderDistY * shoulderDistY)

        // Calculate rotation from shoulder angle.
        // ML Kit leftShoulder is on the right side of the image (higher X) and rightShoulder is on the left.
        // To get 0 degrees when level, dx should be positive.
        val dy = leftShoulder.y - rightShoulder.y
        val dx = leftShoulder.x - rightShoulder.x
        val rawRotation = Math.toDegrees(
            atan2(dy.toDouble(), dx.toDouble())
        ).toFloat()

        // Apply exponential moving average smoothing
        val anchorX = applySmoothing(smoothedCenterX, rawAnchorX).also { smoothedCenterX = it }
        val anchorY = applySmoothing(smoothedCenterY, rawAnchorY).also { smoothedCenterY = it }
        val shoulderDistance = applySmoothing(smoothedShoulderDistance, rawShoulderDistance).also { smoothedShoulderDistance = it }
        val rotation = applySmoothing(smoothedRotation, rawRotation).also { smoothedRotation = it }

        return OverlayTransform(
            anchorX = anchorX,
            anchorY = anchorY,
            shoulderDistance = shoulderDistance,
            rotation = rotation
        )
    }

    fun createTransformMatrix(transform: OverlayTransform, garmentBitmap: Bitmap): Matrix {
        val matrix = Matrix()

        val bitmapWidth = garmentBitmap.width.toFloat()
        val bitmapHeight = garmentBitmap.height.toFloat()

        // Translate to position the center of the bitmap at the target center
        matrix.postTranslate(-bitmapWidth / 2f, -bitmapHeight / 2f)

        // The garment should be about 1.4x the shoulder distance (padding factor)
        val targetWidth = transform.shoulderDistance * SCALE_PADDING_FACTOR
        val scale = targetWidth / bitmapWidth

        // Apply scaling
        matrix.postScale(scale, scale)

        // Apply rotation around center
        matrix.postRotate(transform.rotation)

        // Translate to final canvas position using the anchor.
        // We handle the collar offset dynamically inside the View where bitmap height is known.
        matrix.postTranslate(transform.anchorX, transform.anchorY)

        return matrix
    }

    fun resetSmoothing() {
        smoothedCenterX = null
        smoothedCenterY = null
        smoothedShoulderDistance = null
        smoothedRotation = null
    }

    private fun applySmoothing(previousValue: Float?, newValue: Float): Float {
        return if (previousValue == null) {
            newValue
        } else {
            previousValue + POSITION_SMOOTHING_FACTOR * (newValue - previousValue)
        }
    }
}
