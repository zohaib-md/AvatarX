package com.project.avatarx.domain.model

import androidx.annotation.DrawableRes

data class Garment(
    val id: String,
    val name: String,
    val category: String,
    val fitType: String,
    @DrawableRes val imageResId: Int? = null,
    @DrawableRes val overlayResId: Int? = null,
    val localImagePath: String? = null,
    val fitCompatibility: FitCompatibility? = null
)
