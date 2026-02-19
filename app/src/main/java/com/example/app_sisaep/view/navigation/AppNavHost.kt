package com.example.app_sisaep.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app_sisaep.view.screens.*

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {

        // 🔐 Auth
        composable(Routes.Login) {
            LoginScreen(navController = navController)
        }

        composable(Routes.PreRegistro) {
            PreRegistroScreen(navController = navController)
        }

        // 🏠 Principales
        composable(Routes.Home) {
            HomeScreen(navController = navController)
        }

        composable(Routes.Calendario) {
            CalendarioScreen(navController = navController)
        }

        composable(Routes.Agenda) {
            AgendaScreen(navController = navController)
        }

        composable(Routes.Clases) {
            ClasesScreen(navController = navController)
        }

        // 📱 QR
        composable(Routes.GenerarQR) {
            GenerarQrScreen(navController = navController)
        }

        composable(Routes.ScanQR) {
            ScanQrScreen(navController = navController)
        }

        // ⚙️ Config
        composable(Routes.Config) {
            ConfigScreen(navController = navController)
        }
    }
}
