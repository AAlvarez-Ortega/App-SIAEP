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

// ── Constantes de color reutilizables ──────────────────────────────────────────
private val GuindaInstitucional = Color(0xFF8A1F4D)
private val AzulAcademico       = Color(0xFF3F51B5)

// ── Mapa de colores por tipo de día (sin recrear en cada recomposición) ────────
private fun colorParaTipoDia(idTipo: Int): Color = when (idTipo) {
    1, 10    -> Color(0xFF4CAF50)  // Inscripción (Verde)
    2, 3     -> Color(0xFF2196F3)  // Inicio Periodo (Azul)
    4        -> Color(0xFFF44336)  // Fin Periodo (Rojo)
    13       -> Color(0xFFFFEB3B)  // Vacaciones (Amarillo)
    14, 15   -> Color(0xFF9C27B0)  // Asuetos (Morado)
    else     -> GuindaInstitucional
}

// ── Algoritmo de fusión extraído como función pura ─────────────────────────────
private fun fusionarMaterias(materiasDelDia: List<HoraClaseDto>): List<HoraClaseDto> =
    materiasDelDia
        .groupBy { it.id_asignatura }
        .mapNotNull { (_, subLista) ->
            val ordenada = subLista.sortedBy { it.id_horas }
            ordenada.firstOrNull()?.copy()?.also { base ->
                base.hora_inicio_fusionada = ordenada.first().ini_horas
                base.hora_fin_fusionada    = ordenada.last().fin_horas
            }
        }
        .sortedBy { it.id_horas }

// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun EventosPorDia(
    diaCoincidente: DiaEscolarDto?,
    eventosPersonales: List<EventoIdUsuarioDto>,
    materiasDelDia: List<HoraClaseDto>,
    modifier: Modifier = Modifier
) {
    val materiasFusionadas = remember(materiasDelDia) { fusionarMaterias(materiasDelDia) }

    val hayContenido = diaCoincidente != null ||
            eventosPersonales.isNotEmpty() ||
            materiasFusionadas.isNotEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {

        // 1 ── Día oficial del calendario escolar ──────────────────────────────
        if (diaCoincidente != null) {
            item(key = "dia_oficial") {
                CardDiaOficial(dia = diaCoincidente)
            }
        }

        // 2 ── Materias académicas fusionadas ──────────────────────────────────
        if (materiasFusionadas.isNotEmpty()) {
            item(key = "header_clases") {
                SeccionHeader(texto = "Clases de Hoy")
            }
            items(materiasFusionadas, key = { "${it.id_asignatura}_${it.id_horas}" }) { clase ->
                CardClase(clase = clase)
            }
        }

        // 3 ── Eventos personales del usuario ──────────────────────────────────
        if (eventosPersonales.isNotEmpty()) {
            item(key = "header_eventos") {
                SeccionHeader(texto = "Eventos Personales")
            }
            items(eventosPersonales, key = { it.idEvento }) { evento ->
                CardEvento(evento = evento)
            }
        }

        // 4 ── Estado vacío ────────────────────────────────────────────────────
        if (!hayContenido) {
            item(key = "estado_vacio") {
                CardVacia()
            }
        }
    }
}

// ── Componentes privados ───────────────────────────────────────────────────────

@Composable
private fun SeccionHeader(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = GuindaInstitucional,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 6.dp, top = 4.dp)
    )
}

@Composable
private fun BarraIndicador(color: Color, height: Int = 54) {
    Box(
        modifier = Modifier
            .width(6.dp)
            .height(height.dp)
            .background(color, RoundedCornerShape(4.dp))
    )
}

@Composable
private fun CardDiaOficial(dia: DiaEscolarDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarraIndicador(color = colorParaTipoDia(dia.id_tipodias))
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = dia.descripcionActividad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Calendario Oficial • Actividad Tipo ${dia.id_tipodias}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CardClase(clase: HoraClaseDto) {
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
            BarraIndicador(color = AzulAcademico, height = 50)
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
                    text = "Secuencia: ${clase.id_secuencia} • " +
                            "Edificio: ${clase.edificio_siglas ?: clase.id_edificio} • " +
                            "Salón: ${clase.numero_salon ?: clase.id_salones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = clase.hora_inicio_fusionada ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulAcademico
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

@Composable
private fun CardEvento(evento: EventoIdUsuarioDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarraIndicador(color = GuindaInstitucional, height = 58)
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
                color = GuindaInstitucional,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun CardVacia() {
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