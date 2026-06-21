package com.project.avatarx.presentation.screens.avatarprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.project.avatarx.domain.model.*
import com.project.avatarx.ml.measurements.BodyMeasurementCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AvatarProfileUiState(
    val measurements: BodyMeasurements = BodyMeasurements(),
    val fashionDNA: FashionDNA = FashionDNA(
        bodyType = BodyType.REGULAR,
        fitProfile = "Regular",
        recommendedSize = GarmentSize.M,
        trackingAccuracy = 0.94f
    ),
    val insights: List<BodyInsight> = emptyList(),
    val isRevealed: Boolean = false
)

@HiltViewModel
class AvatarProfileViewModel @Inject constructor(
    private val calculator: BodyMeasurementCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarProfileUiState())
    val uiState: StateFlow<AvatarProfileUiState> = _uiState.asStateFlow()

    fun setMeasurements(measurements: BodyMeasurements) {
        val fashionDNA = FashionDNA(
            bodyType = measurements.bodyType,
            fitProfile = when (measurements.bodyType) {
                BodyType.ATHLETIC -> "Athletic Fit"
                BodyType.SLIM -> "Slim Fit"
                BodyType.BROAD -> "Relaxed Fit"
                BodyType.REGULAR -> "Regular"
            },
            recommendedSize = measurements.recommendedSize,
            trackingAccuracy = measurements.trackingConfidence
        )

        val insights = calculator.generateInsights(measurements)

        _uiState.value = AvatarProfileUiState(
            measurements = measurements,
            fashionDNA = fashionDNA,
            insights = insights,
            isRevealed = true
        )
    }
}
