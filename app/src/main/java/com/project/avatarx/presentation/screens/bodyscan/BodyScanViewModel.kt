package com.project.avatarx.presentation.screens.bodyscan

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.PoseLandmarkData
import com.project.avatarx.domain.repository.MeasurementRepository
import com.project.avatarx.domain.repository.PoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BodyScanUiState(
    val isTracking: Boolean = false,
    val shouldersDetected: Boolean = false,
    val hipsDetected: Boolean = false,
    val poseActive: Boolean = false,
    val confidence: Float = 0f,
    val currentLandmarks: PoseLandmarkData? = null,
    val capturedMeasurements: BodyMeasurements? = null,
    val capturedBitmap: Bitmap? = null,
    val capturedLandmarks: List<com.project.avatarx.domain.model.NormalizedLandmark>? = null,
    val isFrontCamera: Boolean = true,
    val isCapturing: Boolean = false
)

@HiltViewModel
class BodyScanViewModel @Inject constructor(
    private val poseRepository: PoseRepository,
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyScanUiState())
    val uiState: StateFlow<BodyScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            poseRepository.poseResults.collect { poseData ->
                if (poseData != null) {
                    _uiState.update { state ->
                        state.copy(
                            isTracking = true,
                            shouldersDetected = poseData.shouldersDetected,
                            hipsDetected = poseData.hipsDetected,
                            poseActive = poseData.isFullBodyDetected,
                            confidence = poseData.landmarks
                                .filter { it.visibility > 0.5f }
                                .map { it.visibility }
                                .let { visibilities ->
                                    if (visibilities.isEmpty()) 0f
                                    else visibilities.average().toFloat().coerceIn(0f, 1f)
                                },
                            currentLandmarks = poseData
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isTracking = false,
                            shouldersDetected = false,
                            hipsDetected = false,
                            poseActive = false,
                            confidence = 0f,
                            currentLandmarks = null
                        )
                    }
                }
            }
        }
    }

    fun startDetection() {
        poseRepository.startDetection()
    }

    fun stopDetection() {
        poseRepository.stopDetection()
    }

    fun processFrame(imageProxy: Any, rotationDegrees: Int) {
        poseRepository.processImageProxy(imageProxy, rotationDegrees)
    }

    fun toggleCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun captureAndAnalyze(bitmap: Bitmap) {
        val landmarks = _uiState.value.currentLandmarks ?: return
        _uiState.update { it.copy(isCapturing = true) }

        // Stop detection immediately to prevent camera thread from racing
        poseRepository.stopDetection()

        viewModelScope.launch {
            try {
                val measurements = measurementRepository.calculateMeasurements(landmarks)
                _uiState.update { it.copy(capturedMeasurements = measurements, capturedBitmap = bitmap, capturedLandmarks = landmarks.landmarks, isCapturing = false) }
            } catch (e: Exception) {
                // If calculation fails for any reason, reset and let user retry
                _uiState.update { it.copy(isCapturing = false) }
                poseRepository.startDetection()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        poseRepository.stopDetection()
    }
}

