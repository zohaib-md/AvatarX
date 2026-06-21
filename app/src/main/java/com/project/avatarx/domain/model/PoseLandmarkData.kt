package com.project.avatarx.domain.model

data class NormalizedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

data class PoseLandmarkData(
    val landmarks: List<NormalizedLandmark>,
    val timestampMs: Long,
    val imageWidth: Int,
    val imageHeight: Int
) {
    val shouldersDetected: Boolean
        get() = landmarks.size > 12 &&
                landmarks[11].visibility > 0.5f &&
                landmarks[12].visibility > 0.5f

    val hipsDetected: Boolean
        get() = landmarks.size > 24 &&
                landmarks[23].visibility > 0.5f &&
                landmarks[24].visibility > 0.5f

    val isFullBodyDetected: Boolean
        get() = shouldersDetected && hipsDetected
}
