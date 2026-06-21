package com.project.avatarx.presentation.screens.fitanalysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.presentation.components.AnimatedScoreBar
import com.project.avatarx.presentation.components.GlassCard
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.InterFontFamily
import com.project.avatarx.ui.theme.MintSuccess
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlass
import com.project.avatarx.ui.theme.SlateGlassBorder
import com.project.avatarx.ui.theme.SpaceMonoFontFamily
import com.project.avatarx.ui.theme.SteelBlue
import com.project.avatarx.ui.theme.StoneGray
import kotlinx.coroutines.delay

@Composable
fun FitAnalysisScreen(
    garmentId: String,
    measurements: BodyMeasurements,
    capturedBodyImage: android.graphics.Bitmap?,
    onTryAnother: () -> Unit,
    onDone: () -> Unit,
    viewModel: FitAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(garmentId) {
        viewModel.loadAnalysis(garmentId, measurements, capturedBodyImage)
    }

    if (!uiState.isLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NightBlack),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ANALYZING FIT",
                        style = TextStyle(
                            fontFamily = SpaceMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 4.sp,
                            color = ElectricBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.CircularProgressIndicator(
                        color = ElectricBlue,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        return
    }

    val analysis = uiState.analysis!!
    val garment = uiState.garment!!

    var showHeader by remember { mutableStateOf(false) }
    var showSize by remember { mutableStateOf(false) }
    var showInsight by remember { mutableStateOf(false) }
    var showScores by remember { mutableStateOf(false) }
    var showCTA by remember { mutableStateOf(false) }

    val sizeScale = remember { Animatable(0.5f) }

    LaunchedEffect(uiState.isLoaded) {
        showHeader = true
        delay(300)
        showSize = true
        sizeScale.animateTo(1f, tween(500, easing = EaseOutCubic))
        delay(200)
        showInsight = true
        delay(300)
        showScores = true
        delay(300)
        showCTA = true
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlack)
            .verticalScroll(rememberScrollState())
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = navBarPadding + 16.dp)
    ) {
        // === HEADER ===
        AnimatedVisibility(
            visible = showHeader,
            enter = fadeIn(tween(600))
        ) {
            Column {
                Text(
                    text = "FIT ANALYSIS",
                    style = TextStyle(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 6.sp,
                        color = ElectricBlue
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = garment.name,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = SilverHighlight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // === RECOMMENDED SIZE — Big dramatic display ===
        AnimatedVisibility(
            visible = showSize,
            enter = fadeIn(tween(600))
        ) {
            GlassCard(
                accentBorder = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RECOMMENDED SIZE",
                        style = TextStyle(
                            fontFamily = SpaceMonoFontFamily,
                            fontSize = 9.sp,
                            letterSpacing = 3.sp,
                            color = CloudGray
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysis.recommendedSize.displayName,
                        style = TextStyle(
                            fontFamily = SpaceMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 72.sp,
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricBlue, SteelBlue)
                            )
                        ),
                        modifier = Modifier.graphicsLayer {
                            scaleX = sizeScale.value
                            scaleY = sizeScale.value
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Confidence inline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (analysis.confidence > 0.8f) MintSuccess.copy(alpha = 0.15f)
                                    else ElectricBlue.copy(alpha = 0.15f),
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(analysis.confidence * 100).toInt()}% CONFIDENCE",
                                style = TextStyle(
                                    fontFamily = SpaceMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    color = if (analysis.confidence > 0.8f) MintSuccess else ElectricBlue
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === AI INSIGHT ===
        AnimatedVisibility(
            visible = showInsight,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateGlass, MaterialTheme.shapes.small)
                    .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.small)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .background(ElectricBlue.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI INSIGHT",
                        style = TextStyle(
                            fontFamily = SpaceMonoFontFamily,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            color = ElectricBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = analysis.insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CloudGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === SCORE BARS ===
        AnimatedVisibility(
            visible = showScores,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
        ) {
            Column {
                Text(
                    text = "FIT METRICS",
                    style = TextStyle(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = ElectricBlue
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedScoreBar(
                    label = "Comfort Fit",
                    score = analysis.comfortScore,
                    barColor = MintSuccess,
                    animationDelay = 0
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedScoreBar(
                    label = "Style Match",
                    score = analysis.styleScore,
                    barColor = ElectricBlue,
                    animationDelay = 200
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedScoreBar(
                    label = "Body Alignment",
                    score = analysis.alignmentScore,
                    barColor = SteelBlue,
                    animationDelay = 400
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // === CTA ===
        AnimatedVisibility(
            visible = showCTA,
            enter = fadeIn(tween(600))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                GradientButton(
                    text = "DONE",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onTryAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.small),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "TRY ANOTHER GARMENT",
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
