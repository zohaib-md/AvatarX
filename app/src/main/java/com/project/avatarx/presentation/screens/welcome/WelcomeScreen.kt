package com.project.avatarx.presentation.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.avatarx.presentation.components.AbstractMeshBackground
import com.project.avatarx.presentation.components.GradientButton
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.ElectricBlue
import com.project.avatarx.ui.theme.InterFontFamily
import com.project.avatarx.ui.theme.NightBlack
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SpaceMonoFontFamily
import com.project.avatarx.ui.theme.StoneGray
import com.project.avatarx.ui.theme.SteelBlue
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onStartScan: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val initialAlpha = remember { Animatable(0f) }
    var expanded by remember { mutableStateOf(false) }
    val middleAlpha = remember { Animatable(0f) }
    val xOutlineAlpha = remember { Animatable(0f) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showBottom by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Phase 1: "AX" fades in
        initialAlpha.animateTo(1f, tween(800))
        delay(1500)

        // Phase 2: Expand "AX" → "AVATARX"
        expanded = true
        middleAlpha.animateTo(1f, tween(700))
        delay(300)

        // Phase 3: X flicker with white outline
        // Flicker 1
        xOutlineAlpha.snapTo(1f)
        delay(80)
        xOutlineAlpha.snapTo(0f)
        delay(120)

        // Flicker 2
        xOutlineAlpha.snapTo(0.8f)
        delay(60)
        xOutlineAlpha.snapTo(0f)
        delay(80)

        // Flicker 3 — rapid double
        xOutlineAlpha.snapTo(1f)
        delay(50)
        xOutlineAlpha.snapTo(0.3f)
        delay(40)
        xOutlineAlpha.snapTo(1f)
        delay(60)
        xOutlineAlpha.snapTo(0f)
        delay(150)

        // Final lock — outline pulses on and fades out smoothly
        xOutlineAlpha.snapTo(1f)
        delay(200)
        xOutlineAlpha.animateTo(0f, tween(600))

        // Phase 4: Show subtitle + CTA
        delay(200)
        showSubtitle = true
        delay(400)
        showBottom = true
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val xGradientStyle = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = 0.sp,
        brush = Brush.linearGradient(colors = listOf(ElectricBlue, SteelBlue))
    )

    val xOutlineStyle = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = 0.sp,
        color = Color.White,
        drawStyle = Stroke(width = 3f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlack)
    ) {
        AbstractMeshBackground(modifier = Modifier.fillMaxSize())

        // Centered branding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.alpha(initialAlpha.value)
            ) {
                // "A"
                Text(
                    text = "A",
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 50.sp,
                        letterSpacing = 4.sp,
                        color = SilverHighlight
                    )
                )

                // "VATAR" expands
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandHorizontally(
                        animationSpec = tween(600),
                        expandFrom = Alignment.Start
                    ) + fadeIn(tween(700))
                ) {
                    Text(
                        text = "VATAR",
                        style = TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp,
                            letterSpacing = 4.sp,
                            color = SilverHighlight
                        ),
                        modifier = Modifier.alpha(middleAlpha.value)
                    )
                }

                // "X" — gradient fill + flickering white outline overlay
                Box(modifier = Modifier.padding(start = 2.dp)) {
                    // Base X with gradient
                    Text(text = "X", style = xGradientStyle)

                    // White outline overlay that flickers
                    if (xOutlineAlpha.value > 0f) {
                        Text(
                            text = "X",
                            style = xOutlineStyle,
                            modifier = Modifier.alpha(xOutlineAlpha.value)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(800))
            ) {
                Text(
                    text = "FASHION OPERATING SYSTEM",
                    style = MaterialTheme.typography.labelLarge,
                    color = CloudGray,
                    letterSpacing = 3.sp
                )
            }
        }

        // Bottom CTA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(bottom = navBarPadding + 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showBottom,
                enter = fadeIn(tween(600))
            ) {
                Text(
                    text = "Create a digital body profile and experience\nreal-time virtual garment fitting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showBottom,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 3 })
            ) {
                GradientButton(
                    text = "INITIALIZE SYSTEM",
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
