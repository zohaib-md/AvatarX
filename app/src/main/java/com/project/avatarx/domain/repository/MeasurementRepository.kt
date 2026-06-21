package com.project.avatarx.domain.repository

import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.PoseLandmarkData

interface MeasurementRepository {
    fun calculateMeasurements(poseLandmarkData: PoseLandmarkData): BodyMeasurements
}
