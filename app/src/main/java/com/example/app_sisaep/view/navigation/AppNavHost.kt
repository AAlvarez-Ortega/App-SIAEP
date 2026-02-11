package com.example.app_sisaep.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app_sisaep.view.screens.HomeScreen
import com.example.app_sisaep.view.screens.LoginScreen
import com.example.app_sisaep.view.screens.PreRegistroScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            LoginScreen(navController = navController)
        }
        composable(Routes.PreRegistro) {
            PreRegistroScreen(navController = navController)
        }
        composable(Routes.Home) {
            HomeScreen(navController = navController)
        }
    }
}
