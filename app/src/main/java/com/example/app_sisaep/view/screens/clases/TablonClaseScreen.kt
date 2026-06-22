package com.example.app_sisaep.view.screens.clases

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ════════════════════════════════════════════════════════════════════════════
// RUTA — agrega esto a tu Routes.kt y AppNavHost
// ════════════════════════════════════════════════════════════════════════════
//
// En Routes.kt (o donde tengas tus constantes):
//   const val ROUTE_TABLON_CLASE = "tablon_clase/{clase_id}/{clase_nombre}"
//   fun routeTablonClase(claseId: String, claseNombre: String) =
//       "tablon_clase/$claseId/${Uri.encode(claseNombre)}"
//
// En AppNavHost, dentro del bloque NavHost { ... }:
//   composable(
//       route = ROUTE_TABLON_CLASE,
//       arguments = listOf(
//           navArgument("clase_id")     { type = NavType.StringType },
//           navArgument("clase_nombre") { type = NavType.StringType }
//       )
//   ) { back ->
//       TablonClaseScreen(
//           navController = navController,
//           claseId       = back.arguments?.getString("clase_id")     ?: "",
//           claseNombre   = back.arguments?.getString("clase_nombre")  ?: ""
//       )
//   }
//
// En ClasesScreen, al hacer click en una CardMateria:
//   onClick = {
//       navController.navigate(
//           routeTablonClase(clase.id_asignatura, clase.asignatura_descripcion ?: clase.id_asignatura)
//       )
//   }
//
// ════════════════════════════════════════════════════════════════════════════

// ── Paleta ────────────────────────────────────────────────────────────────────
private val GuindaPrimario  = Color(0xFF7A003C)
private val GuindaOscuro    = Color(0xFF5C0011)
private val GuindaMedio     = Color(0xFF9B1B30)
private val GuindaSuave     = Color(0xFFF5E6ED)
private val AzulTarea       = Color(0xFF3F51B5)
private val AzulTareaSuave  = Color(0xFFE8EAF6)
private val VerdeArchivo    = Color(0xFF009688)
private val VerdeArchivoSuave = Color(0xFFE0F2F1)

private val GradienteGuinda = Brush.horizontalGradient(listOf(GuindaOscuro, GuindaMedio))

// ── Modelo dummy (reemplazar con DTOs reales cuando conectes Supabase) ────────
data class AvisoClaseItem(
    val id: String,
    val titulo: String,
    val contenido: String,
    val autor: String,
    val fecha: String,
    val tipo: TipoPublicacion
)

enum class TipoPublicacion { GENERAL, TAREA, ARCHIVO }

// ── Datos dummy de ejemplo ────────────────────────────────────────────────────
private fun avisosEjemplo() = listOf(
    AvisoClaseItem("1", "Bienvenidos al tablón",
        "Aquí el profesor publicará anuncios importantes de la clase.",
        "Profesor Martínez", "Hoy", TipoPublicacion.GENERAL),
    AvisoClaseItem("2", "Examen parcial",
        "El primer examen parcial será el próximo viernes en el horario habitual.",
        "Profesor Martínez", "Hace 2 días", TipoPublicacion.GENERAL)
)

private fun tareasEjemplo() = listOf(
    AvisoClaseItem("3", "Tarea 1 — Conjuntos",
        "Resolver ejercicios de conjuntos y relaciones del libro, páginas 12-18.",
        "Profesor Martínez", "Entrega: viernes", TipoPublicacion.TAREA),
    AvisoClaseItem("4", "Actividad — Tablas de verdad",
        "Completar tablas de verdad y equivalencias lógicas del formulario adjunto.",
        "Profesor Martínez", "Entrega: lunes", TipoPublicacion.TAREA)
)

private fun archivosEjemplo() = listOf(
    AvisoClaseItem("5", "Presentación — Unidad 1",
        "Material PDF con todos los temas vistos en clase durante la primera unidad.",
        "Profesor Martínez", "Hoy", TipoPublicacion.ARCHIVO),
    AvisoClaseItem("6", "Formulario de ejercicios",
        "Documento con ejercicios adicionales para repasar antes del examen.",
        "Profesor Martínez", "Ayer", TipoPublicacion.ARCHIVO)
)

// ── Secciones disponibles ─────────────────────────────────────────────────────
private enum class SeccionTablon(
    val etiqueta: String,
    val icono: ImageVector,
    val colorActivo: Color,
    val colorSuave: Color
) {
    GENERAL("General",  Icons.Rounded.Campaign,    GuindaPrimario, GuindaSuave),
    TAREAS( "Tareas",   Icons.Rounded.Assignment,  AzulTarea,      AzulTareaSuave),
    ARCHIVOS("Archivos",Icons.Rounded.Folder,       VerdeArchivo,   VerdeArchivoSuave)
}

