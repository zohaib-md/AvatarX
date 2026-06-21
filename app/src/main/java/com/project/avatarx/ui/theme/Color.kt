package com.project.avatarx.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core backgrounds - Deep Obsidian
val NightBlack = Color(0xFF0A0A0A)
val OnyxSurface = Color(0xFF121212)
val GraphiteSurface = Color(0xFF1A1A1A)

// Glass effects - Sharp industrial glass
val SlateGlass = Color(0x0AFFFFFF)
val SlateGlassBorder = Color(0x1AFFFFFF)

// Primary accent - Signature Orange-Red
val ElectricBlue = Color(0xFFE8450A) // Keeping the variable name for compatibility
val SteelBlue = Color(0xFFFF5722)
val IceBlue = Color(0xFFFFCCBC)

// Neutrals
val SilverHighlight = Color(0xFFF5F5F7)

// Status colors
val MintSuccess = Color(0xFF34C759)
val AmberWarn = Color(0xFFFF9F0A)
val CoralAlert = Color(0xFFFF453A)

// Grays
val CloudGray = Color(0xFF8E8E93)
val StoneGray = Color(0xFF636366)
val DimGray = Color(0xFF3A3A3C)

// Accent colors
val DeepNavy = Color(0xFF7A1F00)
val IndigoAccent = Color(0xFFFF6D3A)
val TealScan = Color(0xFF30D158)

// Gradient brushes
val PrimaryGradient = Brush.linearGradient(colors = listOf(ElectricBlue, DeepNavy))
val AccentGradient = Brush.linearGradient(colors = listOf(ElectricBlue, IndigoAccent))
val ScanGradient = Brush.linearGradient(colors = listOf(ElectricBlue, MintSuccess))
val ConfidenceGradient = Brush.linearGradient(colors = listOf(MintSuccess, ElectricBlue))
val SurfaceGradient = Brush.linearGradient(colors = listOf(OnyxSurface, NightBlack))