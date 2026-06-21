package com.project.avatarx.domain.repository

import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.FitAnalysis
import com.project.avatarx.domain.model.FitCompatibility
import com.project.avatarx.domain.model.Garment

interface GarmentRepository {
    fun getGarments(): List<Garment>
    fun getGarmentById(id: String): Garment?
    fun addGarment(garment: Garment)
    fun calculateFitCompatibility(garment: Garment, measurements: BodyMeasurements): FitCompatibility
    suspend fun generateFitAnalysis(garment: Garment, measurements: BodyMeasurements): FitAnalysis
}
