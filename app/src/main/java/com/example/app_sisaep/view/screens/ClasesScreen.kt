package com.example.app_sisaep.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.HoraClaseDto
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.navigation.Routes.routeTablonClase
import com.example.app_sisaep.viewModel.consultaas.obtenerHorarioAcademico
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos

// ── Paleta institucional ───────────────────────────────────────────────────────
private val GuindaPrimario = Color(0xFF7A003C)
private val GuindaSuave    = Color(0xFFF5E6ED)

// Paleta de colores para distinguir materias visualmente
private val ColoresMaterias = listOf(
    Color(0xFF3F51B5), // Azul índigo
    Color(0xFF009688), // Verde teal
    Color(0xFF03A9F4), // Azul cielo
    Color(0xFFE53935), // Rojo
    Color(0xFF7B1FA2), // Morado
    Color(0xFF00897B), // Verde oscuro
    Color(0xFF1976D2), // Azul
    Color(0xFFF57C00), // Naranja
)

@Composable
fun ClasesScreen(navController: NavController) {

    var nombreReal      by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(true) }
    var horario         by remember { mutableStateOf<List<HoraClaseDto>>(emptyList()) }
    var secuenciaActual by remember { mutableStateOf<String?>(null) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home))      { Icon(Icons.Rounded.Home, null) },
        BottomNavItem(stringResource(R.string.calendar))  { Icon(Icons.Rounded.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda))    { Icon(Icons.Rounded.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes))   { Icon(Icons.Rounded.School, null) },
    )

    // Carga única: usuario + horario real
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val usuario = obtenerMisDatos()
            if (usuario != null) {
                nombreReal = usuario.nombre
            }
            val listaCompleta = obtenerHorarioAcademico()
            horario = listaCompleta

            // Auto-selecciona la secuencia del usuario logueado (primer bloque encontrado)
            secuenciaActual = listaCompleta.firstOrNull()?.id_secuencia
        } catch (_: Exception) {
            // Sin red — listas vacías, la UI lo maneja
        } finally {
            isLoading = false
        }
    }

    // Materias únicas del horario (una Card por asignatura, sin duplicados por hora)
    val materiasUnicas = remember(horario) {
        horario.distinctBy { it.id_asignatura }
    }

    // Mapa de color por asignatura (consistente entre recomposiciones)
    val coloresPorAsignatura = remember(materiasUnicas) {
        materiasUnicas.mapIndexed { idx, clase ->
            clase.id_asignatura to ColoresMaterias[idx % ColoresMaterias.size]
        }.toMap()
    }

    AppScaffold(
        nombreUsuario = nombreReal,
        selectedIndex = 3,
        onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }
        },
        onGenerateQrClick = { navController.navigate(Routes.GenerarQR) },
        onReadQrClick     = { navController.navigate(Routes.ScanQR) },
        onConfigClick     = { navController.navigate("config") },
        onLogoutClick     = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        onUserClick = { navController.navigate(Routes.Perfil) },
        navItems    = navItems
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Cabecera ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = "Mis Clases",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    // Secuencia auto-detectada como chip informativo
                    if (secuenciaActual != null) {
                        ChipSecuencia(secuencia = secuenciaActual!!)
                    }
                }

                // Contador de materias
                if (!isLoading && materiasUnicas.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GuindaSuave
                    ) {
                        Text(
                            text      = "${materiasUnicas.size} materias",
                            style     = MaterialTheme.typography.labelMedium,
                            color     = GuindaPrimario,
                            fontWeight = FontWeight.SemiBold,
                            modifier  = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ── Cuerpo ────────────────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = GuindaPrimario)
                            Text(
                                text  = "Cargando tus materias…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                materiasUnicas.isEmpty() -> {
                    EstadoVacioClases()
                }

                else -> {
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn(),
                        exit    = fadeOut()
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding      = PaddingValues(bottom = 80.dp)
                        ) {
                            items(materiasUnicas, key = { it.id_asignatura }) { clase ->
                                CardMateria(
                                    clase  = clase,
                                    color  = coloresPorAsignatura[clase.id_asignatura] ?: GuindaPrimario,
                                    onClick = {
                                        navController.navigate(routeTablonClase(clase.id_asignatura, clase.asignatura_descripcion ?: clase.id_asignatura))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Card de materia ───────────────────────────────────────────────────────────
@Composable
private fun CardMateria(
    clase: HoraClaseDto,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar circular con inicial de la materia
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = (clase.asignatura_abreviatura
                        ?: clase.asignatura_descripcion
                        ?: clase.id_asignatura)
                        .take(2)
                        .uppercase(),
                    color      = color,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
            }

            // Info de la materia
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = clase.asignatura_descripcion ?: clase.id_asignatura,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Edificio + salón
                    val ubicacion = buildString {
                        val edif = clase.edificio_siglas ?: clase.edificio_nombre
                        ?: "Edif. ${clase.id_edificio}"
                        val salon = clase.numero_salon ?: clase.id_salones.toString()
                        append("$edif • Salón $salon")
                    }
                    Icon(
                        imageVector        = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(12.dp)
                    )
                    Text(
                        text  = ubicacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Secuencia + turno
                val meta = buildString {
                    append("Sec. ${clase.id_secuencia}")
                    if (!clase.turno.isNullOrBlank()) append(" • ${clase.turno}")
                }
                Text(
                    text  = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Flecha + indicador de chat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = "Ir al chat",
                    tint               = color,
                    modifier           = Modifier.size(20.dp)
                )
                Icon(
                    imageVector        = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Chip informativo de secuencia ─────────────────────────────────────────────
@Composable
private fun ChipSecuencia(secuencia: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GuindaPrimario.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.Group,
                contentDescription = null,
                tint               = GuindaPrimario,
                modifier           = Modifier.size(12.dp)
            )
            Text(
                text       = secuencia,
                style      = MaterialTheme.typography.labelSmall,
                color      = GuindaPrimario,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun EstadoVacioClases() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.School,
                contentDescription = null,
                tint               = GuindaPrimario.copy(alpha = 0.35f),
                modifier           = Modifier.size(64.dp)
            )
            Text(
                text       = "Sin materias asignadas",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "Cuando tengas materias registradas aparecerán aquí",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}