package com.danilkinkin.buckwheat.finishPeriod

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.datePicker.CELL_SIZE
import com.danilkinkin.buckwheat.base.datePicker.WeekSelectionPill
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarUiState
import com.danilkinkin.buckwheat.base.datePicker.model.Month
import com.danilkinkin.buckwheat.base.datePicker.model.Week
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.ui.typography
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.getWeek
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.prettyWeekDay
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.util.toPalette
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.Date

data class SpendingDay(
    val date: Date,
    val spends: List<Transaction>,
    val budget: BigDecimal,
    val spending: BigDecimal,
)

@Composable
fun SpendsCalendar(
    modifier: Modifier = Modifier,
    budget: BigDecimal,
    transactions: List<Transaction>,
    startDate: Date,
    finishDate: Date,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val localDensity = LocalDensity.current

    val dayWidth = remember { mutableStateOf(CELL_SIZE) }

    val spendingDays = remember(transactions) {
        val days: MutableMap<LocalDate, SpendingDay> = emptyMap<LocalDate, SpendingDay>().toMutableMap()
        var currDay: SpendingDay? = null

        transactions.forEach {
            if (it.type == TransactionType.INCOME) {
                return@forEach
            }

            if (currDay == null || !isSameDay(currDay!!.date.time, it.date.time)) {
                if (currDay !== null) {
                    days[currDay!!.date.toLocalDate()] = currDay!!
                }

                if (it.type == TransactionType.SET_DAILY_BUDGET) {
                    currDay = SpendingDay(
                        date = it.date,
                        spends = listOf(),
                        spending = BigDecimal.ZERO,
                        budget = it.value,
                    )

                    return@forEach
                }

                currDay = SpendingDay(
                    date = it.date,
                    spends = listOf(it),
                    spending = it.value,
                    budget = currDay?.budget ?: BigDecimal.ZERO,
                )

                return@forEach
            }

            currDay = currDay!!.copy(
                spending = currDay!!.spending + it.value,
                spends = currDay!!.spends.plus(it)
            )
        }

        if (currDay != null) {
            days[currDay!!.date.toLocalDate()] = currDay!!
        }

        days.toMutableMap()
    }

    Log.d("SpendsCalendar", "spendingDays: $spendingDays")
    Log.d("SpendsCalendar", "transactions: $transactions")

    val calendarState by remember() {
        mutableStateOf(CalendarState(
            context = context,
            disableBeforeDate = startDate,
            disableAfterDate = finishDate,
        ))
    }

    Column(
        modifier = modifier
            .onGloballyPositioned {
                dayWidth.value = with(localDensity) { it.size.width.toDp() / 7 }
            },
    ) {
        calendarState.listMonths.forEach { month ->
            ItemsCalendarMonth(
                calendarState.calendarUiState.value,
                {},
                month,
                dayWidth.value,
                spendingDays,
            )
        }
    }
}

@Composable
private fun ItemsCalendarMonth(
    calendarUiState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    month: Month,
    dayWidth: Dp,
    spendingDays: Map<LocalDate, SpendingDay>,
) {
    MonthHeader(
        modifier = Modifier.padding(top = 16.dp),
        yearMonth = month.yearMonth
    )

    // Expanding width and centering horizontally
    val contentModifier = Modifier.fillMaxWidth()

    DaysOfWeek(modifier = contentModifier)

    // A custom key needs to be given to these items so that they can be found in tests that
    // need scrolling. The format of the key is ${year/month/weekNumber}. Thus,
    // the key for the fourth week of December 2020 is "2020/12/4"
    month.weeks.forEach { week ->
        val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
        val currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek()[0]))

        if (
            currentDay.plusDays(6).isBefore(calendarUiState.disabledBefore) ||
            currentDay.isAfter(calendarUiState.disabledAfter)
        ) return@forEach

        Week(
            calendarUiState = calendarUiState,
            modifier = contentModifier,
            week = week,
            onDayClicked = onDayClicked,
            spendingDays = spendingDays,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun MonthHeader(modifier: Modifier = Modifier, yearMonth: YearMonth) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f),
            text = prettyYearMonth(yearMonth),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
