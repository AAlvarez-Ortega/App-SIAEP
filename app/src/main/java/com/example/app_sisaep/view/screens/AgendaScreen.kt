package com.example.app_sisaep.view.screens

import android.util.Log
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
import androidx.compose.material3.*
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

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.model.dto.DiaEscolarDto
import com.example.app_sisaep.view.screens.agenda.formatearFechaEstricta
import com.example.app_sisaep.viewModel.consultaas.obtenerCalendarioEscolar
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos

private enum class AgendaBody { EVENTOS, HORARIO, NUEVO_EVENTO }

@Composable
fun AgendaScreen(navController: NavController) {
    var nombreReal by remember { mutableStateOf("") }

    // 🗄️ Estado para guardar únicamente la actividad de la fecha seleccionada
    var diaCoincidenteActual by remember { mutableStateOf<DiaEscolarDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) { Icon(Icons.Filled.Home, null) },
        BottomNavItem(stringResource(R.string.calendar)) { Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda)) { Icon(Icons.Filled.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes)) { Icon(Icons.Filled.School, null) },
    )

    val selectedDateStrState = rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selectedDate: LocalDate = LocalDate.parse(selectedDateStrState.value)

    val bodyState = rememberSaveable { mutableStateOf(AgendaBody.EVENTOS.name) }
    val currentBody = AgendaBody.valueOf(bodyState.value)

    // 📡 Carga única de información de usuario al iniciar la pantalla
    LaunchedEffect(Unit) {
        val usuario = obtenerMisDatos()
        if (usuario != null) {
            nombreReal = usuario.nombre
        }
    }

    // 📡 LaunchedEffect reactivo: Se dispara CADA VEZ que selectedDate cambia en la UI
    LaunchedEffect(selectedDate) {
        isLoading = true
        val fechaFormateada = formatearFechaEstricta(selectedDate)
        Log.d("SUPABASE_AGENDA", "Disparando consulta para la fecha seleccionada: $fechaFormateada")

        // Consume la nueva consulta asíncrona de dos pasos
        diaCoincidenteActual = obtenerCalendarioEscolar(fechaFormateada)

        Log.d("SUPABASE_AGENDA", "Resultado final: Encontrado?=${diaCoincidenteActual != null}, Actividad=${diaCoincidenteActual?.descripcionActividad}")
        isLoading = false
    }

    AppScaffold(
        nombreUsuario = nombreReal,
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
        onConfigClick = { navController.navigate(Routes.Config) },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        onUserClick = { navController.navigate(Routes.Perfil) },
        navItems = navItems
    ) { padding ->

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
                // Header fijo de la parte superior
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.agenda),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dayLabel • ${selectedDate.dayOfMonth} $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Calendario Desplegable ligero
                CalDesplegable(
                    selectedDate = selectedDate,
                    diasEscolares = emptyList(), // Se mantiene ligero por diseño bajo demanda
                    onDateSelected = { newDate ->
                        selectedDateStrState.value = newDate.toString()
                    }
                )

                // Renderizado y animación del cuerpo cambiante inferior
                AnimatedContent(
                    targetState = currentBody,
                    transitionSpec = {
                        val duration = 260
                        when (targetState) {
                            AgendaBody.HORARIO -> {
                                (slideInHorizontally(tween(duration)) { it } + fadeIn(tween(duration)))
                                    .togetherWith(slideOutHorizontally(tween(duration)) { -it } + fadeOut(tween(duration)))
                                    .using(SizeTransform(clip = false))
                            }
                            AgendaBody.NUEVO_EVENTO -> {
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
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { body ->
                    when (body) {
                        AgendaBody.EVENTOS -> {
                            if (isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = guindaSuave)
                                }
                            } else {
                                // 🚀 Renderiza la lista oficial pasando el estado obtenido en tiempo real
                                EventosPorDia(diaCoincidente = diaCoincidenteActual)
                            }
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
                                    bodyState.value = AgendaBody.EVENTOS.name
                                }
                            )
                        }
                    }
                }
            }

            // FAB Izquierdo: Nuevo Evento
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
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 18.dp)
            ) {
                Icon(
                    imageVector = if (currentBody == AgendaBody.NUEVO_EVENTO) Icons.Filled.ArrowBack else Icons.Filled.Add,
                    contentDescription = null
                )
            }

            // FAB Derecho: Horario
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
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
            ) {
                Icon(
                    imageVector = if (currentBody == AgendaBody.HORARIO) Icons.Filled.ArrowBack else Icons.Filled.DateRange,
                    contentDescription = null
                )
            }
        }
    }
}