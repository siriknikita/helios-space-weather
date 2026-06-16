package com.helios.spaceweather.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helios.spaceweather.ui.dashboard.DashboardScreen

/**
 * App navigation graph. Two destinations: the dashboard and settings. Horizontal slide keeps
 * the spatial model simple (settings sits "to the right" of the dashboard).
 *
 * The settings destination is a placeholder here; the real screen lands with the localization
 * change so this PR stays scoped to the dashboard.
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
            SettingsPlaceholder()
        }
    }
}

/** Temporary settings stand-in; replaced by the real screen in the localization PR. */
@Composable
private fun SettingsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
    }
}
