package com.example.app_sisaep.view.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.noticias.InicioNoticias

@Composable
fun HomeScreen(navController: NavController) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) {
            androidx.compose.material3.Icon(Icons.Filled.Home, contentDescription = null)
        },
        BottomNavItem(stringResource(R.string.calendar)) {
            androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        },
        BottomNavItem(stringResource(R.string.agenda)) {
            androidx.compose.material3.Icon(Icons.Filled.Schedule, contentDescription = null)
        },
        BottomNavItem(stringResource(R.string.classes)) {
            androidx.compose.material3.Icon(Icons.Filled.School, contentDescription = null)
        }
    )

    AppScaffold(
        selectedIndex = selectedIndex,
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
        topBarLogoRes = R.drawable.ipn,
        onConfigClick = {
            navController.navigate("config")
        },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        navItems = navItems
    ) { innerPadding ->
        InicioNoticias(
            modifier = Modifier.padding(innerPadding)
        )
    }
}