internal fun DaysOfWeek(modifier: Modifier = Modifier) {
    val week = getWeek()

    Row(modifier = modifier) {
        for (day in week) {
            DayOfWeekHeading(
                day = prettyWeekDay(day),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun Week(
    calendarUiState: CalendarUiState,
    week: Week,
    onDayClicked: (LocalDate) -> Unit,
    spendingDays: Map<LocalDate, SpendingDay>,
    modifier: Modifier = Modifier
) {
    val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
    var currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek()[0]))

    Box(Modifier.fillMaxWidth()) {
        Row(modifier = modifier) {
            for (i in 0..6) {
                if (
                    currentDay.month == week.yearMonth.month &&
                    !calendarUiState.isDisabledDay(currentDay)
                ) {
                    Day(
                        modifier = Modifier.weight(1f),
                        day = currentDay,
                        onDayClicked = onDayClicked,
                        spendingDays = spendingDays,
                    )
                } else {
                    Box(modifier = Modifier
                        .size(CELL_SIZE)
                        .weight(1f))
                }
                currentDay = currentDay.plusDays(1)
            }
        }
    }
}

@Composable
internal fun DayOfWeekHeading(day: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
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
internal fun Day(
    day: LocalDate,
    onDayClicked: (LocalDate) -> Unit,
    spendingDays: Map<LocalDate, SpendingDay>,
    modifier: Modifier = Modifier
) {
    val spendingDay = spendingDays[day]

    Log.d("SpendsCalendar", "spendingDay: $day $spendingDay")

    val harmonizedColor = if (spendingDay !== null) toPalette(
        harmonize(
            combineColors(
                listOf(
                    colorBad,
                    colorNotGood,
                    colorGood,
                ),
                (spendingDay.budget - spendingDay.spending)
                    .divide(spendingDay.budget, 2, RoundingMode.HALF_EVEN)
                    .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
                    .toFloat(),
            ),
            colorEditor
        )
    ) else toPalette(MaterialTheme.colorScheme.primary).copy(
        container = Color.Transparent,
        onContainer = MaterialTheme.colorScheme.onSurface,
    )

    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .height(CELL_SIZE - 2.dp)
                .width(CELL_SIZE - 2.dp)
                .background(
                    color = harmonizedColor.container,
                    shape = RoundedCornerShape(8.dp),
                )
        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W800),
                color = harmonizedColor.onContainer,
            )
        }
    }
}


@Preview(name = "Zero overspending")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Surface {
            SpendsCalendar(
                budget = BigDecimal(200),
                transactions = listOf(
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(8), date = LocalDate.now().minusDays(4).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(10), date = LocalDate.now().minusDays(2).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(3), date = LocalDate.now().minusDays(2).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(10), date = LocalDate.now().minusDays(1).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(5), date = LocalDate.now().minusDays(1).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(15), date = LocalDate.now().toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = Date()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(12), date = LocalDate.now().plusDays(1).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(6), date = LocalDate.now().plusDays(1).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(12), date = LocalDate.now().plusDays(1).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = LocalDate.now().plusDays(2).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(10), date = LocalDate.now().plusDays(2).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(12), date = LocalDate.now().plusDays(2).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(9), date = LocalDate.now().plusDays(5).toDate()),
                    Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = LocalDate.now().plusDays(5).toDate()),
                    Transaction(type = TransactionType.SET_DAILY_BUDGET, value = BigDecimal(14), date = LocalDate.now().plusDays(7).toDate()),
                ),
                currency = ExtendCurrency.none(),
                startDate = LocalDate.now().minusDays(7).toDate(),
                finishDate = LocalDate.now().plusDays(14).toDate(),
            )
        }
    }
}