package com.project.avatarx.presentation.screens.avatarprofile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.project.avatarx.utils.ShareReportGenerator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.presentation.components.BodyWireframe
import com.project.avatarx.presentation.components.DigitalFitModelView
import com.project.avatarx.presentation.components.GlassCard
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.presentation.components.MetricChip
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.GraphiteSurface
import com.project.avatarx.ui.theme.InterFontFamily
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlass
import com.project.avatarx.ui.theme.SlateGlassBorder
import com.project.avatarx.ui.theme.SpaceMonoFontFamily
import com.project.avatarx.ui.theme.SteelBlue
import com.project.avatarx.ui.theme.StoneGray
import com.project.avatarx.ui.theme.MintSuccess
import com.project.avatarx.ui.theme.OnyxSurface
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AvatarProfileScreen(
    measurements: BodyMeasurements,
    capturedBitmap: android.graphics.Bitmap? = null,
    capturedLandmarks: List<com.project.avatarx.domain.model.NormalizedLandmark>? = null,
    onSelectGarment: () -> Unit,
    viewModel: AvatarProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(measurements) {
        viewModel.setMeasurements(measurements)
    }

    // === Animation States ===
    var isScanning by remember { mutableStateOf(true) }
    var showContent by remember { mutableStateOf(false) }
    var showHeader by remember { mutableStateOf(false) }
    var showTwin by remember { mutableStateOf(false) }
    var showMetrics by remember { mutableStateOf(false) }
    var showHighlights by remember { mutableStateOf(false) }
    var showDNA by remember { mutableStateOf(false) }
    var showCTA by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Visual Twin, 1 = Digital Fit Model

    val infiniteTransition = rememberInfiniteTransition(label = "header_anims")

    // Scan line animation
    val scanProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 1: 2-second body scanning animation
        scanProgress.animateTo(1f, tween(2000, easing = LinearEasing))
        delay(300)
        isScanning = false
        showContent = true

        // Phase 2: Staggered content reveal
        showHeader = true
        delay(300)
        showTwin = true
        delay(300)
        showMetrics = true
        delay(300)
        showHighlights = true
        delay(300)
        showDNA = true
        delay(300)
        showCTA = true
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlack)
    ) {
        // === SCANNING PHASE ===
        if (isScanning) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scanning label
                Text(
                    text = "ANALYZING BODY STRUCTURE",
                    style = TextStyle(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 4.sp,
                        color = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Body wireframe with scan line overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Body silhouette
                    if (capturedBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = capturedBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.large)
                                .alpha(0.4f),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        BodyWireframe(
                            shoulderWidth = measurements.shoulderWidthCm,
                            hipWidth = measurements.hipWidthCm,
                            heightCm = measurements.heightCm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .alpha(0.5f)
                        )
                    }

                    // Scan line overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineY = size.height * scanProgress.value

                        // Horizontal scan line
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ElectricBlue.copy(alpha = 0.8f),
                                    ElectricBlue,
                                    ElectricBlue.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(0f, lineY),
                            end = Offset(size.width, lineY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )

                        // Glow below scan line
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                startY = lineY,
                                endY = (lineY + 60f).coerceAtMost(size.height)
                            ),
                            topLeft = Offset(0f, lineY),
                            size = androidx.compose.ui.geometry.Size(
                                size.width,
                                60f.coerceAtMost(size.height - lineY)
                            )
                        )

                        // Dashed grid lines (scanned area)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
                        val lineCount = ((lineY / size.height) * 6).toInt()
                        for (i in 0 until lineCount) {
                            val y = size.height * (i + 1) / 7f
                            if (y < lineY) {
                                drawLine(
                                    color = ElectricBlue.copy(alpha = 0.1f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f,
                                    pathEffect = dashEffect
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress percentage
                Text(
                    text = "${(scanProgress.value * 100).toInt()}%",
                    style = TextStyle(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = SilverHighlight
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { scanProgress.value },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp),
                    color = ElectricBlue,
                    trackColor = DimGray.copy(alpha = 0.3f)
                )
            }
        }

        // === CONTENT PHASE ===
        if (showContent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = navBarPadding + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // === HEADER ===
                AnimatedVisibility(
                    visible = showHeader,
                    enter = fadeIn(tween(600))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ElectricBlue.copy(alpha = 0.06f),
                                        Color.Transparent
                                    )
                                ),
                                MaterialTheme.shapes.large
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ElectricBlue.copy(alpha = 0.25f),
                                        SlateGlassBorder
                                    )
                                ),
                                MaterialTheme.shapes.large
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Scanner badge
                        val scanSweep by infiniteTransition.animateFloat(
                            initialValue = -1f,
                            targetValue = 2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "scan_sweep"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Animated scanner sweep behind text
                            Canvas(
                                modifier = Modifier
                                    .matchParentSize()
                            ) {
                                val sweepWidth = size.width * 0.35f
                                val sweepX = size.width * scanSweep
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MintSuccess.copy(alpha = 0.15f),
                                            MintSuccess.copy(alpha = 0.25f),
                                            MintSuccess.copy(alpha = 0.15f),
                                            Color.Transparent
                                        ),
                                        startX = sweepX - sweepWidth,
                                        endX = sweepX + sweepWidth
                                    )
                                )
                            }

                            Text(
                                text = "SCAN COMPLETE",
                                style = TextStyle(
                                    fontFamily = SpaceMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 3.sp,
                                    color = MintSuccess
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Decorative line
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ElectricBlue,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "DIGITAL TWIN",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 6.sp,
                                color = SilverHighlight
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GENERATED",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 8.sp,
                                brush = Brush.linearGradient(
                                    colors = listOf(ElectricBlue, SteelBlue)
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Subtitle
                        Text(
                            text = "Visual identity + body-measurement intelligence",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = StoneGray
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // === TAB TOGGLE ===
                AnimatedVisibility(
                    visible = showTwin,
                    enter = fadeIn(tween(400))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GraphiteSurface, MaterialTheme.shapes.medium)
                            .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.medium)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TwinTabButton(
                            text = "VISUAL TWIN",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TwinTabButton(
                            text = "FIT MODEL",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === TWIN / FIT MODEL CONTENT ===
                AnimatedVisibility(
                    visible = showTwin,
                    enter = fadeIn(tween(600)) + scaleIn(
                        tween(600),
                        initialScale = 0.95f
                    )
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "twin_tab_content"
                    ) { tab ->
                        when (tab) {
                            0 -> {
                                // === VISUAL TWIN (original captured image) ===
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(MaterialTheme.shapes.large)
                                            .background(GraphiteSurface)
                                            .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.large),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (capturedBitmap != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = capturedBitmap.asImageBitmap(),
                                                contentDescription = "Digital Twin",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                NightBlack.copy(alpha = 0.3f),
                                                                Color.Transparent,
                                                                NightBlack.copy(alpha = 0.8f)
                                                            )
                                                        )
                                                    )
                                            )
                                            if (capturedLandmarks != null) {
                                                com.project.avatarx.presentation.components.PoseLandmarkOverlay(
                                                    landmarks = capturedLandmarks,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        } else {
                                            BodyWireframe(
                                                shoulderWidth = measurements.shoulderWidthCm,
                                                hipWidth = measurements.hipWidthCm,
                                                heightCm = measurements.heightCm,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(260.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(12.dp)
                                                .background(NightBlack.copy(alpha = 0.85f), MaterialTheme.shapes.small)
                                                .border(1.dp, ElectricBlue.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${(measurements.trackingConfidence * 100).toInt()}%",
                                                    style = TextStyle(
                                                        fontFamily = SpaceMonoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = ElectricBlue
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ACCURACY",
                                                    style = TextStyle(
                                                        fontFamily = SpaceMonoFontFamily,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 9.sp,
                                                        letterSpacing = 2.sp,
                                                        color = CloudGray
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your captured visual identity",
                                        style = TextStyle(
                                            fontFamily = SpaceMonoFontFamily,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp,
                                            color = StoneGray
                                        )
                                    )
                                }
                            }
                            1 -> {
                                // === DIGITAL FIT MODEL (measurement-based mannequin) ===
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    DigitalFitModelView(
                                        measurements = measurements,
                                        fashionDNA = uiState.fashionDNA,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Measurement-based model optimized for virtual fitting",
                                        style = TextStyle(
                                            fontFamily = SpaceMonoFontFamily,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp,
                                            color = StoneGray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === MEASUREMENTS GRID ===
                AnimatedVisibility(
                    visible = showMetrics,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip(label = "HEIGHT", value = "${measurements.heightCm.toInt()} cm")
                        MetricChip(label = "SHOULDERS", value = "${measurements.shoulderWidthCm.toInt()} cm")
                        MetricChip(label = "HIPS", value = "${measurements.hipWidthCm.toInt()} cm")
                        MetricChip(label = "RATIO", value = "%.2f".format(measurements.shoulderToHipRatio))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === SCAN HIGHLIGHTS ===
                AnimatedVisibility(
                    visible = showHighlights,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SCAN HIGHLIGHTS",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 4.sp,
                                color = ElectricBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Highlight cards
                        ScanHighlightRow(
                            label = "Body Type",
                            value = uiState.fashionDNA.bodyType.displayName,
                            detail = "Detected from shoulder-to-hip proportions"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScanHighlightRow(
                            label = "Fit Profile",
                            value = uiState.fashionDNA.fitProfile,
                            detail = "Recommended based on body geometry"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScanHighlightRow(
                            label = "Recommended Size",
                            value = uiState.fashionDNA.recommendedSize.displayName,
                            detail = "Optimized for your measurements",
                            isAccent = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === FASHION DNA ===
                AnimatedVisibility(
                    visible = showDNA,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
                ) {
                    GlassCard(
                        accentBorder = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "FASHION DNA",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 4.sp,
                                color = ElectricBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DNADataPoint(label = "TYPE", value = uiState.fashionDNA.bodyType.displayName)
                            DNADataPoint(label = "FIT", value = uiState.fashionDNA.fitProfile)
                            DNADataPoint(label = "SIZE", value = uiState.fashionDNA.recommendedSize.displayName, isAccent = true)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tracking accuracy bar
                        Text(
                            text = "TRACKING ACCURACY",
                            style = TextStyle(
                                fontFamily = SpaceMonoFontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 2.sp,
                                color = CloudGray
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { uiState.fashionDNA.trackingAccuracy.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp),
                                color = ElectricBlue,
                                trackColor = DimGray.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${(uiState.fashionDNA.trackingAccuracy * 100).toInt()}%",
                                style = TextStyle(
                                    fontFamily = SpaceMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ElectricBlue
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === CTA BUTTONS ===
                AnimatedVisibility(
                    visible = showCTA,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GradientButton(
                            text = "SELECT GARMENT",
                            onClick = onSelectGarment,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Export Scan Report
                        TextButton(
                            onClick = { showExportSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.small),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = null,
                                tint = CloudGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXPORT SCAN REPORT",
                                style = TextStyle(
                                    fontFamily = SpaceMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 2.sp,
                                    color = CloudGray
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showExportSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = sheetState,
            containerColor = OnyxSurface,
            scrimColor = NightBlack.copy(alpha = 0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SHARE DIGITAL TWIN",
                    style = TextStyle(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 4.sp,
                        color = ElectricBlue
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your scan is ready to be shared with your tailor or on your socials.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CloudGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "SHARE ON WHATSAPP",
                    onClick = {
                        coroutineScope.launch {
                            showExportSheet = false
                            ShareReportGenerator.generateAndShare(
                                context = context,
                                uiState = uiState,
                                twinBitmap = capturedBitmap,
                                shareToWhatsApp = true
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            showExportSheet = false
                            ShareReportGenerator.generateAndShare(
                                context = context,
                                uiState = uiState,
                                twinBitmap = capturedBitmap,
                                shareToWhatsApp = false
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OTHER OPTIONS",
                        style = TextStyle(
                            fontFamily = SpaceMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            color = StoneGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(navBarPadding))
            }
        }
    }
}

@Composable
private fun ScanHighlightRow(
    label: String,
    value: String,
    detail: String,
    isAccent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateGlass, MaterialTheme.shapes.small)
            .border(1.dp, if (isAccent) ElectricBlue.copy(alpha = 0.3f) else SlateGlassBorder, MaterialTheme.shapes.small)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    color = CloudGray
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = StoneGray,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isAccent) ElectricBlue else SilverHighlight
            )
        )
    }
}

@Composable
private fun DNADataPoint(
    label: String,
    value: String,
    isAccent: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = CloudGray
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isAccent) ElectricBlue else SilverHighlight
            )
        )
    }
}

@Composable
private fun TwinTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) ElectricBlue.copy(alpha = 0.5f) else Color.Transparent
    val textColor = if (isSelected) ElectricBlue else StoneGray

    TextButton(
        onClick = onClick,
        modifier = modifier
            .background(bgColor, MaterialTheme.shapes.small)
            .border(1.dp, borderColor, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = textColor
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
