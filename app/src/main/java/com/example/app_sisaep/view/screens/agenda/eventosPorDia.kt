package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate

data class AgendaEventUi(
    val id: String,
    val date: LocalDate,
    val startTime: String,
    val title: String,
    val location: String,
    val note: String,
    val typeLabel: String,
    val badgeColor: androidx.compose.ui.graphics.Color
)

@Composable
fun EventosPorDia(
    selectedDate: LocalDate,
    allEvents: List<AgendaEventUi>,
    modifier: Modifier = Modifier,
    onEventClick: (AgendaEventUi) -> Unit = {}
) {
    val filtered = remember(selectedDate, allEvents) {
        allEvents.filter { it.date == selectedDate }.sortedBy { it.startTime }
    }

    if (filtered.isEmpty()) {
        EmptyAgendaState(
            title = "No tienes eventos para este día",
            subtitle = "Cuando tengas clases, asesorías o recordatorios, aparecerán aquí.",
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            items(filtered, key = { it.id }) { ev ->
                AgendaEventCard(ev = ev, onClick = { onEventClick(ev) })
            }
        }
    }
}

@Composable
private fun AgendaEventCard(
    ev: AgendaEventUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ev.badgeColor)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ev.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${ev.startTime} • ${ev.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (ev.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = ev.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            AssistChip(
                onClick = onClick,
                label = { Text(ev.typeLabel) }
            )
        }
    }
}

@Composable
private fun EmptyAgendaState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun demoAgendaEvents(
    primary: androidx.compose.ui.graphics.Color,
    secondary: androidx.compose.ui.graphics.Color,
    tertiary: androidx.compose.ui.graphics.Color
): List<AgendaEventUi> {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    return listOf(
        AgendaEventUi(
            id = "1",
            date = today,
            startTime = "09:00",
            title = "Clase: Ingeniería de Software",
            location = "Aula 203",
            note = "Llevar avance del documento y rúbrica.",
            typeLabel = "Clase",
            badgeColor = primary
        ),
        AgendaEventUi(
            id = "2",
            date = today,
            startTime = "13:30",
            title = "Asesoría: Proyecto SAES",
            location = "Laboratorio",
            note = "Revisar Agenda + Calendario.",
            typeLabel = "Asesoría",
            badgeColor = secondary
        ),
        AgendaEventUi(
            id = "3",
            date = tomorrow,
            startTime = "10:15",
            title = "Entrega: Evidencia Unidad 2",
            location = "Plataforma",
            note = "Subir PDF y capturas.",
            typeLabel = "Entrega",
            badgeColor = tertiary
        )
    )
}
