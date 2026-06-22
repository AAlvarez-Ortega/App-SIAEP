package com.example.app_sisaep.view.screens.agenda

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.HoraClaseDto
import com.example.app_sisaep.viewModel.consultaas.obtenerHorarioAcademico
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

// ── Constantes de diseño extraídas del composable para no recrearlas en cada recomposición ──
private val GuindaSuave = Color(0xFF8A1F4D)
private val ColoresPaleta = listOf(
    Color(0xFF3F51B5),
    Color(0xFF009688),
    Color(0xFF03A9F4),
    Color(0xFFE53935)
)
private val ColumnasDias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")

// ── Algoritmo de fusión extraído como función pura (no depende de Compose) ──
private fun fusionarHorario(lista: List<HoraClaseDto>): List<HoraClaseDto> {
    return lista
        .groupBy { (it.nombre_dia?.trim()?.uppercase() ?: "") to it.id_asignatura }
        .mapNotNull { (_, subLista) ->
            val ordenada = subLista.sortedBy { it.id_horas }
            ordenada.firstOrNull()?.copy()?.also { base ->
                base.hora_inicio_fusionada = ordenada.first().ini_horas
                base.hora_fin_fusionada = ordenada.last().fin_horas
            }
        }
}

// ── Función pura para asignar colores por id_asignatura ──
private fun asignarColores(lista: List<HoraClaseDto>): Map<String, Color> {
    val idsUnicos = lista.map { it.id_asignatura }.distinct()
    return idsUnicos.associateWith { id ->
        ColoresPaleta[idsUnicos.indexOf(id) % ColoresPaleta.size]
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    var listaHorarioReal by remember { mutableStateOf<List<HoraClaseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var asignaturaSeleccionada by remember { mutableStateOf<HoraClaseDto?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            listaHorarioReal = obtenerHorarioAcademico()
        } catch (_: Exception) { /* mantiene lista vacía */ }
        isLoading = false
    }

    // Derivadas memorizadas con la lista como key correcta
    val horarioFusionado = remember(listaHorarioReal) { fusionarHorario(listaHorarioReal) }
    val coloresAsignados = remember(listaHorarioReal) { asignarColores(listaHorarioReal) }

    // Renglones únicos de tiempo, ordenados por id_horas del primer bloque que los define
    val renglonesHoras = remember(horarioFusionado) {
        horarioFusionado
            .distinctBy { it.hora_inicio_fusionada to it.hora_fin_fusionada }
            .sortedBy { it.id_horas }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.school_schedule),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        when {
            isLoading -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GuindaSuave)
            }

            horarioFusionado.isEmpty() -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes asignaturas cargadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // Header de días
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        Spacer(modifier = Modifier.width(65.dp))
                        ColumnasDias.forEach { dia ->
                            Text(
                                text = dia,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(68.dp)
                            )
                        }
                    }

                    // Filas de horas
                    renglonesHoras.forEach { bloqueRenglon ->
                        FilaHorario(
                            bloqueRenglon = bloqueRenglon,
                            horarioFusionado = horarioFusionado,
                            coloresAsignados = coloresAsignados,
                            onCeldaClick = { clase ->
                                asignaturaSeleccionada = clase
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet
    if (showBottomSheet && asignaturaSeleccionada != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DetalleAsignaturaSheet(
                detalle = asignaturaSeleccionada!!,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

// ── Fila completa de una franja horaria ──
@Composable
private fun FilaHorario(
    bloqueRenglon: HoraClaseDto,
    horarioFusionado: List<HoraClaseDto>,
    coloresAsignados: Map<String, Color>,
    onCeldaClick: (HoraClaseDto) -> Unit
) {
    val textoRango = "${bloqueRenglon.hora_inicio_fusionada ?: ""}-\n${bloqueRenglon.hora_fin_fusionada ?: ""}"

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = textoRango,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 12.sp,
            modifier = Modifier.width(65.dp)
        )

        ColumnasDias.forEach { nombreDia ->
            val clase = horarioFusionado.firstOrNull { c ->
                c.nombre_dia?.trim()?.uppercase() == nombreDia.uppercase() &&
                        c.hora_inicio_fusionada == bloqueRenglon.hora_inicio_fusionada &&
                        c.hora_fin_fusionada == bloqueRenglon.hora_fin_fusionada
            }

            if (clase != null) {
                CeldaClase(
                    clase = clase,
                    color = coloresAsignados[clase.id_asignatura] ?: GuindaSuave,
                    onClick = { onCeldaClick(clase) }
                )
            } else {
                CeldaVacia()
            }
        }
    }
}

// ── Celda con clase (extrae el estado isPressed fuera del forEach) ──
@Composable
private fun CeldaClase(
    clase: HoraClaseDto,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "CeldaScale"
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(width = 60.dp, height = 60.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(color, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White)
            ) {
                scope.launch {
                    isPressed = true
                    delay(80)
                    isPressed = false
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clase.asignatura_abreviatura ?: clase.id_asignatura,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ── Celda vacía ──
@Composable
private fun CeldaVacia() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(width = 60.dp, height = 60.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    )
}

// ── Contenido del Bottom Sheet ──
@Composable
private fun DetalleAsignaturaSheet(
    detalle: HoraClaseDto,
    onDismiss: () -> Unit
) {
    var triggerAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggerAnimation = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Detalles de la Asignatura",
            style = MaterialTheme.typography.titleMedium,
            color = GuindaSuave,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        AnimatedVisibility(
            visible = triggerAnimation,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ItemDetalleRow("Nombre Completo:", detalle.asignatura_descripcion ?: "N/A")
                ItemDetalleRow("Clave:", detalle.id_asignatura)
                ItemDetalleRow("Secuencia / Turno:", "${detalle.id_secuencia} (${detalle.turno ?: "N/A"})")
                ItemDetalleRow(
                    lbl = "Ubicación:",
                    valor = "${detalle.edificio_nombre ?: "Edif ${detalle.id_edificio}"} • Salón: ${detalle.numero_salon ?: detalle.id_salones}"
                )
                ItemDetalleRow(
                    lbl = "Horario de Bloque Completo:",
                    valor = "${detalle.nombre_dia} de ${detalle.hora_inicio_fusionada} a ${detalle.hora_fin_fusionada} hrs"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = GuindaSuave),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Entendido", color = Color.White)
        }
    }
}

@Composable
fun ItemDetalleRow(lbl: String, valor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = lbl,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}