package com.project.avatarx.presentation.screens.garmentselection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.presentation.components.FitCompatibilityRing
import com.project.avatarx.presentation.components.GlassCard
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.presentation.components.RecommendedBadge
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.DimGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.GraphiteSurface
import com.project.avatarx.ui.theme.MintSuccess
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.StoneGray

import coil.compose.AsyncImage
import java.io.File

@Composable
fun GarmentSelectionScreen(
    measurements: BodyMeasurements,
    onTryOn: (String) -> Unit,
    onScanGarment: () -> Unit,
    viewModel: GarmentSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadGarments(measurements)
    }

    val garments = uiState.garments
    val pagerState = rememberPagerState(pageCount = { garments.size })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectGarment(pagerState.currentPage)
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlack)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
            .padding(bottom = navBarPadding)
    ) {
        Text(
            text = "Select a Garment",
            style = MaterialTheme.typography.headlineLarge,
            color = SilverHighlight,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Text(
            text = "Based on your body profile",
            style = MaterialTheme.typography.bodyMedium,
            color = CloudGray,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (garments.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                val garment = garments[page]
                val isSelected = page == pagerState.currentPage
                
                val scale by animateFloatAsState(if (isSelected) 1.0f else 0.92f, label = "scale")
                val alpha by animateFloatAsState(if (isSelected) 1.0f else 0.7f, label = "alpha")

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    GlassCard(
                        accentBorder = isSelected,
                        accentColor = ElectricBlue,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Garment Image
                        if (garment.localImagePath != null) {
                            AsyncImage(
                                model = File(garment.localImagePath),
                                contentDescription = garment.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GraphiteSurface)
                                    .padding(24.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else if (garment.imageResId != null) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = garment.imageResId),
                                contentDescription = garment.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GraphiteSurface)
                                    .padding(24.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = garment.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = SilverHighlight
                        )
                        Text(
                            text = garment.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudGray
                        )
                        Text(
                            text = "${garment.fitType} Fit",
                            style = MaterialTheme.typography.labelLarge,
                            color = StoneGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        garment.fitCompatibility?.let { compatibility ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FitCompatibilityRing(score = compatibility.overallScore)
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Check, null, tint = MintSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Shoulders: ${compatibility.shoulderAlignment}", style = MaterialTheme.typography.labelSmall, color = CloudGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Check, null, tint = MintSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Torso: ${compatibility.torsoFit}", style = MaterialTheme.typography.labelSmall, color = CloudGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Check, null, tint = MintSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(compatibility.sizeMatch, style = MaterialTheme.typography.labelSmall, color = CloudGray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (compatibility.isRecommended) {
                                RecommendedBadge()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(garments.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ElectricBlue else DimGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "Try It On",
                onClick = { 
                    viewModel.getSelectedGarment()?.id?.let { onTryOn(it) } 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.material3.TextButton(
                onClick = onScanGarment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .border(1.dp, ElectricBlue, MaterialTheme.shapes.small),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Rounded.Checkroom, null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Custom Garment",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
