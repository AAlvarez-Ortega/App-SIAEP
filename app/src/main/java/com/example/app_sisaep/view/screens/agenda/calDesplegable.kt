package com.example.app_sisaep.view.screens.agenda

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.DiaEscolarDto
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState

@Composable
fun CalDesplegable(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    diaEscolarActual: DiaEscolarDto?, // 🚀 OPTIMIZACIÓN: Cambiado de List a un objeto Único/Nulo
    modifier: Modifier = Modifier
) {
    val locale = remember { Locale("es", "MX") }
    val expandedState = rememberSaveable { mutableStateOf(false) }

    val shownMonthStrState = rememberSaveable {
        mutableStateOf(YearMonth.from(selectedDate).toString())
    }
    val shownMonth = remember(shownMonthStrState.value) { YearMonth.parse(shownMonthStrState.value) }

    LaunchedEffect(selectedDate) {
        val ym = YearMonth.from(selectedDate).toString()
        if (ym != shownMonthStrState.value) shownMonthStrState.value = ym
    }

    BackHandler(enabled = expandedState.value) { expandedState.value = false }

    val animMs = 240
    val dragAccum = remember { mutableFloatStateOf(0f) }
    val openThresholdPx = 70f
    val closeThresholdPx = 50f

    fun onDragStopDecide() {
        val dy = dragAccum.floatValue
        if (!expandedState.value && dy > openThresholdPx) {
            expandedState.value = true
        } else if (expandedState.value && dy < -closeThresholdPx) {
            expandedState.value = false
        }
        dragAccum.floatValue = 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(animMs))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!expandedState.value) {
                // Pasamos el día actual para pintar indicadores si hay agenda
                WeekStrip(
                    selected = selectedDate,
                    onSelect = onDateSelected,
                    locale = locale,
                    diaEscolarActual = diaEscolarActual
                )
            }

            CalendarHandleDrag(
                expanded = expandedState.value,
                onDragDelta = { deltaY -> dragAccum.floatValue += deltaY },
                onDragEnd = { onDragStopDecide() }
            )
        }

        AnimatedVisibility(
            visible = expandedState.value,
            enter = fadeIn(animationSpec = tween(animMs)),
            exit = fadeOut(animationSpec = tween(animMs))
        ) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(min = 280.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { expandedState.value = false }
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(22.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalendarHandleDrag(expanded = true, onDragDelta = { deltaY -> dragAccum.floatValue += deltaY }, onDragEnd = { onDragStopDecide() })
                        MonthHeader(shownMonth = shownMonth, locale = locale, onPrev = { shownMonthStrState.value = shownMonth.minusMonths(1).toString() }, onNext = { shownMonthStrState.value = shownMonth.plusMonths(1).toString() })
                        MonthGrid(shownMonth = shownMonth, selectedDate = selectedDate, locale = locale, onDateSelected = { onDateSelected(it); expandedState.value = false })
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(shownMonth: YearMonth, selectedDate: LocalDate, locale: Locale, onDateSelected: (LocalDate) -> Unit) {
    val daysOfWeek = remember { listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY) }
    val grid = remember(shownMonth) { buildMonthGrid(shownMonth) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        daysOfWeek.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.NARROW, locale).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(40.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        grid.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { day ->
                    val isSelected = day == selectedDate
                    val isInMonth = YearMonth.from(day) == shownMonth

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        onClick = { onDateSelected(day) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (!isInMonth) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    locale: Locale,
    diaEscolarActual: DiaEscolarDto? // 🚀 Recibido aquí
) {
    val start = remember(selected) { selected.minusDays(((selected.dayOfWeek.value % 7).toLong())) }
    val days = remember(start) { (0..6).map { start.plusDays(it.toLong()) } }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { d ->
            val isSelected = d == selected
            val dayLetter = d.dayOfWeek.getDisplayName(TextStyle.NARROW, locale).uppercase()

            // Verificamos si este día de la tira semanal coincide con el que tiene eventos cargados
            val tieneEvento = diaEscolarActual != null && d == selected

            Surface(
                modifier = Modifier.size(width = 44.dp, height = 56.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                onClick = { onSelect(d) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = dayLetter, style = MaterialTheme.typography.labelMedium, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = d.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)

                    // 🚀 INDICADOR VISUAL: Pequeño punto si el día seleccionado tiene contenido en Supabase
                    if (tieneEvento) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

private fun buildMonthGrid(ym: YearMonth): List<LocalDate> {
    val first = ym.atDay(1)
    val firstDow = first.dayOfWeek.value % 7
    val start = first.minusDays(firstDow.toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}

@Composable
private fun MonthHeader(shownMonth: YearMonth, locale: Locale, onPrev: () -> Unit, onNext: () -> Unit) {
    val monthName = remember(shownMonth) {
        shownMonth.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month)) }
        Text(text = "$monthName ${shownMonth.year}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month)) }
    }
}

@Composable
private fun CalendarHandleDrag(expanded: Boolean, onDragDelta: (Float) -> Unit, onDragEnd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .draggable(orientation = Orientation.Vertical, state = rememberDraggableState { onDragDelta(it) }, onDragStopped = { onDragEnd() })
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDragDelta(if (expanded) -100f else 100f); onDragEnd() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(if (expanded) 62.dp else 52.dp).height(6.dp).clip(RoundedCornerShape(999.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (expanded) 0.22f else 0.14f)))
    }
}

fun formatearFechaEstricta(date: LocalDate): String {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}