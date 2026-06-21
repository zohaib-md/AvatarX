package com.project.avatarx.domain.model

data class FitAnalysis(
    val recommendedSize: GarmentSize,
    val confidence: Float,
    val comfortScore: Float,
    val styleScore: Float,
    val alignmentScore: Float,
    val insight: String
)
