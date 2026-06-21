package com.project.avatarx.presentation.screens.virtualtryon

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.avatarx.domain.model.Garment
import com.project.avatarx.presentation.components.GarmentOverlayView
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.presentation.navigation.MeasurementsHolder
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.GraphiteSurface
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlass

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VirtualTryOnScreen(
    garmentId: String,
    onAnalyzeFit: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: VirtualTryOnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val capturedBitmap = MeasurementsHolder.capturedImage
    val capturedLandmarks = MeasurementsHolder.capturedLandmarks

    LaunchedEffect(garmentId) {
        viewModel.selectGarment(garmentId)
    }

    val garmentBitmap = remember(uiState.garment) {
        uiState.garment?.overlayResId?.let { resId ->
            BitmapFactory.decodeResource(context.resources, resId)
        } ?: uiState.garment?.localImagePath?.let { path ->
            BitmapFactory.decodeFile(path)
        }
    }

    // User gesture state for pinch-to-zoom and drag-to-reposition
    var userScale by remember { mutableFloatStateOf(1f) }
    var userOffsetX by remember { mutableFloatStateOf(0f) }
    var userOffsetY by remember { mutableFloatStateOf(0f) }

    // Reset user adjustments when garment changes
    LaunchedEffect(uiState.garment?.id) {
        userScale = 1f
        userOffsetX = 0f
        userOffsetY = 0f
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(NightBlack)) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(capturedBitmap, capturedLandmarks, screenWidthPx, screenHeightPx) {
            if (capturedBitmap != null && capturedLandmarks != null) {
                viewModel.initAvatarTransform(capturedLandmarks, screenWidthPx.toInt(), screenHeightPx.toInt())
            }
        }

        // Background Avatar
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Your Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // Animated Garment Overlay with gesture support
        AnimatedContent(
            targetState = garmentBitmap,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
            },
            modifier = Modifier.fillMaxSize(),
            label = "garment_transition"
        ) { targetBitmap ->
            targetBitmap?.let { bitmap ->
                GarmentOverlayView(
                    garmentBitmap = bitmap,
                    overlayTransform = uiState.overlayTransform,
                    userScale = userScale,
                    userOffsetX = userOffsetX,
                    userOffsetY = userOffsetY,
                    onGestureTransform = { scaleChange, panX, panY ->
                        userScale = (userScale * scaleChange).coerceIn(0.3f, 4f)
                        userOffsetX += panX
                        userOffsetY += panY
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NightBlack.copy(alpha = 0.8f),
                            Color.Transparent,
                            NightBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(SlateGlass)
            ) {
                Icon(Icons.Rounded.Close, null, tint = SilverHighlight)
            }

            Text(
                text = "Avatar Studio",
                style = MaterialTheme.typography.titleMedium,
                color = SilverHighlight,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = navBarPadding + 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Garment Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.garments) { garment ->
                    GarmentThumbnail(
                        garment = garment,
                        isSelected = garment.id == uiState.garment?.id,
                        onClick = { viewModel.selectGarment(garment.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(
                text = "Analyze Fit",
                onClick = {
                    uiState.garment?.let {
                        onAnalyzeFit(it.id)
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun GarmentThumbnail(
    garment: Garment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ElectricBlue else Color.Transparent
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GraphiteSurface)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (garment.localImagePath != null) {
            coil.compose.AsyncImage(
                model = java.io.File(garment.localImagePath),
                contentDescription = garment.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else if (garment.imageResId != null) {
            Image(
                painter = painterResource(id = garment.imageResId),
                contentDescription = garment.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
