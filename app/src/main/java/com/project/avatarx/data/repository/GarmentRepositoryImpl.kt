package com.project.avatarx.data.repository

import com.project.avatarx.R
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.FitAnalysis
import com.project.avatarx.domain.model.FitCompatibility
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.domain.model.GarmentSize
import com.project.avatarx.domain.repository.GarmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GarmentRepositoryImpl @Inject constructor() : GarmentRepository {

    private val garmentCatalog: MutableList<Garment> = mutableListOf(
        Garment(
            id = "tshirt_001",
            name = "Classic Fit T-Shirt",
            category = "Tops",
            fitType = "Regular",
            imageResId = R.drawable.real_tshirt,
            overlayResId = R.drawable.real_tshirt
        ),
        Garment(
            id = "hoodie_001",
            name = "Urban Hoodie",
            category = "Outerwear",
            fitType = "Relaxed",
            imageResId = R.drawable.real_hoodie,
            overlayResId = R.drawable.real_hoodie
        ),
        Garment(
            id = "jacket_001",
            name = "Performance Jacket",
            category = "Outerwear",
            fitType = "Slim",
            imageResId = R.drawable.real_jacket,
            overlayResId = R.drawable.real_jacket
        )
    )

    override fun getGarments(): List<Garment> = garmentCatalog.toList()

    override fun getGarmentById(id: String): Garment? {
        return garmentCatalog.find { it.id == id }
    }

    override fun addGarment(garment: Garment) {
        garmentCatalog.add(0, garment) // Add to the top of the list
    }

    override fun calculateFitCompatibility(
        garment: Garment,
        measurements: BodyMeasurements
    ): FitCompatibility {
        val shoulderWidth = measurements.shoulderWidthCm
        val ratio = measurements.shoulderToHipRatio

        // Shoulder alignment scoring
        val shoulderAlignment: String
        val shoulderScore: Float
        when {
            shoulderWidth in 40f..48f -> {
                shoulderAlignment = "Excellent"
                shoulderScore = 95f
            }
            shoulderWidth in 36f..40f || shoulderWidth in 48f..52f -> {
                shoulderAlignment = "Good"
                shoulderScore = 80f
            }
            else -> {
                shoulderAlignment = "Fair"
                shoulderScore = 65f
            }
        }

        // Torso fit based on shoulder-to-hip ratio
        val torsoFit: String
        val torsoScore: Float
        when {
            ratio in 1.0f..1.2f -> {
                torsoFit = "Well Balanced"
                torsoScore = 92f
            }
            ratio in 0.9f..1.0f || ratio in 1.2f..1.3f -> {
                torsoFit = "Moderate Fit"
                torsoScore = 78f
            }
            else -> {
                torsoFit = "Loose Fit"
                torsoScore = 60f
            }
        }

        // Size match evaluation
        val garmentDefaultSize = when (garment.fitType) {
            "Slim" -> GarmentSize.M
            "Relaxed" -> GarmentSize.L
            else -> GarmentSize.M
        }
        val sizeMatch: String
        val sizeScore: Float
        if (measurements.recommendedSize == garmentDefaultSize) {
            sizeMatch = "Recommended Size Match"
            sizeScore = 100f
        } else {
            val sizeDiff = kotlin.math.abs(
                measurements.recommendedSize.ordinal - garmentDefaultSize.ordinal
            )
            when (sizeDiff) {
                1 -> {
                    sizeMatch = "Close Size Match"
                    sizeScore = 85f
                }
                else -> {
                    sizeMatch = "Size Adjustment Needed"
                    sizeScore = 65f
                }
            }
        }

        // Weighted overall score
        val overallScore = (shoulderScore * 0.35f + torsoScore * 0.35f + sizeScore * 0.30f)
        val isRecommended = overallScore > 85f

        return FitCompatibility(
            overallScore = overallScore,
            shoulderAlignment = shoulderAlignment,
            torsoFit = torsoFit,
            sizeMatch = sizeMatch,
            isRecommended = isRecommended
        )
    }

    override suspend fun generateFitAnalysis(
        garment: Garment,
        measurements: BodyMeasurements
    ): FitAnalysis {
        val compatibility = calculateFitCompatibility(garment, measurements)

        val confidence = measurements.trackingConfidence
        val comfortScore = calculateComfortScore(garment, measurements)
        val styleScore = calculateStyleScore(garment, measurements)
        val alignmentScore = compatibility.overallScore / 100f

        val insight = buildInsightString(garment, measurements, compatibility)

        return FitAnalysis(
            recommendedSize = measurements.recommendedSize,
            confidence = confidence,
            comfortScore = comfortScore,
            styleScore = styleScore,
            alignmentScore = alignmentScore,
            insight = insight
        )
    }

    private fun calculateComfortScore(garment: Garment, measurements: BodyMeasurements): Float {
        val baseScore = when (garment.fitType) {
            "Relaxed" -> 0.90f
            "Regular" -> 0.85f
            "Slim" -> 0.75f
            else -> 0.80f
        }

        // Adjust based on body type compatibility with fit type
        val bodyTypeAdjustment = when {
            garment.fitType == "Slim" && measurements.bodyType == com.project.avatarx.domain.model.BodyType.BROAD -> -0.10f
            garment.fitType == "Relaxed" && measurements.bodyType == com.project.avatarx.domain.model.BodyType.SLIM -> -0.05f
            garment.fitType == "Slim" && measurements.bodyType == com.project.avatarx.domain.model.BodyType.SLIM -> 0.05f
            garment.fitType == "Regular" && measurements.bodyType == com.project.avatarx.domain.model.BodyType.REGULAR -> 0.05f
            else -> 0f
        }

        return (baseScore + bodyTypeAdjustment).coerceIn(0f, 1f)
    }

    private fun calculateStyleScore(garment: Garment, measurements: BodyMeasurements): Float {
        val baseScore = when {
            measurements.bodyType == com.project.avatarx.domain.model.BodyType.ATHLETIC && garment.fitType == "Slim" -> 0.92f
            measurements.bodyType == com.project.avatarx.domain.model.BodyType.ATHLETIC && garment.fitType == "Regular" -> 0.88f
            measurements.bodyType == com.project.avatarx.domain.model.BodyType.REGULAR && garment.fitType == "Regular" -> 0.90f
            measurements.bodyType == com.project.avatarx.domain.model.BodyType.SLIM && garment.fitType == "Slim" -> 0.93f
            measurements.bodyType == com.project.avatarx.domain.model.BodyType.BROAD && garment.fitType == "Relaxed" -> 0.88f
            else -> 0.80f
        }

        return baseScore.coerceIn(0f, 1f)
    }

    private fun buildInsightString(
        garment: Garment,
        measurements: BodyMeasurements,
        compatibility: FitCompatibility
    ): String {
        val sizeInsight = when {
            compatibility.isRecommended ->
                "Size ${measurements.recommendedSize.displayName} is an excellent match for this ${garment.name}."
            compatibility.overallScore > 75f ->
                "Size ${measurements.recommendedSize.displayName} works well, though you might also try one size ${if (measurements.shoulderWidthCm > 48f) "up" else "down"}."
            else ->
                "Consider trying a different size for the best fit with this ${garment.name}."
        }

        val fitInsight = when (garment.fitType) {
            "Slim" -> "This slim-fit design follows body contours closely."
            "Relaxed" -> "The relaxed cut provides extra room for comfortable movement."
            else -> "This regular-fit design offers a balanced silhouette."
        }

        val bodyInsight = when (measurements.bodyType) {
            com.project.avatarx.domain.model.BodyType.ATHLETIC ->
                "Your athletic build pairs well with structured fits."
            com.project.avatarx.domain.model.BodyType.SLIM ->
                "Your slim frame is complemented by fitted designs."
            com.project.avatarx.domain.model.BodyType.BROAD ->
                "Your broader build benefits from relaxed or regular fits."
            com.project.avatarx.domain.model.BodyType.REGULAR ->
                "Your balanced proportions work well across fit types."
        }

        return "$sizeInsight $fitInsight $bodyInsight"
    }
}
