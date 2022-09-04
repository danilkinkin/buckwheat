package com.danilkinkin.buckwheat.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.danilkinkin.buckwheat.calendar.model.CalendarState
import com.danilkinkin.buckwheat.calendar.model.CalendarUiState
import com.danilkinkin.buckwheat.calendar.model.Week
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate


@Composable
fun WeekSelectionPill(
    week: Week,
    currentWeekStart: LocalDate,
    state: CalendarUiState,
    modifier: Modifier = Modifier,
    widthPerDay: Dp = 48.dp,
    heightPerDay: Dp = 48.dp,
    pillColor: Color = MaterialTheme.colorScheme.primary
) {
    val widthPerDayPx = with(LocalDensity.current) { widthPerDay.toPx() }
    val heightPerDayPx = with(LocalDensity.current) { heightPerDay.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { 24.dp.toPx() }

    Canvas(
        modifier = modifier.fillMaxWidth(),
        onDraw = {
            val (offset, size) = getOffsetAndSize(
                this.size.width,
                state,
                currentWeekStart,
                week,
                widthPerDayPx,
                heightPerDayPx,
                cornerRadiusPx,
            )

            drawRoundRect(
                color = pillColor,
                topLeft = offset,
                size = Size(size, heightPerDayPx),
                cornerRadius = CornerRadius(cornerRadiusPx)
            )
        }
    )
}

private fun getOffsetAndSize(
    width: Float,
    state: CalendarUiState,
    currentWeekStart: LocalDate,
    week: Week,
    widthPerDayPx: Float,
    heightPerDayPx: Float,
    cornerRadiusPx: Float,
): Pair<Offset, Float> {
    val numberDaysSelected = state.getNumberSelectedDaysInWeek(currentWeekStart, week.yearMonth)
    val monthOverlapDelay = state.monthOverlapSelectionDelay(currentWeekStart, week)
    val dayDelay = state.dayDelay(currentWeekStart)
    val edgePadding = (width - widthPerDayPx * CalendarState.DAYS_IN_WEEK + ((widthPerDayPx - heightPerDayPx)) / 2F) + 1

    val sideSize = edgePadding + cornerRadiusPx

    val leftSize =
        if (state.isLeftHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f
    val rightSize =
        if (state.isRightHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f

    var totalSize = (numberDaysSelected * widthPerDayPx) + (leftSize + rightSize) - ((widthPerDayPx - heightPerDayPx))
    if (dayDelay + monthOverlapDelay == 0 && numberDaysSelected >= 1) {
        totalSize = totalSize.coerceAtLeast(heightPerDayPx)
    }

    totalSize = totalSize.coerceAtLeast(0F)

    val startOffset = state.selectedStartOffset(currentWeekStart, week.yearMonth) * widthPerDayPx

    val offset = Offset(startOffset + edgePadding - leftSize, 0f)

    return offset to totalSize
}
