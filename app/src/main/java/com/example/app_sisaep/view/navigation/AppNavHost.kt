package com.example.app_sisaep.view.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app_sisaep.view.screens.*
import com.example.app_sisaep.view.screens.avsos.AvisosGlobalesListener



@Composable
fun AppNavHost(navController: NavHostController) {

    Box {

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

            composable(Routes.Calendario) {
                CalendarioScreen(navController = navController)
            }

            composable(Routes.Agenda) {
                AgendaScreen(navController = navController)
            }

            composable(Routes.Clases) {
                ClasesScreen(navController = navController)
            }

            composable(Routes.GenerarQR) {
                GenerarQrScreen(navController = navController)
            }

            composable(Routes.ScanQR) {
                ScanQrScreen(navController = navController)
            }

            composable(Routes.Config) {
                ConfigScreen(navController = navController)
            }
        }

            AvisosGlobalesListener()


    }
}