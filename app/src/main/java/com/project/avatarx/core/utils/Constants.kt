package com.project.avatarx.core.utils

object Constants {
    // Animation durations
    const val ANIM_DURATION_SHORT = 200
    const val ANIM_DURATION_MEDIUM = 400
    const val ANIM_DURATION_LONG = 800
    const val ANIM_DURATION_REVEAL = 1200

    // MediaPipe Landmark indices
    const val LANDMARK_NOSE = 0
    const val LANDMARK_LEFT_SHOULDER = 11
    const val LANDMARK_RIGHT_SHOULDER = 12
    const val LANDMARK_LEFT_HIP = 23
    const val LANDMARK_RIGHT_HIP = 24
    const val LANDMARK_LEFT_ANKLE = 27
    const val LANDMARK_RIGHT_ANKLE = 28

    // Measurement
    const val REFERENCE_SHOULDER_WIDTH_CM = 46f
    const val REFERENCE_HIP_WIDTH_CM = 42f
    const val REFERENCE_HEIGHT_CM = 174f

    // Smoothing
    const val POSITION_SMOOTHING_FACTOR = 0.3f

    // Particle system
    const val PARTICLE_COUNT = 40
}
