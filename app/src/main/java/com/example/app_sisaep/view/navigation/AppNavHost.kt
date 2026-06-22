package com.example.app_sisaep.view.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.app_sisaep.view.screens.*
import com.example.app_sisaep.view.screens.clases.TablonClaseScreen
import com.example.app_sisaep.view.screens.clases.sectionChat.IndividualChatScreen
import com.example.app_sisaep.view.screens.perfilUsuario.PerfilUsuarioScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    NavHost(
        navController    = navController,
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
            ConfigScreen(
                navController    = navController,
                darkMode         = darkMode,
                onDarkModeChange = onDarkModeChange
            )
        }

        composable(Routes.Perfil) {
            PerfilUsuarioScreen(navController = navController)
        }

        // ── Chat individual ───────────────────────────────────────────────────
        composable(
            route     = "chat/{userId}/{userName}",
            arguments = listOf(
                navArgument("userId")   { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { back ->
            IndividualChatScreen(
                navController = navController,
                receiverId    = back.arguments?.getString("userId")   ?: "",
                receiverName  = back.arguments?.getString("userName") ?: "Usuario"
            )
        }

        // ── Tablón de la clase ────────────────────────────────────────────────
        composable(
            route     = Routes.TablonDeclase,
            arguments = listOf(
                navArgument("clase_id")     { type = NavType.StringType },
                navArgument("clase_nombre") { type = NavType.StringType }
            )
        ) { back ->
            TablonClaseScreen(
                navController = navController,
                claseId       = back.arguments?.getString("clase_id")     ?: "",
                claseNombre   = Uri.decode(back.arguments?.getString("clase_nombre") ?: "")
            )
        }

    }
}