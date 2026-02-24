package com.example.app_sisaep.view.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.agenda.CalDesplegable
import com.example.app_sisaep.view.screens.agenda.EventosPorDia
import com.example.app_sisaep.view.screens.agenda.HorarioContent
import com.example.app_sisaep.view.screens.agenda.NuevoEventoContent
import com.example.app_sisaep.view.screens.agenda.demoAgendaEvents
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private enum class AgendaBody { EVENTOS, HORARIO, NUEVO_EVENTO }

@Composable
fun AgendaScreen(navController: NavController) {

    val navItems = listOf(
        BottomNavItem("Inicio") { Icon(Icons.Filled.Home, null) },
        BottomNavItem("Calendario") { Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem("Agenda") { Icon(Icons.Filled.Schedule, null) },
        BottomNavItem("Clases") { Icon(Icons.Filled.School, null) },
    )

    // LocalDate como String para evitar Saver/stateSaver
    val selectedDateStrState = rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selectedDate: LocalDate = LocalDate.parse(selectedDateStrState.value)

    // Estado del contenido inferior (3 vistas)
    val bodyState = rememberSaveable { mutableStateOf(AgendaBody.EVENTOS.name) }
    val currentBody = AgendaBody.valueOf(bodyState.value)

    AppScaffold(
        selectedIndex = 2,
        onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }
        },
        onGenerateQrClick = { navController.navigate(Routes.GenerarQR) },
        onReadQrClick = { navController.navigate(Routes.ScanQR) },
        topBarLogoRes = R.drawable.ipn,
        onConfigClick = { navController.navigate("config") },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        navItems = navItems
    ) { padding ->

        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val tertiary = MaterialTheme.colorScheme.tertiary

        val events = remember(primary, secondary, tertiary) {
            demoAgendaEvents(primary, secondary, tertiary)
        }

        val locale = remember { Locale("es", "MX") }
        val dayLabel = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }
        val monthLabel = selectedDate.month.getDisplayName(TextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }

        val guindaSuave = Color(0xFF8A1F4D)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header fijo
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Agenda",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dayLabel • ${selectedDate.dayOfMonth} $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Calendario SIEMPRE visible
                CalDesplegable(
                    selectedDate = selectedDate,
                    onDateSelected = { newDate ->
                        selectedDateStrState.value = newDate.toString()
                    }
                )

                // Solo cambia el cuerpo
                AnimatedContent(
                    targetState = currentBody,
                    transitionSpec = {
                        val duration = 260

                        when (targetState) {
                            AgendaBody.HORARIO -> {
                                // EVENTOS -> HORARIO (horizontal)
                                (slideInHorizontally(tween(duration)) { it } + fadeIn(tween(duration)))
                                    .togetherWith(slideOutHorizontally(tween(duration)) { -it } + fadeOut(tween(duration)))
                                    .using(SizeTransform(clip = false))
                            }

                            AgendaBody.NUEVO_EVENTO -> {
                                // EVENTOS/HORARIO -> NUEVO_EVENTO (vertical + “a la izquierda”)
                                // Entra desde abajo y con leve empuje a la izquierda
                                (slideInVertically(tween(duration)) { it } +
                                        slideInHorizontally(tween(duration)) { -it / 6 } +
                                        fadeIn(tween(duration)))
                                    .togetherWith(
                                        slideOutVertically(tween(duration)) { -it / 3 } +
                                                slideOutHorizontally(tween(duration)) { -it / 6 } +
                                                fadeOut(tween(duration))
                                    )
                                    .using(SizeTransform(clip = false))
                            }

                            AgendaBody.EVENTOS -> {
                                // Volver a EVENTOS:
                                // si vienes de HORARIO -> regresa horizontal
                                // si vienes de NUEVO_EVENTO -> regresa vertical inverso (sube)
                                if (initialState == AgendaBody.NUEVO_EVENTO) {
                                    (slideInVertically(tween(duration)) { -it } + fadeIn(tween(duration)))
                                        .togetherWith(slideOutVertically(tween(duration)) { it } + fadeOut(tween(duration)))
                                        .using(SizeTransform(clip = false))
                                } else {
                                    (slideInHorizontally(tween(duration)) { -it } + fadeIn(tween(duration)))
                                        .togetherWith(slideOutHorizontally(tween(duration)) { it } + fadeOut(tween(duration)))
                                        .using(SizeTransform(clip = false))
                                }
                            }
                        }
                    },
                    label = "AgendaBodyPager",
                    modifier = Modifier.fillMaxSize()
                ) { body ->
                    when (body) {
                        AgendaBody.EVENTOS -> {
                            EventosPorDia(
                                selectedDate = selectedDate,
                                allEvents = events,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AgendaBody.HORARIO -> {
                            HorarioContent(
                                selectedDate = selectedDate,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AgendaBody.NUEVO_EVENTO -> {
                            NuevoEventoContent(
                                selectedDate = selectedDate,
                                modifier = Modifier.fillMaxSize(),
                                onCancel = { bodyState.value = AgendaBody.EVENTOS.name },
                                onSaveSimulated = {
                                    // aquí después conectamos a Supabase
                                }
                            )
                        }
                    }
                }
            }

            // ✅ FAB Izquierdo: Nuevo Evento
            FloatingActionButton(
                onClick = {
                    bodyState.value = if (currentBody == AgendaBody.NUEVO_EVENTO) {
                        AgendaBody.EVENTOS.name
                    } else {
                        AgendaBody.NUEVO_EVENTO.name
                    }
                },
                containerColor = guindaSuave,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 18.dp)
            ) {
                if (currentBody == AgendaBody.NUEVO_EVENTO) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver a eventos")
                } else {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo evento")
                }
            }

            // ✅ FAB Derecho: Horario
            FloatingActionButton(
                onClick = {
                    bodyState.value = if (currentBody == AgendaBody.HORARIO) {
                        AgendaBody.EVENTOS.name
                    } else {
                        AgendaBody.HORARIO.name
                    }
                },
                containerColor = guindaSuave,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
            ) {
                if (currentBody == AgendaBody.HORARIO) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver a eventos")
                } else {
                    Icon(Icons.Filled.DateRange, contentDescription = "Ver horario")
                }
            }
        }
    }
}
