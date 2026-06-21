package com.project.avatarx.presentation.screens.virtualtryon

import androidx.lifecycle.ViewModel
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.domain.model.NormalizedLandmark
import com.project.avatarx.domain.repository.GarmentRepository
import com.project.avatarx.ml.overlay.GarmentOverlayRenderer
import com.project.avatarx.ml.overlay.OverlayTransform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TryOnUiState(
    val garment: Garment? = null,
    val garments: List<Garment> = emptyList(),
    val overlayTransform: OverlayTransform? = null
)

@HiltViewModel
class VirtualTryOnViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository,
    private val overlayRenderer: GarmentOverlayRenderer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TryOnUiState())
    val uiState: StateFlow<TryOnUiState> = _uiState.asStateFlow()

    init {
        loadAllGarments()
    }

    private fun loadAllGarments() {
        val garments = garmentRepository.getGarments()
        _uiState.update { it.copy(garments = garments) }
    }

    fun selectGarment(garmentId: String) {
        val garment = garmentRepository.getGarmentById(garmentId)
        _uiState.update { it.copy(garment = garment) }
    }

    fun initAvatarTransform(landmarks: List<NormalizedLandmark>?, imageWidth: Int, imageHeight: Int) {
        if (landmarks != null && landmarks.size >= 33) {
            val transform = overlayRenderer.calculateTransform(
                leftShoulder = landmarks[11],
                rightShoulder = landmarks[12],
                leftHip = landmarks[23],
                rightHip = landmarks[24],
                canvasWidth = imageWidth.toFloat(),
                canvasHeight = imageHeight.toFloat()
            )
            _uiState.update { it.copy(overlayTransform = transform) }
        }
    }
}
