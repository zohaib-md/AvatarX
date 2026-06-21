package com.project.avatarx.domain.model

data class BodyMeasurements(
    val heightCm: Float = 174f,
    val shoulderWidthCm: Float = 46f,
    val hipWidthCm: Float = 42f,
    val shoulderToHipRatio: Float = 1.095f,
    val bodyType: BodyType = BodyType.REGULAR,
    val recommendedSize: GarmentSize = GarmentSize.M,
    val trackingConfidence: Float = 0.94f
)
