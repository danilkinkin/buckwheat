package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.datePicker.CELL_SIZE
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarUiState
import com.danilkinkin.buckwheat.base.datePicker.model.Week
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.getWeek
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.isZero
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
import java.util.Locale
import kotlin.math.pow

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
    val locale = LocalConfiguration.current.locales[0]

    val spendingDays = remember(transactions) {
        val days: MutableMap<LocalDate, SpendingDay> =
            emptyMap<LocalDate, SpendingDay>().toMutableMap()
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
                spending = currDay!!.spending + it.value, spends = currDay!!.spends.plus(it)
            )
        }

        if (currDay != null) {
            days[currDay!!.date.toLocalDate()] = currDay!!
        }

        days.toMutableMap()
    }

    val calendarState by remember {
        mutableStateOf(
            CalendarState(
                context = context,
                disableBeforeDate = startDate,
                disableAfterDate = finishDate,
            )
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = combineColors(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant,
                angle = 0.3f,
            ),
        )
    ) {
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 2.dp),
                painter = painterResource(R.drawable.ic_info),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.spends_calendar_hint),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                ),
            )
        }
        Layout(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            measurePolicy = verticalGridMeasurePolicy(7),
            content = {
                val calendarUiState = calendarState.calendarUiState.value

                calendarState.listMonths.forEach { month ->
                    MonthHeader(
                        modifier = Modifier.layoutId("fullWidth"),
                        yearMonth = month.yearMonth
                    )

                    DaysOfWeek(locale)

                    month.weeks.forEach { week ->
                        val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
                        val currentDay =
                            beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek(locale)[0]))


                        if (
                            currentDay.plusDays(6).isAfter(calendarUiState.disabledBefore) &&
                            currentDay.isBefore(calendarUiState.disabledAfter)
                        ) {
                            Week(
                                week = week,
                                calendarUiState = calendarUiState,
                                spendingDays = spendingDays,
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
internal fun MonthHeader(modifier: Modifier = Modifier, yearMonth: YearMonth) {
    Row(modifier = modifier.height(CELL_SIZE), verticalAlignment = Alignment.Bottom) {
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
internal fun DaysOfWeek(locale: Locale) {
    val week = getWeek(locale)

    for (day in week) {
        DayOfWeekHeading(
            day = prettyWeekDay(day),
        )
    }
}


@Composable
internal fun Week(
    calendarUiState: CalendarUiState,
    week: Week,
    spendingDays: Map<LocalDate, SpendingDay>,
) {
    val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())

    for (day in 0..6) {
        val currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek()[0]))
            .plusDays(day.toLong())

        if (currentDay.month == week.yearMonth.month && !calendarUiState.isDisabledDay(currentDay)) {
            Day(
                modifier = Modifier,
                day = currentDay,
                spendingDays = spendingDays,
            )
        } else {
            Box(
                modifier = Modifier.size(CELL_SIZE)
            )
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
            .background(Color.Transparent), contentAlignment = Alignment.Center
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
    spendingDays: Map<LocalDate, SpendingDay>,
    modifier: Modifier = Modifier
) {
    val spendingDay = if (spendingDays[day] === null || spendingDays[day]!!.spending.isZero()) {
        null
    } else {
        spendingDays[day]
    }

    val harmonizedColor = if (spendingDay !== null) toPalette(
        harmonize(
            if (spendingDay.spending <= spendingDay.budget) {
                combineColors(
                    listOf(
                        colorNotGood,
                        colorGood,
                    ),
                    (spendingDay.budget - spendingDay.spending).divide(
                        spendingDay.budget,
                        2,
                        RoundingMode.HALF_EVEN
                    ).coerceIn(BigDecimal.ZERO, BigDecimal.ONE).toFloat(),
                )
            } else {
                colorBad
            }, colorEditor
        )
    ) else toPalette(MaterialTheme.colorScheme.primary).copy(
        surface = combineColors(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.3f,
        ),
        container = Color.Transparent,
        onContainer = MaterialTheme.colorScheme.onSurface,
    )

    val percent = if (spendingDay !== null) {
        spendingDay.spending.divide(spendingDay.budget, 2, RoundingMode.HALF_EVEN).toFloat()
    } else {
        0f
    }

    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .zIndex(if (spendingDay === null) 0f else -percent + 1000f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .size(CELL_SIZE - 2.dp)
                .background(
                    color = harmonizedColor.surface.copy(0.8f),
                    shape = RoundedCornerShape(10.dp),
                )
                .border(
                    width = 2.dp,
                    color = harmonizedColor.container.copy(
                        (if (percent < 1f) 0.4f else 1f).coerceAtMost(harmonizedColor.container.alpha)
                    ),
                    shape = RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (spendingDay !== null) {
                Box(
                    modifier = Modifier
                        .requiredSize(CELL_SIZE * percent)
                        .background(
                            color = harmonizedColor.container.copy(
                                if (percent > 1f) (1 - (percent.coerceIn(
                                    1f,
                                    3f
                                ) - 1) / 2) * 0.3f + 0.2f else 0.5f
                            ),
                            shape = RoundedCornerShape(
                                10.dp * percent
                                    .coerceAtLeast(0.7f)
                                    .pow(1.8f),
                            ),
                        )
                )
            }
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

fun verticalGridMeasurePolicy(columns: Int) =
    MeasurePolicy { measurables, constraints ->
        val cellWidth = constraints.maxWidth / columns
        val cells = emptyList<Int>().toMutableList()
        var cellsCount = 0

        val placeables = measurables.mapIndexed { index, it ->
            cells.add(if (it.layoutId == "fullWidth") columns else 1)
            cellsCount += cells[index]

            it.measure(
                constraints.copy(
                    maxWidth = cellWidth * cells[index],
                )
            )
        }


        layout(
            constraints.maxWidth,
            cellsCount / columns * CELL_SIZE.roundToPx(),
        ) {
            var cellsOffset = 0

            placeables.forEachIndexed { index, it ->
                val cellIndex = (cells.getOrNull(index - 1) ?: 0) + cellsOffset

                cellsOffset = cellIndex
                it.place(
                    cellWidth * (cellIndex % columns),
                    CELL_SIZE.roundToPx() * (cellIndex / columns),
                    0f
                )
            }
        }
    }


@Preview(name = "Zero overspending")
@Preview(name = "Zero overspending (Dark mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        SpendsCalendar(
            budget = BigDecimal(200),
            transactions = listOf(
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(8),
                    date = LocalDate.now().minusDays(4).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(10),
                    date = LocalDate.now().minusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(3),
                    date = LocalDate.now().minusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(10),
                    date = LocalDate.now().minusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(5),
                    date = LocalDate.now().minusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(15),
                    date = LocalDate.now().toDate()
                ),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = Date()),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(12),
                    date = LocalDate.now().plusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(6),
                    date = LocalDate.now().plusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(12),
                    date = LocalDate.now().plusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(8),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(10),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(12),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(9),
                    date = LocalDate.now().plusDays(5).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(8),
                    date = LocalDate.now().plusDays(5).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(14),
                    date = LocalDate.now().plusDays(7).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(82),
                    date = LocalDate.now().plusDays(11).toDate()
                ),
                Transaction(
                    type = TransactionType.SET_DAILY_BUDGET,
                    value = BigDecimal(14),
                    date = LocalDate.now().plusDays(7).toDate()
                ),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(27).toDate(),
        )
    }
}