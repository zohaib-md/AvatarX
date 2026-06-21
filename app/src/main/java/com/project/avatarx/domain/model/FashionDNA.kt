package com.project.avatarx.domain.model

data class FashionDNA(
    val bodyType: BodyType,
    val fitProfile: String,
    val recommendedSize: GarmentSize,
    val trackingAccuracy: Float
)
