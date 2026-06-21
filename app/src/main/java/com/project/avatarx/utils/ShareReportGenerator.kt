package com.project.avatarx.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.project.avatarx.R
import com.project.avatarx.presentation.screens.avatarprofile.AvatarProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareReportGenerator {

    suspend fun generateAndShare(
        context: Context,
        uiState: AvatarProfileUiState,
        twinBitmap: Bitmap?,
        shareToWhatsApp: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Load fonts safely
            val interBold = ResourcesCompat.getFont(context, R.font.inter_bold) ?: Typeface.DEFAULT_BOLD
            val spaceMonoBold = ResourcesCompat.getFont(context, R.font.space_mono_bold) ?: Typeface.MONOSPACE
            val spaceMonoReg = ResourcesCompat.getFont(context, R.font.space_mono_regular) ?: Typeface.MONOSPACE

            // Colors
            val nightBlack = Color.parseColor("#0A0A0A")
            val onyxSurface = Color.parseColor("#121212")
            val orangeRed = Color.parseColor("#E8450A")
            val steelOrange = Color.parseColor("#FF5722")
            val silverHighlight = Color.parseColor("#F5F5F7")
            val cloudGray = Color.parseColor("#8E8E93")

            // 1. Background
            canvas.drawColor(nightBlack)

            // 2. Subtle Glow Background
            val bgPaint = Paint()
            bgPaint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(onyxSurface, nightBlack, Color.parseColor("#1A0702")),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
            bgPaint.shader = null

            // 3. Inner Border (Glass effect)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeRed
                style = Paint.Style.STROKE
                strokeWidth = 3f
                alpha = 60
            }
            canvas.drawRoundRect(40f, 40f, width - 40f, height - 40f, 30f, 30f, borderPaint)

            // Corner Accents (Tech UI lines)
            val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeRed
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            val len = 60f
            // Top Left
            canvas.drawLine(40f, 40f, 40f + len, 40f, cornerPaint)
            canvas.drawLine(40f, 40f, 40f, 40f + len, cornerPaint)
            // Top Right
            canvas.drawLine(width - 40f, 40f, width - 40f - len, 40f, cornerPaint)
            canvas.drawLine(width - 40f, 40f, width - 40f, 40f + len, cornerPaint)
            // Bottom Left
            canvas.drawLine(40f, height - 40f, 40f + len, height - 40f, cornerPaint)
            canvas.drawLine(40f, height - 40f, 40f, height - 40f - len, cornerPaint)
            // Bottom Right
            canvas.drawLine(width - 40f, height - 40f, width - 40f - len, height - 40f, cornerPaint)
            canvas.drawLine(width - 40f, height - 40f, width - 40f, height - 40f - len, cornerPaint)

            // Text Paints
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = silverHighlight
                textSize = 96f
                typeface = interBold
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeRed
                textSize = 34f
                typeface = spaceMonoBold
                letterSpacing = 0.3f
                textAlign = Paint.Align.CENTER
            }

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = cloudGray
                textSize = 32f
                typeface = spaceMonoReg
                letterSpacing = 0.15f
                textAlign = Paint.Align.CENTER
            }

            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = silverHighlight
                textSize = 52f
                typeface = interBold
                textAlign = Paint.Align.CENTER
            }

            val sizeLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = cloudGray
                textSize = 32f
                typeface = spaceMonoReg
                letterSpacing = 0.15f
                textAlign = Paint.Align.CENTER
            }

            val sizePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = steelOrange
                textSize = 140f
                typeface = interBold
                textAlign = Paint.Align.CENTER
            }

            // Draw Brand Header
            val avatarWidth = titlePaint.measureText("AVATAR")
            val xWidth = titlePaint.measureText("X")
            val totalWidth = avatarWidth + xWidth
            val startX = (width - totalWidth) / 2f
            
            titlePaint.textAlign = Paint.Align.LEFT
            canvas.drawText("AVATAR", startX, 260f, titlePaint)
            
            val redTitlePaint = Paint(titlePaint).apply { 
                color = orangeRed 
            }
            canvas.drawText("X", startX + avatarWidth, 260f, redTitlePaint)

            canvas.drawText("DIGITAL TWIN PROFILE", width / 2f, 350f, subtitlePaint)

            // Draw Image (Twin)
            val imageSize = 650f
            val imageTop = 460f
            val rect = RectF((width - imageSize) / 2, imageTop, (width + imageSize) / 2, imageTop + imageSize)
            
            if (twinBitmap != null) {
                val imageBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(orangeRed, nightBlack),
                        null, Shader.TileMode.CLAMP
                    )
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawRoundRect(rect, 40f, 40f, imageBorderPaint)
                
                val scaledTwin = Bitmap.createScaledBitmap(twinBitmap, imageSize.toInt(), imageSize.toInt(), true)
                canvas.drawBitmap(scaledTwin, rect.left, rect.top, null)
            }

            // Draw Measurements Frame
            val dataTop = 1320f
            
            canvas.drawText("HEIGHT", width * 0.25f, dataTop, labelPaint)
            canvas.drawText("${uiState.measurements.heightCm.toInt()}cm", width * 0.25f, dataTop + 70f, valuePaint)
            
            canvas.drawText("SHOULDERS", width * 0.5f, dataTop, labelPaint)
            canvas.drawText("${uiState.measurements.shoulderWidthCm.toInt()}cm", width * 0.5f, dataTop + 70f, valuePaint)
            
            canvas.drawText("HIPS", width * 0.75f, dataTop, labelPaint)
            canvas.drawText("${uiState.measurements.hipWidthCm.toInt()}cm", width * 0.75f, dataTop + 70f, valuePaint)

            // Draw Separator Line
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1AFFFFFF")
                strokeWidth = 2f
            }
            canvas.drawLine(150f, dataTop + 140f, width - 150f, dataTop + 140f, linePaint)

            // Draw DNA
            val dnaTop = 1580f
            
            canvas.drawText("BODY TYPE", width * 0.30f, dnaTop, sizeLabelPaint)
            canvas.drawText(uiState.fashionDNA.bodyType.name, width * 0.30f, dnaTop + 70f, valuePaint)

            canvas.drawText("IDEAL SIZE", width * 0.75f, dnaTop, sizeLabelPaint)
            canvas.drawText(uiState.fashionDNA.recommendedSize.name, width * 0.75f, dnaTop + 130f, sizePaint)

            // Footer
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#4B4B4D")
                textSize = 24f
                typeface = spaceMonoReg
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            canvas.drawText("GENERATED BY AVATARX FASHION OS", width / 2f, 1840f, footerPaint)

            // Save and Share
            shareImage(context, bitmap, shareToWhatsApp)
        }
    }

    private fun shareImage(context: Context, bitmap: Bitmap, shareToWhatsApp: Boolean) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "avatarx_scan_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val text = "Check out my digital body scan from AvatarX! 🚀👕"

            if (shareToWhatsApp) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, text)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Fallback to regular share if WhatsApp is not installed
                    launchGenericShare(context, uri, text)
                }
            } else {
                launchGenericShare(context, uri, text)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchGenericShare(context: Context, uri: Uri, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Scan Report"))
    }
}
