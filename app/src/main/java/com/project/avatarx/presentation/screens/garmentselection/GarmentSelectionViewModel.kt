package com.project.avatarx.presentation.screens.garmentselection

import androidx.lifecycle.ViewModel
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.domain.repository.GarmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GarmentSelectionUiState(
    val garments: List<Garment> = emptyList(),
    val selectedIndex: Int = 0
)

@HiltViewModel
class GarmentSelectionViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarmentSelectionUiState())
    val uiState: StateFlow<GarmentSelectionUiState> = _uiState.asStateFlow()

    fun loadGarments(measurements: BodyMeasurements) {
        val garments = garmentRepository.getGarments().map { garment ->
            garment.copy(
                fitCompatibility = garmentRepository.calculateFitCompatibility(garment, measurements)
            )
        }
        _uiState.value = GarmentSelectionUiState(garments = garments)
    }

    fun selectGarment(index: Int) {
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun getSelectedGarment(): Garment? = _uiState.value.garments.getOrNull(_uiState.value.selectedIndex)
}
