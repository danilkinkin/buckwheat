package com.danilkinkin.buckwheat.calendar

import android.app.usage.UsageEvents
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.danilkinkin.buckwheat.calendar.model.CalendarUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun DayOfWeekHeading(day: String, modifier: Modifier = Modifier) {
    DayContainer(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            text = day,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W700),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
        )
    }
}

@Composable
private fun DayContainer(
    modifier: Modifier = Modifier,
    current: Boolean = false,
    onSelect: () -> Unit = { },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .background(Color.Transparent,
            )
            .pointerInput(Any()) {
                detectTapGestures {
                    onSelect()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (current) {
            Box(
                modifier = modifier
                    .height(CELL_SIZE - 8.dp)
                    .width(CELL_SIZE - 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
            ) {
                content()
            }
        } else {
            content()
        }

    }
}

@Composable
internal fun Day(
    day: LocalDate,
    calendarState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    month: YearMonth,
    modifier: Modifier = Modifier
) {
    val disabled = calendarState.isBeforeCurrentDay(day)
    val selected = calendarState.isDateInSelectedPeriod(day)
    val current = calendarState.isCurrentDay(day)

    DayContainer(
        modifier = modifier
            .semantics {
                text = AnnotatedString(
                    "${
                        month.month.name.lowercase().capitalize(Locale.current)
                    } ${day.dayOfMonth} ${month.year}"
                )
                dayStatusProperty = selected
            },
        current = current,
        onSelect = { if (!disabled) onDayClicked(day) },
    ) {

        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                // Parent will handle semantics
                .clearAndSetSemantics {},
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W800),
            color = when (true) {
                current -> MaterialTheme.colorScheme.onPrimaryContainer
                selected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = if (disabled) 0.3F else 1F)
            },
        )
    }
}

val DayStatusKey = SemanticsPropertyKey<Boolean>("DayStatusKey")
var SemanticsPropertyReceiver.dayStatusProperty by DayStatusKey
