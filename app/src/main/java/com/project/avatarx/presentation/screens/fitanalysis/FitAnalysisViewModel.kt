package com.project.avatarx.presentation.screens.fitanalysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.project.avatarx.BuildConfig
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.domain.model.FitAnalysis
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.domain.repository.GarmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class FitAnalysisUiState(
    val garment: Garment? = null,
    val analysis: FitAnalysis? = null,
    val isLoaded: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FitAnalysisViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FitAnalysisUiState())
    val uiState: StateFlow<FitAnalysisUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val generativeModel = if (apiKey.isNotEmpty() && apiKey != "null") {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    } else null

    fun loadAnalysis(garmentId: String, measurements: BodyMeasurements, capturedBodyImage: Bitmap?) {
        val garment = garmentRepository.getGarmentById(garmentId) ?: return

        _uiState.update { it.copy(garment = garment, isProcessing = true, error = null) }

        viewModelScope.launch {
            try {
                if (generativeModel != null && capturedBodyImage != null) {
                    // Get garment bitmap
                    val garmentBitmap = garment.overlayResId?.let { resId ->
                        BitmapFactory.decodeResource(context.resources, resId)
                    } ?: garment.localImagePath?.let { path ->
                        BitmapFactory.decodeFile(path)
                    }

                    if (garmentBitmap != null) {
                        val analysis = generateAiAnalysis(garment, measurements, capturedBodyImage, garmentBitmap)
                        _uiState.update { it.copy(analysis = analysis, isLoaded = true, isProcessing = false) }
                        return@launch
                    }
                }
                
                // Fallback to local logic if no API key or images
                val fallbackAnalysis = garmentRepository.generateFitAnalysis(garment, measurements)
                _uiState.update { it.copy(analysis = fallbackAnalysis, isLoaded = true, isProcessing = false) }

            } catch (e: Exception) {
                // Fallback to local logic on error
                android.util.Log.e("FitAnalysisAI", "AI Analysis failed: ${e.message}", e)
                val fallbackAnalysis = garmentRepository.generateFitAnalysis(garment, measurements)
                _uiState.update { it.copy(
                    analysis = fallbackAnalysis,
                    isLoaded = true, 
                    isProcessing = false,
                    error = "AI Analysis failed, using local model. ${e.message}"
                )}
            }
        }
    }

    private suspend fun generateAiAnalysis(
        garment: Garment, 
        measurements: BodyMeasurements, 
        bodyBitmap: Bitmap, 
        garmentBitmap: Bitmap
    ): FitAnalysis {
        
        val prompt = """
            You are a professional fashion stylist and tailor AI. 
            Analyze the provided user body image and the selected garment image. 
            The user has the following body measurements:
            - Shoulder Width: ${measurements.shoulderWidthCm} cm
            - Recommended Size: ${measurements.recommendedSize.name}
            - Body Type: ${measurements.bodyType.name}

            The garment is a ${garment.name} (${garment.category}, ${garment.fitType} fit).

            Provide a personalized fit analysis.
            Output ONLY a raw JSON object with no markdown formatting and no extra text. Use the following fields:
            - "insight": A 2-3 sentence personalized styling insight explaining how the garment fits the user's specific body shape.
            - "comfortScore": A float between 0.0 and 1.0 representing how comfortable this fit would be.
            - "styleScore": A float between 0.0 and 1.0 representing how stylish the match is.
            - "alignmentScore": A float between 0.0 and 1.0 representing how well the garment's proportions align with the user's proportions.
        """.trimIndent()

        val inputContent = content {
            image(bodyBitmap)
            image(garmentBitmap)
            text(prompt)
        }

        val response = generativeModel!!.generateContent(inputContent)
        var textResponse = response.text ?: "{}"
        
        // Extract JSON block using regex in case the model adds markdown or conversational text
        val jsonRegex = Regex("""\{[\s\S]*\}""")
        val matchResult = jsonRegex.find(textResponse)
        if (matchResult != null) {
            textResponse = matchResult.value
        }
        
        val json = JSONObject(textResponse)
        
        return FitAnalysis(
            recommendedSize = measurements.recommendedSize,
            confidence = 0.95f,
            comfortScore = json.optDouble("comfortScore", 0.85).toFloat(),
            styleScore = json.optDouble("styleScore", 0.85).toFloat(),
            alignmentScore = json.optDouble("alignmentScore", 0.85).toFloat(),
            insight = json.optString("insight", "The garment fits well with your body proportions.")
        )
    }
}
