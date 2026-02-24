package com.example.app_sisaep.view.screens.agenda

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import kotlin.math.abs

@Composable
fun CalDesplegable(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = remember { Locale("es", "MX") }

    val expandedState = rememberSaveable { mutableStateOf(false) }

    // ✅ Guardamos YearMonth como String para evitar Saver/stateSaver
    val shownMonthStrState = rememberSaveable {
        mutableStateOf(YearMonth.from(selectedDate).toString()) // "2026-02"
    }
    val shownMonth: YearMonth = YearMonth.parse(shownMonthStrState.value)

    // Sincroniza mes cuando selectedDate cae en otro mes
    LaunchedEffect(selectedDate) {
        val ym = YearMonth.from(selectedDate).toString()
        if (ym != shownMonthStrState.value) shownMonthStrState.value = ym
    }

    // Cierra con Back cuando expandido
    BackHandler(enabled = expandedState.value) { expandedState.value = false }

    val animMs = 240

    // --- Drag logic ---
    val dragAccum = remember { mutableFloatStateOf(0f) }
    val openThresholdPx = 70f  // si arrastra hacia abajo más de esto -> abre
    val closeThresholdPx = 50f // si arrastra hacia arriba más de esto -> cierra

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
            // Week strip solo cuando NO está expandido
            AnimatedVisibility(
                visible = !expandedState.value,
                enter = slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(animMs)
                ) + fadeIn(animationSpec = tween(animMs)),
                exit = slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(animMs)
                ) + fadeOut(animationSpec = tween(animMs))
            ) {
                WeekStrip(
                    selected = selectedDate,
                    onSelect = onDateSelected,
                    locale = locale
                )
            }

            // ✅ Handle arrastrable (drag)
            CalendarHandleDrag(
                expanded = expandedState.value,
                onDragDelta = { deltaY -> dragAccum.floatValue += deltaY },
                onDragEnd = { onDragStopDecide() }
            )
        }

        // Overlay + sheet cuando expandido
        AnimatedVisibility(
            visible = expandedState.value,
            enter = fadeIn(animationSpec = tween(animMs)),
            exit = fadeOut(animationSpec = tween(animMs))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
            ) {
                // Tap fuera para cerrar
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

                // Sheet animado
                AnimatedVisibility(
                    visible = expandedState.value,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(animMs)
                    ) + fadeIn(animationSpec = tween(animMs)),
                    exit = slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(animMs)
                    ) + fadeOut(animationSpec = tween(animMs))
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()

                            .padding(top = 14.dp),
                        shape = RoundedCornerShape(22.dp),
                        tonalElevation = 6.dp,
                        shadowElevation = 10.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mini handle dentro del sheet (también drag up para cerrar)
                            CalendarHandleDrag(
                                expanded = true,
                                onDragDelta = { deltaY -> dragAccum.floatValue += deltaY },
                                onDragEnd = { onDragStopDecide() }
                            )

                            MonthHeader(
                                shownMonth = shownMonth,
                                locale = locale,
                                onPrev = { shownMonthStrState.value = shownMonth.minusMonths(1).toString() },
                                onNext = { shownMonthStrState.value = shownMonth.plusMonths(1).toString() }
                            )

                            MonthGrid(
                                shownMonth = shownMonth,
                                selectedDate = selectedDate,
                                locale = locale,
                                onDateSelected = {
                                    onDateSelected(it)
                                    // ✅ pedido: al seleccionar fecha, cerrar
                                    expandedState.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------- HANDLE DRAG ------------------- */

@Composable
private fun CalendarHandleDrag(
    expanded: Boolean,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp) // área táctil grande
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    onDragDelta(deltaY)
                },
                onDragStopped = { onDragEnd() }
            )
            .clickable( // toque simple también alterna
                indication = null,
                interactionSource = interaction
            ) {
                // si tocan sin arrastrar, toggle
                // (como el gesto principal es drag, esto es solo backup)
                onDragDelta(if (expanded) -100f else 100f)
                onDragEnd()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(if (expanded) 62.dp else 52.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (expanded) 0.22f else 0.14f
                    )
                )
        )
    }
}

/* ------------------- MONTH UI ------------------- */

@Composable
private fun MonthHeader(
    shownMonth: YearMonth,
    locale: Locale,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = shownMonth.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() }
    val title = "$monthName ${shownMonth.year}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun MonthGrid(
    shownMonth: YearMonth,
    selectedDate: LocalDate,
    locale: Locale,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = remember {
        listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
    }

    val grid = remember(shownMonth) { buildMonthGrid(shownMonth) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { dow ->
            val label = dow.getDisplayName(TextStyle.NARROW, locale).uppercase()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                maxLines = 1
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        grid.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    val isSelected = day == selectedDate
                    val isInMonth = YearMonth.from(day) == shownMonth

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                        onClick = { onDateSelected(day) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    !isInMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------- HELPERS ------------------- */

private fun buildMonthGrid(ym: YearMonth): List<LocalDate> {
    val first = ym.atDay(1)
    val firstDow = first.dayOfWeek.value % 7 // Sunday -> 0
    val start = first.minusDays(firstDow.toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}

@Composable
private fun WeekStrip(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    locale: Locale
) {
    val start = selected.minusDays(((selected.dayOfWeek.value % 7).toLong()))
    val days = remember(selected) { (0..6).map { start.plusDays(it.toLong()) } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { d ->
            val isSelected = d == selected
            val dayLetter = d.dayOfWeek.getDisplayName(TextStyle.NARROW, locale).uppercase()

            Surface(
                modifier = Modifier.size(width = 44.dp, height = 56.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                onClick = { onSelect(d) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLetter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = d.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
