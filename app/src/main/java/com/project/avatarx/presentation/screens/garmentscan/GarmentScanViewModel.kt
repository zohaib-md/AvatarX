package com.project.avatarx.presentation.screens.garmentscan

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.domain.repository.GarmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class GarmentScanUiState(
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GarmentScanViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarmentScanUiState())
    val uiState: StateFlow<GarmentScanUiState> = _uiState.asStateFlow()

    private val segmenterOptions = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()
    
    private val segmenter = SubjectSegmentation.getClient(segmenterOptions)

    fun processCapturedGarment(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                segmenter.process(inputImage)
                    .addOnSuccessListener { result ->
                        val foregroundBitmap = result.foregroundBitmap
                        if (foregroundBitmap != null) {
                            val savedPath = saveBitmapToInternalStorage(context, foregroundBitmap)
                            val newGarmentId = "scanned_${UUID.randomUUID().toString().take(8)}"
                            
                            val newGarment = Garment(
                                id = newGarmentId,
                                name = "Custom Scanned Garment",
                                category = "Tops",
                                fitType = "Regular",
                                localImagePath = savedPath
                            )
                            
                            garmentRepository.addGarment(newGarment)
                            _uiState.update { it.copy(isProcessing = false, isSuccess = true) }
                        } else {
                            _uiState.update { it.copy(isProcessing = false, error = "Could not detect garment.") }
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Unknown error") }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val filename = "scanned_garment_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        segmenter.close()
    }
}
