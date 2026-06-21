package com.project.avatarx.ml.measurements

import com.project.avatarx.domain.model.BodyInsight
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.BodyType
import com.project.avatarx.domain.model.GarmentSize
import com.project.avatarx.domain.model.NormalizedLandmark
import com.project.avatarx.domain.model.PoseLandmarkData
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class BodyMeasurementCalculator @Inject constructor() {

    companion object {
        // Scaling factors for converting normalized coordinates to approximate cm
        // Reduced to prevent instantly hitting max clamps when standing normally
        private const val SHOULDER_SCALE = 160f
        private const val HIP_SCALE = 160f
        private const val HEIGHT_SCALE = 220f

        // Clamp ranges for reasonable body measurements
        private const val MIN_SHOULDER_CM = 35f
        private const val MAX_SHOULDER_CM = 60f
        private const val MIN_HIP_CM = 30f
        private const val MAX_HIP_CM = 55f
        private const val MIN_HEIGHT_CM = 150f
        private const val MAX_HEIGHT_CM = 200f

        // MediaPipe landmark indices
        private const val NOSE = 0
        private const val LEFT_SHOULDER = 11
        private const val RIGHT_SHOULDER = 12
        private const val LEFT_HIP = 23
        private const val RIGHT_HIP = 24
        private const val LEFT_ANKLE = 27
        private const val RIGHT_ANKLE = 28
    }

    fun calculate(poseLandmarkData: PoseLandmarkData): BodyMeasurements {
        val landmarks = poseLandmarkData.landmarks

        // Need at least 29 landmarks (up to index 28 for RIGHT_ANKLE)
        if (!poseLandmarkData.isFullBodyDetected || landmarks.size < 29) {
            return BodyMeasurements() // Return defaults if body not fully detected
        }

        return try {
            val leftShoulder = landmarks[LEFT_SHOULDER]
            val rightShoulder = landmarks[RIGHT_SHOULDER]
            val leftHip = landmarks[LEFT_HIP]
            val rightHip = landmarks[RIGHT_HIP]

            // Calculate normalized distances
            val normalizedShoulderDist = euclideanDistance2D(leftShoulder, rightShoulder)
            val normalizedHipDist = euclideanDistance2D(leftHip, rightHip)

            // Calculate height from nose to midpoint of ankles
            val normalizedHeight = calculateNormalizedHeight(landmarks)

            // Scale to approximate real-world cm and clamp to reasonable ranges
            val shoulderWidthCm = (normalizedShoulderDist * SHOULDER_SCALE)
                .coerceIn(MIN_SHOULDER_CM, MAX_SHOULDER_CM)
            val hipWidthCm = (normalizedHipDist * HIP_SCALE)
                .coerceIn(MIN_HIP_CM, MAX_HIP_CM)
            val heightCm = (normalizedHeight * HEIGHT_SCALE)
                .coerceIn(MIN_HEIGHT_CM, MAX_HEIGHT_CM)

            // Calculate ratios
            val shoulderToHipRatio = if (hipWidthCm > 0f) {
                shoulderWidthCm / hipWidthCm
            } else {
                1.0f
            }

            // Determine body type
            val bodyType = classifyBodyType(shoulderWidthCm, shoulderToHipRatio)

            // Recommend garment size
            val recommendedSize = recommendSize(shoulderWidthCm)

            // Calculate tracking confidence as average visibility of ALL key landmarks
            // This yields a much more realistic (and fluctuating) accuracy score than just using shoulders/hips
            val trackingConfidence = calculateTrackingConfidence(*landmarks.toTypedArray())

            BodyMeasurements(
                heightCm = heightCm,
                shoulderWidthCm = shoulderWidthCm,
                hipWidthCm = hipWidthCm,
                shoulderToHipRatio = shoulderToHipRatio,
                bodyType = bodyType,
                recommendedSize = recommendedSize,
                trackingConfidence = trackingConfidence
            )
        } catch (e: Exception) {
            // Fallback to defaults if anything goes wrong during calculation
            BodyMeasurements()
        }
    }

    private fun euclideanDistance2D(a: NormalizedLandmark, b: NormalizedLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculateNormalizedHeight(landmarks: List<NormalizedLandmark>): Float {
        val nose = landmarks.getOrNull(NOSE)
        val leftAnkle = landmarks.getOrNull(LEFT_ANKLE)
        val rightAnkle = landmarks.getOrNull(RIGHT_ANKLE)

        if (nose == null || leftAnkle == null || rightAnkle == null) {
            return 0.35f // Default normalized height
        }

        // Midpoint of ankles
        val ankleMidX = (leftAnkle.x + rightAnkle.x) / 2f
        val ankleMidY = (leftAnkle.y + rightAnkle.y) / 2f

        val dx = nose.x - ankleMidX
        val dy = nose.y - ankleMidY
        return sqrt(dx * dx + dy * dy)
    }

    private fun classifyBodyType(shoulderWidthCm: Float, shoulderToHipRatio: Float): BodyType {
        return when {
            shoulderToHipRatio > 1.35f -> BodyType.ATHLETIC  // Increased threshold for V-taper
            shoulderToHipRatio < 1.10f -> BodyType.SLIM      // Adjusted for narrower shoulders relative to hips
            shoulderWidthCm > 52f -> BodyType.BROAD
            else -> BodyType.REGULAR
        }
    }

    private fun recommendSize(shoulderWidthCm: Float): GarmentSize {
        return when {
            shoulderWidthCm < 36f -> GarmentSize.XS
            shoulderWidthCm < 40f -> GarmentSize.S
            shoulderWidthCm < 44f -> GarmentSize.M
            shoulderWidthCm < 48f -> GarmentSize.L
            shoulderWidthCm < 52f -> GarmentSize.XL
            else -> GarmentSize.XXL
        }
    }

    private fun calculateTrackingConfidence(vararg landmarks: NormalizedLandmark): Float {
        if (landmarks.isEmpty()) return 0f
        return landmarks.map { it.visibility }.average().toFloat().coerceIn(0f, 1f)
    }

    fun generateInsights(measurements: BodyMeasurements): List<BodyInsight> {
        val insights = mutableListOf<BodyInsight>()

        // Body type insight
        val bodyTypeInsight = when (measurements.bodyType) {
            BodyType.ATHLETIC -> BodyInsight(
                iconName = "fitness_center",
                text = "Athletic build detected — structured and fitted styles will complement your frame well."
            )
            BodyType.SLIM -> BodyInsight(
                iconName = "straighten",
                text = "Slim build detected — slim-fit and tapered designs will enhance your silhouette."
            )
            BodyType.BROAD -> BodyInsight(
                iconName = "open_with",
                text = "Broad build detected — relaxed and regular fits will provide optimal comfort."
            )
            BodyType.REGULAR -> BodyInsight(
                iconName = "balance",
                text = "Balanced proportions detected — most fit types will work well for your frame."
            )
        }
        insights.add(bodyTypeInsight)

        // Size recommendation insight
        val sizeInsight = BodyInsight(
            iconName = "checkroom",
            text = "Recommended size: ${measurements.recommendedSize.displayName} — based on shoulder width of ${String.format("%.1f", measurements.shoulderWidthCm)} cm."
        )
        insights.add(sizeInsight)

        // Tracking quality insight
        val confidencePercent = (measurements.trackingConfidence * 100).toInt()
        val trackingInsight = when {
            measurements.trackingConfidence > 0.85f -> BodyInsight(
                iconName = "verified",
                text = "Excellent tracking quality ($confidencePercent%) — measurements are highly reliable."
            )
            measurements.trackingConfidence > 0.65f -> BodyInsight(
                iconName = "check_circle",
                text = "Good tracking quality ($confidencePercent%) — measurements are reasonably accurate."
            )
            else -> BodyInsight(
                iconName = "info",
                text = "Fair tracking quality ($confidencePercent%) — try better lighting or adjust your distance for improved accuracy."
            )
        }
        insights.add(trackingInsight)

        return insights
    }
}
