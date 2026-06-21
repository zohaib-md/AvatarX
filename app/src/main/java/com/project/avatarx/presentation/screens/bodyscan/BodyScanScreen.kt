package com.project.avatarx.presentation.screens.bodyscan

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.presentation.components.PoseLandmarkOverlay
import com.project.avatarx.presentation.components.StatusChip
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlass
import com.project.avatarx.ui.theme.SlateGlassBorder
import com.project.avatarx.ui.theme.SteelBlue
import com.project.avatarx.ui.theme.StoneGray
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BodyScanScreen(
    onScanComplete: (BodyMeasurements, Bitmap, List<com.project.avatarx.domain.model.NormalizedLandmark>?) -> Unit,
    onClose: () -> Unit,
    viewModel: BodyScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Navigate when measurements are captured
    LaunchedEffect(uiState.capturedMeasurements, uiState.capturedBitmap) {
        if (uiState.capturedMeasurements != null && uiState.capturedBitmap != null) {
            onScanComplete(uiState.capturedMeasurements!!, uiState.capturedBitmap!!, uiState.capturedLandmarks)
        }
    }

    // Start/stop detection lifecycle
    DisposableEffect(Unit) {
        viewModel.startDetection()
        onDispose {
            viewModel.stopDetection()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraContent(
            uiState = uiState,
            onClose = onClose,
            onToggleCamera = viewModel::toggleCamera,
            onCapture = { bitmap -> viewModel.captureAndAnalyze(bitmap) },
            onProcessFrame = viewModel::processFrame
        )
    } else {
        PermissionRequestContent(
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(72.dp),
                tint = ElectricBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Camera Access Required",
                style = MaterialTheme.typography.headlineSmall,
                color = SilverHighlight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AvatarX needs camera access to perform body scanning and pose tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = StoneGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(
                text = "Grant Camera Access",
                onClick = onRequestPermission
            )
        }
    }
}

@Composable
private fun CameraContent(
    uiState: BodyScanUiState,
    onClose: () -> Unit,
    onToggleCamera: () -> Unit,
    onCapture: (Bitmap) -> Unit,
    onProcessFrame: (Any, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE)
            bindToLifecycle(lifecycleOwner)
        }
    }

    // Update camera selector when toggled
    LaunchedEffect(uiState.isFrontCamera) {
        cameraController.cameraSelector = if (uiState.isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // Set image analysis analyzer
    LaunchedEffect(Unit) {
        cameraController.setImageAnalysisAnalyzer(
            Executors.newSingleThreadExecutor()
        ) { imageProxy ->
            onProcessFrame(imageProxy, imageProxy.imageInfo.rotationDegrees)
            imageProxy.close()
        }
    }

    // Infinite rotation animation for capture ring
    val infiniteTransition = rememberInfiniteTransition(label = "captureRing")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    // Pulsing dot animation
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingDot"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pose landmark overlay
        val currentLandmarks = uiState.currentLandmarks
        if (currentLandmarks != null) {
            PoseLandmarkOverlay(
                landmarks = currentLandmarks.landmarks,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top bar with glassmorphism
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(SlateGlass)
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = SilverHighlight
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tracking active indicator
            AnimatedVisibility(
                visible = uiState.isTracking,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = pulsingAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Body Tracking Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Camera flip button
            IconButton(onClick = onToggleCamera) {
                Icon(
                    imageVector = Icons.Rounded.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
                    tint = SilverHighlight
                )
            }
        }

        // Bottom panel with glassmorphism
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    SlateGlass,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .border(
                    width = 1.dp,
                    color = SlateGlassBorder,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusChip(
                    text = "Shoulders Detected",
                    isActive = uiState.shouldersDetected
                )
                StatusChip(
                    text = "Hips Detected",
                    isActive = uiState.hipsDetected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatusChip(
                text = "Pose Tracking Active",
                isActive = uiState.poseActive
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence text
            Text(
                text = "Tracking Confidence: ${(uiState.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricBlue,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Capture button with animated gradient ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .then(
                        if (uiState.isTracking) {
                            Modifier.border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        ElectricBlue,
                                        SteelBlue,
                                        ElectricBlue.copy(alpha = 0.3f),
                                        ElectricBlue
                                    )
                                ),
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 2.dp,
                                color = StoneGray,
                                shape = CircleShape
                            )
                        }
                    )
                    .then(
                        if (uiState.isTracking) {
                            Modifier.rotate(ringRotation)
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = uiState.poseActive && !uiState.isCapturing) {
                        cameraController.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    val bitmap = imageProxy.toBitmap()
                                    onCapture(bitmap)
                                    imageProxy.close()
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    // Fallback handling or ignore
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Inner filled circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.poseActive) {
                                Brush.linearGradient(
                                    colors = listOf(ElectricBlue, SteelBlue)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        StoneGray.copy(alpha = 0.5f),
                                        StoneGray.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        )
                )
            }
        }
    }
}
