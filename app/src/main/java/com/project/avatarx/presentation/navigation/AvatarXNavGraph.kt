package com.project.avatarx.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project.avatarx.domain.model.BodyMeasurements
import com.project.avatarx.presentation.screens.avatarprofile.AvatarProfileScreen
import com.project.avatarx.presentation.screens.bodyscan.BodyScanScreen
import com.project.avatarx.presentation.screens.fitanalysis.FitAnalysisScreen
import com.project.avatarx.presentation.screens.garmentselection.GarmentSelectionScreen
import com.project.avatarx.presentation.screens.garmentscan.GarmentScanScreen
import com.project.avatarx.presentation.screens.splash.SplashScreen
import com.project.avatarx.presentation.screens.virtualtryon.VirtualTryOnScreen
import com.project.avatarx.presentation.screens.welcome.WelcomeScreen

import android.graphics.Bitmap
import com.project.avatarx.domain.model.NormalizedLandmark

object MeasurementsHolder {
    var measurements: BodyMeasurements = BodyMeasurements()
    var capturedImage: Bitmap? = null
    var capturedLandmarks: List<NormalizedLandmark>? = null
}

@Composable
fun AvatarXNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onStartScan = { navController.navigate(Screen.BodyScan.route) }
            )
        }

        composable(route = Screen.BodyScan.route) {
            BodyScanScreen(
                onScanComplete = { measurements, bitmap, landmarks ->
                    MeasurementsHolder.measurements = measurements
                    MeasurementsHolder.capturedImage = bitmap
                    MeasurementsHolder.capturedLandmarks = landmarks
                    navController.navigate(Screen.AvatarProfile.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AvatarProfile.route) {
            AvatarProfileScreen(
                measurements = MeasurementsHolder.measurements,
                capturedBitmap = MeasurementsHolder.capturedImage,
                capturedLandmarks = MeasurementsHolder.capturedLandmarks,
                onSelectGarment = { navController.navigate(Screen.GarmentSelection.route) }
            )
        }

        composable(route = Screen.GarmentSelection.route) {
            GarmentSelectionScreen(
                measurements = MeasurementsHolder.measurements,
                onTryOn = { garmentId -> navController.navigate(Screen.VirtualTryOn.createRoute(garmentId)) },
                onScanGarment = { navController.navigate(Screen.GarmentScan.route) }
            )
        }

        composable(route = Screen.GarmentScan.route) {
            GarmentScanScreen(
                onScanSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VirtualTryOn.route,
            arguments = listOf(navArgument("garmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val garmentId = backStackEntry.arguments?.getString("garmentId") ?: ""
            VirtualTryOnScreen(
                garmentId = garmentId,
                onAnalyzeFit = { id -> navController.navigate(Screen.FitAnalysis.createRoute(id)) },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FitAnalysis.route,
            arguments = listOf(navArgument("garmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val garmentId = backStackEntry.arguments?.getString("garmentId") ?: ""
            FitAnalysisScreen(
                garmentId = garmentId,
                measurements = MeasurementsHolder.measurements,
                capturedBodyImage = MeasurementsHolder.capturedImage,
                onTryAnother = { navController.popBackStack(Screen.GarmentSelection.route, inclusive = false) },
                onDone = { navController.popBackStack(Screen.Welcome.route, inclusive = false) }
            )
        }
    }
}
