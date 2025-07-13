package com.amp.appliverytest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amp.appliverytest.screens.MainScreen
import screens.AppDetailsScreen
import screens.DeviceInfoScreen
import screens.InstalledAppsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            MainScreen(
                onNavigateToDeviceInfo = {
                    navController.navigate(AppDestinations.DEVICE_INFO)
                },
                onNavigateToInstalledApps = {
                    navController.navigate(AppDestinations.INSTALLED_APPS)
                }
            )
        }
        
        composable(AppDestinations.DEVICE_INFO) {
            DeviceInfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(AppDestinations.INSTALLED_APPS) {
            InstalledAppsScreen(
                onNavigateToAppDetails = { packageName ->
                    navController.navigate("${AppDestinations.APP_DETAILS}/$packageName")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("${AppDestinations.APP_DETAILS}/{packageName}") { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppDetailsScreen(
                packageName = packageName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

object AppDestinations {
    const val HOME = "home"
    const val DEVICE_INFO = "device-info"
    const val INSTALLED_APPS = "installed-apps"
    const val APP_DETAILS = "app-details"
}