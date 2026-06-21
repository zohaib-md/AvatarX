package com.project.avatarx.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object BodyScan : Screen("body_scan")
    object AvatarProfile : Screen("avatar_profile")
    object GarmentSelection : Screen("garment_selection")
    object GarmentScan : Screen("garment_scan")
    object VirtualTryOn : Screen("virtual_tryon/{garmentId}") {
        fun createRoute(garmentId: String) = "virtual_tryon/$garmentId"
    }
    object FitAnalysis : Screen("fit_analysis/{garmentId}") {
        fun createRoute(garmentId: String) = "fit_analysis/$garmentId"
    }
}
