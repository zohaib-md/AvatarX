package com.project.avatarx.domain.model

data class FitCompatibility(
    val overallScore: Float,
    val shoulderAlignment: String,
    val torsoFit: String,
    val sizeMatch: String,
    val isRecommended: Boolean
)
