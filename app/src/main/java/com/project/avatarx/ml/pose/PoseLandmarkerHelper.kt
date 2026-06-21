package com.project.avatarx.ml.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.project.avatarx.domain.model.NormalizedLandmark
import com.project.avatarx.domain.model.PoseLandmarkData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseLandmarkerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PoseLandmarkerHelper"
        private const val MODEL_NAME = "pose_landmarker_lite.task"
        private const val MIN_POSE_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_POSE_TRACKING_CONFIDENCE = 0.5f
        private const val MIN_POSE_PRESENCE_CONFIDENCE = 0.5f
        private const val NUM_POSES = 1
    }

    private var poseLandmarker: PoseLandmarker? = null

    private val _results = MutableSharedFlow<PoseLandmarkData?>(
        replay = 1,
        extraBufferCapacity = 5
    )
    val results: SharedFlow<PoseLandmarkData?> = _results

    fun initialize() {
        try {
            close()
            setupPoseLandmarker()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PoseLandmarker: ${e.message}", e)
        }
    }

    private fun setupPoseLandmarker() {
        val baseOptions = try {
            // Try GPU delegate first for better performance
            BaseOptions.builder()
                .setModelAssetPath(MODEL_NAME)
                .setDelegate(Delegate.GPU)
                .build()
        } catch (e: Exception) {
            Log.w(TAG, "GPU delegate not available, falling back to CPU: ${e.message}")
            try {
                BaseOptions.builder()
                    .setModelAssetPath(MODEL_NAME)
                    .setDelegate(Delegate.CPU)
                    .build()
            } catch (cpuError: Exception) {
                Log.e(TAG, "Failed to create base options: ${cpuError.message}", cpuError)
                return
            }
        }

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMinPoseDetectionConfidence(MIN_POSE_DETECTION_CONFIDENCE)
            .setMinPosePresenceConfidence(MIN_POSE_PRESENCE_CONFIDENCE)
            .setMinTrackingConfidence(MIN_POSE_TRACKING_CONFIDENCE)
            .setNumPoses(NUM_POSES)
            .setResultListener(this::handleResult)
            .setErrorListener(this::handleError)
            .build()

        try {
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.d(TAG, "PoseLandmarker initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PoseLandmarker from options: ${e.message}", e)
            // Retry with CPU if GPU setup failed silently
            retryWithCpu()
        }
    }

    private fun retryWithCpu() {
        try {
            val cpuBaseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_NAME)
                .setDelegate(Delegate.CPU)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(cpuBaseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMinPoseDetectionConfidence(MIN_POSE_DETECTION_CONFIDENCE)
                .setMinPosePresenceConfidence(MIN_POSE_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_POSE_TRACKING_CONFIDENCE)
                .setNumPoses(NUM_POSES)
                .setResultListener(this::handleResult)
                .setErrorListener(this::handleError)
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.d(TAG, "PoseLandmarker initialized with CPU fallback")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PoseLandmarker with CPU: ${e.message}", e)
        }
    }

    fun detectAsync(imageProxy: Any, rotationDegrees: Int) {
        if (poseLandmarker == null) {
            Log.w(TAG, "PoseLandmarker is not initialized. Call initialize() first.")
            return
        }

        try {
            val cameraImageProxy = imageProxy as? androidx.camera.core.ImageProxy
                ?: run {
                    Log.e(TAG, "imageProxy is not an instance of ImageProxy")
                    return
                }

            val bitmap = cameraImageProxy.toBitmap()

            // Apply rotation if needed
            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val timestampMs = cameraImageProxy.imageInfo.timestamp / 1_000 // Convert to ms

            poseLandmarker?.detectAsync(mpImage, timestampMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error during async detection: ${e.message}", e)
        }
    }

    private fun handleResult(result: PoseLandmarkerResult, input: MPImage) {
        try {
            if (result.landmarks().isEmpty() || result.landmarks()[0].isEmpty()) {
                _results.tryEmit(null)
                return
            }

            val mediapipeLandmarks = result.landmarks()[0]
            val normalizedLandmarks = mediapipeLandmarks.map { landmark ->
                NormalizedLandmark(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = landmark.visibility().orElse(0f)
                )
            }

            val poseLandmarkData = PoseLandmarkData(
                landmarks = normalizedLandmarks,
                timestampMs = result.timestampMs(),
                imageWidth = input.width,
                imageHeight = input.height
            )

            _results.tryEmit(poseLandmarkData)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing pose result: ${e.message}", e)
            _results.tryEmit(null)
        }
    }

    private fun handleError(error: RuntimeException) {
        Log.e(TAG, "PoseLandmarker error: ${error.message}", error)
    }

    fun close() {
        try {
            poseLandmarker?.close()
            poseLandmarker = null
            Log.d(TAG, "PoseLandmarker closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing PoseLandmarker: ${e.message}", e)
        }
    }
}
