package com.helios.spaceweather.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helios.spaceweather.ui.dashboard.DashboardScreen
import com.helios.spaceweather.ui.settings.SettingsScreen

/**
 * App navigation graph. Two destinations: the dashboard and settings. Horizontal slide keeps
 * the spatial model simple (settings sits "to the right" of the dashboard).
 */
@Composable
fun HeliosNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250))
        },
    ) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
