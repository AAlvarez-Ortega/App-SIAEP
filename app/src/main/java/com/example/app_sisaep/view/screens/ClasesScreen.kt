package com.example.app_sisaep.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes

import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.view.screens.clases.sectionChat.SectionChat
import com.example.app_sisaep.view.screens.clases.SectionClases
import com.example.app_sisaep.view.screens.clases.SectionGrupos


@Composable
fun ClasesScreen(navController: NavController) {
                var selectedIndex by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
        BottomNavItem(stringResource(R.string.calendar)) { androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda)) { androidx.compose.material3.Icon(Icons.Filled.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes)) { androidx.compose.material3.Icon(Icons.Filled.School, null) },
    )

                AppScaffold(
                    selectedIndex = 3,
                    onItemSelected = { index ->
                        selectedIndex = index
                        when (index) {
                            0 -> navController.navigate(Routes.Home)
                            1 -> navController.navigate(Routes.Calendario)
                            2 -> navController.navigate(Routes.Agenda)
                            3 -> navController.navigate(Routes.Clases)
                        }
                    },
                    onGenerateQrClick = {
                        navController.navigate(Routes.GenerarQR)
                    },
                    onReadQrClick = {
                        navController.navigate(Routes.ScanQR)
                    },
                    onConfigClick = {
                        navController.navigate("config")
                    },
                    onLogoutClick = {
                        navController.navigate(Routes.Login) {
                            popUpTo(0) // limpia TODO el backstack
                            launchSingleTop = true
                        }
                    },

                    navItems = navItems
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Dentro de ClasesScreen.kt
                        SectionClases()
                        SectionGrupos()
                        SectionChat(navController = navController)
                    }
                }
}









