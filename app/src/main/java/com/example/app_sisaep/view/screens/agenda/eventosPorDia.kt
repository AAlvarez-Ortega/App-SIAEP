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

import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

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
            title = stringResource(R.string.agenda_empty_title),
            subtitle = stringResource(R.string.agenda_empty_subtitle),
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
    context: android.content.Context,
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
            title = context.getString(R.string.demo_event_1_title),
            location = context.getString(R.string.demo_event_1_location),
            note = context.getString(R.string.demo_event_1_note),
            typeLabel = context.getString(R.string.demo_event_1_type),
            badgeColor = primary
        ),
        AgendaEventUi(
            id = "2",
            date = today,
            startTime = "13:30",
            title = context.getString(R.string.demo_event_2_title),
            location = context.getString(R.string.demo_event_2_location),
            note = context.getString(R.string.demo_event_2_note),
            typeLabel = context.getString(R.string.demo_event_2_type),
            badgeColor = secondary
        ),
        AgendaEventUi(
            id = "3",
            date = tomorrow,
            startTime = "10:15",
            title = context.getString(R.string.demo_event_3_title),
            location = context.getString(R.string.demo_event_3_location),
            note = context.getString(R.string.demo_event_3_note),
            typeLabel = context.getString(R.string.demo_event_3_type),
            badgeColor = tertiary
        )
    )
}
