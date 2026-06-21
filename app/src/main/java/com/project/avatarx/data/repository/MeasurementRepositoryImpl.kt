package com.project.avatarx.data.repository

import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.PoseLandmarkData
import com.project.avatarx.domain.repository.MeasurementRepository
import com.project.avatarx.ml.measurements.BodyMeasurementCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementRepositoryImpl @Inject constructor(
    private val calculator: BodyMeasurementCalculator
) : MeasurementRepository {

    override fun calculateMeasurements(poseLandmarkData: PoseLandmarkData): BodyMeasurements {
        return calculator.calculate(poseLandmarkData)
    }
}
