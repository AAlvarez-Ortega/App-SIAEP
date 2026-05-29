package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.model.dto.DiaEscolarDto
import com.example.app_sisaep.model.dto.EventoIdUsuarioDto

@Composable
fun EventosPorDia(
    diaCoincidente: DiaEscolarDto?,           // 📥 Día oficial de la escuela
    eventosPersonales: List<EventoIdUsuarioDto>, // 🚀 NUEVO: Lista de eventos del usuario (Ya ordenados desc)
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Espacio para que los FAB no tapen el último elemento
    ) {
        // --- 1. RENDERIZADO DEL CALENDARIO OFICIAL (Si existe) ---
        if (diaCoincidente != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colorIndicador = when (diaCoincidente.id_tipodias) {
                            1, 10 -> Color(0xFF4CAF50)  // Inscripción (Verde)
                            2, 3  -> Color(0xFF2196F3)  // Inicio Periodo (Azul)
                            4     -> Color(0xFFF44336)  // Fin Periodo (Rojo)
                            13    -> Color(0xFFFFEB3B)  // Vacaciones (Amarillo)
                            14, 15 -> Color(0xFF9C27B0) // Asuetos (Morado)
                            else  -> Color(0xFF8A1F4D)   // Guinda institucional
                        }

                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(54.dp)
                                .background(colorIndicador, RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = diaCoincidente.descripcionActividad,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Calendario Oficial • Actividad Tipo ${diaCoincidente.id_tipodias}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. RENDERIZADO DE EVENTOS PERSONALES DEL USUARIO ---
        if (eventosPersonales.isNotEmpty()) {
            items(eventosPersonales, key = { it.idEvento }) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(

                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Barra indicadora color Guinda Institucional para eventos creados por el Alumno
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(58.dp)
                                .background(Color(0xFF8A1F4D), RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = evento.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!evento.lugar.isNullOrEmpty()) {
                                Text(
                                    text = "📍 ${evento.lugar}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!evento.notas.isNullOrEmpty()) {
                                Text(
                                    text = evento.notas,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Muestra la Hora formateada al extremo derecho
                        Text(
                            text = evento.obtenerHoraInicioFormateada(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A1F4D),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // --- 3. ESTADO VACÍO (Si el día no tiene absolutamente nada de nada) ---
        if (diaCoincidente == null && eventosPersonales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay actividades programadas para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}