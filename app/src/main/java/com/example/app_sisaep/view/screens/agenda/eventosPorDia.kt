package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.model.dto.DiaEscolarDto
import com.example.app_sisaep.model.dto.EventoIdUsuarioDto
import com.example.app_sisaep.model.dto.HoraClaseDto

@Composable
fun EventosPorDia(
    diaCoincidente: DiaEscolarDto?,              // 📥 Día oficial de la escuela
    eventosPersonales: List<EventoIdUsuarioDto>, // 🚀 Lista de eventos personales
    materiasDelDia: List<HoraClaseDto>,          // 🚀 Materias filtradas del día seleccionado
    modifier: Modifier = Modifier
) {
    val guindaInstitucional = Color(0xFF8A1F4D)

    // 🔥 ALGORITMO DE FUSIÓN: Agrupamos las horas continuas de la misma asignatura en este día
    val materiasFusionadas = remember(materiasDelDia) {
        val listaResultado = mutableListOf<HoraClaseDto>()

        // Agrupamos únicamente por el id de la asignatura (ya que todas pertenecen al mismo día seleccionado)
        val agrupadosPorMateria = materiasDelDia.groupBy { it.id_asignatura }

        agrupadosPorMateria.forEach { (_, subListaMateria) ->
            // Ordenamos por el bloque para asegurar orden cronológico continuo
            val subListaOrdenada = subListaMateria.sortedBy { it.id_horas }
            if (subListaOrdenada.isNotEmpty()) {
                val moldeBase = subListaOrdenada.first().copy()

                // Extraemos el inicio del primer bloque y el fin del último bloque consecutivo
                moldeBase.hora_inicio_fusionada = subListaOrdenada.first().ini_horas
                moldeBase.hora_fin_fusionada = subListaOrdenada.last().fin_horas

                listaResultado.add(moldeBase)
            }
        }
        // Las ordenamos finalmente por su horario de inicio para que aparezcan en orden secuencial en el feed
        listaResultado.sortedBy { it.id_horas }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Espacio libre para los FAB inferiores
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
                            else  -> guindaInstitucional
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

        // --- 2. RENDERIZADO DE LAS MATERIAS ACADÉMICAS FUSIONADAS ---
        if (materiasFusionadas.isNotEmpty()) {
            item {
                Text(
                    text = "Clases de Hoy",
                    style = MaterialTheme.typography.labelLarge,
                    color = guindaInstitucional,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                )
            }

            items(materiasFusionadas, key = { "${it.id_asignatura}_${it.id_horas}" }) { clase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color distintivo para el apartado académico de la agenda
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(50.dp)
                                .background(Color(0xFF3F51B5), RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = clase.asignatura_descripcion ?: clase.id_asignatura,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Secuencia: ${clase.id_secuencia} • Edificio: ${clase.edificio_siglas ?: clase.id_edificio.toString()} • Salón: ${clase.numero_salon ?: clase.id_salones.toString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Muestra el Rango Horario consolidado (Ej: 15:00 - 16:30)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = clase.hora_inicio_fusionada ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
                            )
                            Text(
                                text = clase.hora_fin_fusionada ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. RENDERIZADO DE EVENTOS PERSONALES DEL USUARIO ---
        if (eventosPersonales.isNotEmpty()) {
            item {
                Text(
                    text = "Eventos Personales",
                    style = MaterialTheme.typography.labelLarge,
                    color = guindaInstitucional,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, top = 6.dp)
                )
            }

            items(eventosPersonales, key = { it.idEvento }) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(58.dp)
                                .background(guindaInstitucional, RoundedCornerShape(4.dp))
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

                        Text(
                            text = evento.obtenerHoraInicioFormateada(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = guindaInstitucional,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // --- 4. ESTADO VACÍO ACTUALIZADO (Aplica si de plano no hay ninguna de las tres fuentes) ---
        if (diaCoincidente == null && eventosPersonales.isEmpty() && materiasFusionadas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
                            text = "No hay actividades ni clases programadas para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}