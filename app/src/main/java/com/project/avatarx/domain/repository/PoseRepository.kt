package com.project.avatarx.domain.repository

import com.project.avatarx.domain.model.PoseLandmarkData
import kotlinx.coroutines.flow.Flow

interface PoseRepository {
    val poseResults: Flow<PoseLandmarkData?>
    fun startDetection()
    fun stopDetection()
    fun processImageProxy(imageProxy: Any, rotationDegrees: Int)
}