// ═════════════════════════════════════════════════════════════════════════════
// PANTALLA PRINCIPAL
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablonClaseScreen(
    navController: NavController,
    claseId: String,
    claseNombre: String
) {
    var seccionActual by remember { mutableStateOf(SeccionTablon.GENERAL) }

    // Cuando conectes Supabase, reemplaza estos mapas con tu estado real
    val contenidoPorSeccion = remember {
        mapOf(
            SeccionTablon.GENERAL  to avisosEjemplo(),
            SeccionTablon.TAREAS   to tareasEjemplo(),
            SeccionTablon.ARCHIVOS to archivosEjemplo()
        )
    }

    val itemsActuales = contenidoPorSeccion[seccionActual] ?: emptyList()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBarTablon(
                claseNombre   = claseNombre,
                seccionActual = seccionActual,
                onBack        = { navController.popBackStack() }
            )
        },
        bottomBar = {
            TabsTablon(
                seccionActual      = seccionActual,
                conteos            = contenidoPorSeccion.mapValues { it.value.size },
                onSeleccionar      = { seccionActual = it }
            )
        },
        floatingActionButton = {
            // FAB oculto por ahora — descomenta y conecta rutas cuando estén listas
            // FabPublicar(seccion = seccionActual, claseId = claseId, claseNombre = claseNombre, navController = navController)
        }
    ) { padding ->

        AnimatedContent(
            targetState = itemsActuales,
            transitionSpec = {
                fadeIn(tween(220)) togetherWith fadeOut(tween(150))
            },
            label = "ContenidoTablon",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { lista ->
            if (lista.isEmpty()) {
                EstadoVacioTablon(seccion = seccionActual)
            } else {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(lista, key = { it.id }) { item ->
                        CardPublicacion(item = item)
                    }
                }
            }
        }
    }
}

// ── TopBar con título animado ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarTablon(
    claseNombre: String,
    seccionActual: SeccionTablon,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GradienteGuinda)
    ) {
        TopAppBar(
            modifier     = Modifier.statusBarsPadding(),
            windowInsets = WindowInsets(0.dp),
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Título animado al cambiar sección
                    AnimatedContent(
                        targetState = when (seccionActual) {
                            SeccionTablon.GENERAL  -> "Tablón de anuncios"
                            SeccionTablon.TAREAS   -> "Actividades y entregas"
                            SeccionTablon.ARCHIVOS -> "Materiales de clase"
                        },
                        transitionSpec = {
                            slideInVertically(tween(200)) { -it } + fadeIn(tween(200)) togetherWith
                                    slideOutVertically(tween(150)) { it } + fadeOut(tween(150))
                        },
                        label = "TituloSeccion"
                    ) { titulo ->
                        Text(
                            text       = titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 20.sp,
                            color      = Color.White,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text  = claseNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.80f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Regresar",
                        tint               = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ── Tabs inferiores con contador ──────────────────────────────────────────────
@Composable
private fun TabsTablon(
    seccionActual: SeccionTablon,
    conteos: Map<SeccionTablon, Int>,
    onSeleccionar: (SeccionTablon) -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        tonalElevation  = 8.dp,
        shadowElevation = 8.dp,
        shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SeccionTablon.entries.forEach { seccion ->
                val seleccionada = seccion == seccionActual
                val conteo       = conteos[seccion] ?: 0

                FilledTonalButton(
                    onClick           = { onSeleccionar(seccion) },
                    shape             = RoundedCornerShape(50),
                    contentPadding    = PaddingValues(
                        horizontal = if (seleccionada) 16.dp else 14.dp,
                        vertical   = 10.dp
                    ),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (seleccionada) seccion.colorSuave else Color.Transparent,
                        contentColor   = if (seleccionada) seccion.colorActivo
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector        = seccion.icono,
                        contentDescription = seccion.etiqueta,
                        modifier           = Modifier.size(22.dp)
                    )
                    AnimatedVisibility(visible = seleccionada) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = seccion.etiqueta,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                            // Contador de items — la firma visual de esta pantalla
                            if (conteo > 0) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(seccion.colorActivo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text     = "$conteo",
                                        color    = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Card de publicación ───────────────────────────────────────────────────────
@Composable
private fun CardPublicacion(item: AvisoClaseItem) {
    val (colorPrimario, colorSuave, icono) = when (item.tipo) {
        TipoPublicacion.GENERAL  -> Triple(GuindaPrimario, GuindaSuave,        Icons.Rounded.Campaign)
        TipoPublicacion.TAREA    -> Triple(AzulTarea,      AzulTareaSuave,     Icons.Rounded.Assignment)
        TipoPublicacion.ARCHIVO  -> Triple(VerdeArchivo,   VerdeArchivoSuave,  Icons.Rounded.Description)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header de color por tipo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorSuave)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorPrimario.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = icono,
                            contentDescription = null,
                            tint               = colorPrimario,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text       = item.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = colorPrimario,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
            }

            // Cuerpo
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text      = item.contenido,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                // Footer: chip de tipo + autor • fecha
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorSuave
                    ) {
                        Text(
                            text      = item.tipo.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color     = colorPrimario,
                            fontWeight = FontWeight.SemiBold,
                            fontSize  = 12.sp,
                            modifier  = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Text(
                        text  = "${item.autor} · ${item.fecha}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun EstadoVacioTablon(seccion: SeccionTablon) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = seccion.icono,
                contentDescription = null,
                tint               = seccion.colorActivo.copy(alpha = 0.3f),
                modifier           = Modifier.size(64.dp)
            )
            Text(
                text       = "Sin ${seccion.etiqueta.lowercase()} aún",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "Las publicaciones aparecerán aquí",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}