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
import androidx.compose.material.ripple.rememberRipple
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val guindaSuave = Color(0xFF8A1F4D)
    val columnasDias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")

    val mapaColoresAsignaturas = remember {
        listOf(
            Color(0xFF3F51B5), Color(0xFF009688), Color(0xFF03A9F4), Color(0xFFE53935)
        )
    }

    var listaHorarioReal by remember { mutableStateOf<List<HoraClaseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var asignaturaSeleccionada by remember { mutableStateOf<HoraClaseDto?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            listaHorarioReal = obtenerHorarioAcademico()
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    // Algoritmo de fusión
    val horarioFusionado = remember(listaHorarioReal) {
        val listaResultado = mutableListOf<HoraClaseDto>()
        val agrupadosPorDiaYAsignatura = listaHorarioReal.groupBy {
            (it.nombre_dia?.trim()?.uppercase() ?: "") to it.id_asignatura
        }

        agrupadosPorDiaYAsignatura.forEach { (_, subListaMateria) ->
            val subListaOrdenada = subListaMateria.sortedBy { it.id_horas }
            if (subListaOrdenada.isNotEmpty()) {
                val moldeBase = subListaOrdenada.first().copy()
                moldeBase.hora_inicio_fusionada = subListaOrdenada.first().ini_horas
                moldeBase.hora_fin_fusionada = subListaOrdenada.last().fin_horas
                listaResultado.add(moldeBase)
            }
        }
        listaResultado
    }

    val renglonesHorasDinamicas = remember(horarioFusionado) {
        horarioFusionado.distinctBy { (it.hora_inicio_fusionada to it.hora_fin_fusionada) }
            .sortedBy { it.id_horas }
    }

    val coloresAsignados = remember(listaHorarioReal) {
        val unicas = listaHorarioReal.map { it.id_asignatura }.distinct()
        unicas.associateWith { id ->
            val index = unicas.indexOf(id) % mapaColoresAsignaturas.size
            mapaColoresAsignaturas[index]
        }
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

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = guindaSuave)
            }
        } else if (horarioFusionado.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No tienes asignaturas cargadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // --- HEADER DÍAS ---
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        Spacer(modifier = Modifier.width(65.dp))
                        columnasDias.forEach { nombreDia ->
                            Text(
                                text = nombreDia,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(68.dp)
                            )
                        }
                    }

                    // --- FILAS DE HORAS ---
                    renglonesHorasDinamicas.forEach { bloqueRenglon ->
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

                            columnasDias.forEach { nombreDiaUI ->
                                val claseCoincidente = horarioFusionado.firstOrNull { clase ->
                                    val diaBD = clase.nombre_dia?.trim()?.uppercase() ?: ""
                                    val diaUI = nombreDiaUI.trim().uppercase()

                                    diaBD == diaUI &&
                                            clase.hora_inicio_fusionada == bloqueRenglon.hora_inicio_fusionada &&
                                            clase.hora_fin_fusionada == bloqueRenglon.hora_fin_fusionada
                                }

                                if (claseCoincidente != null) {
                                    val colorCelda = coloresAsignados[claseCoincidente.id_asignatura] ?: guindaSuave

                                    // 🚀 ANIMACIÓN A: Estado reactivo de escala al dar click
                                    var isPressed by remember { mutableStateOf(false) }
                                    val scaleAnimated by animateFloatAsState(
                                        targetValue = if (isPressed) 0.92f else 1f,
                                        animationSpec = tween(durationMillis = 100),
                                        label = "CeldaScale"
                                    )
                                    val coroutineScope = rememberCoroutineScope()

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(width = 60.dp, height = 60.dp)
                                            .graphicsLayer(scaleX = scaleAnimated, scaleY = scaleAnimated)
                                            .background(colorCelda, RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, color = Color.White)
                                            ) {
                                                coroutineScope.launch {
                                                    isPressed = true
                                                    delay(80) // Delay mini para que el usuario aprecie el hundimiento
                                                    isPressed = false
                                                    asignaturaSeleccionada = claseCoincidente
                                                    showBottomSheet = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = claseCoincidente.asignatura_abreviatura ?: claseCoincidente.id_asignatura,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
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
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET CON ANIMACIÓN SECUENCIAL INTERNA ---
    if (showBottomSheet && asignaturaSeleccionada != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val detalle = asignaturaSeleccionada!!

            // Estado interno para gatillar la animación de cascada de los renglones
            var triggerContentAnimation by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                triggerContentAnimation = true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 42.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Detalles de la Asignatura",
                    style = MaterialTheme.typography.titleMedium,
                    color = guindaSuave,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // 🚀 ANIMACIÓN B: Entrada escalonada de la información de la clase
                AnimatedVisibility(
                    visible = triggerContentAnimation,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 2 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ItemDetalleRow(lbl = "Nombre Completo:", valor = detalle.asignatura_descripcion ?: "N/A")
                        ItemDetalleRow(lbl = "Clave:", valor = detalle.id_asignatura)
                        ItemDetalleRow(lbl = "Secuencia / Turno:", valor = "${detalle.id_secuencia} (${detalle.turno ?: "N/A"})")
                        ItemDetalleRow(
                            lbl = "Ubicación:",
                            valor = "${detalle.edificio_nombre ?: "Edif ${detalle.id_edificio}"} • Salón: ${detalle.numero_salon ?: detalle.id_salones.toString()}"
                        )
                        ItemDetalleRow(
                            lbl = "Horario de Bloque Completo:",
                            valor = "${detalle.nombre_dia} de ${detalle.hora_inicio_fusionada} a ${detalle.hora_fin_fusionada} hrs"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showBottomSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = guindaSuave),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Entendido", color = Color.White)
                }
            }
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