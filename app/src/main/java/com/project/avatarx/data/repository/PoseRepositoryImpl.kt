package com.project.avatarx.data.repository

import com.project.avatarx.domain.model.PoseLandmarkData
import com.project.avatarx.domain.repository.PoseRepository
import com.project.avatarx.ml.pose.PoseLandmarkerHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseRepositoryImpl @Inject constructor(
    private val poseLandmarkerHelper: PoseLandmarkerHelper
) : PoseRepository {

    override val poseResults: Flow<PoseLandmarkData?> = poseLandmarkerHelper.results

    override fun startDetection() {
        poseLandmarkerHelper.initialize()
    }

    override fun stopDetection() {
        poseLandmarkerHelper.close()
    }

    override fun processImageProxy(imageProxy: Any, rotationDegrees: Int) {
        poseLandmarkerHelper.detectAsync(imageProxy, rotationDegrees)
    }
}
