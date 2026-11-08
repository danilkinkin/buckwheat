package com.danilkinkin.buckwheat.base.datePicker

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarUiState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.prettyYearMonth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

private val MONTH_PAGE_HEIGHT = 344.dp

/**
 * One month per page with a fixed month title above it. Swiping horizontally moves through past
 * and future months; the arrows do the same for accessibility.
 */
@Composable
fun DatePicker(
    calendarState: CalendarState,
    onDayClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarUiState = calendarState.calendarUiState.value
    val months = calendarState.listMonths
    val selectedDate = calendarUiState.selectedStartDate ?: LocalDate.now()
    val initialPage = remember(months) {
        months.indexOfFirst { it.yearMonth == YearMonth.from(selectedDate) }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { months.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage - 1).coerceAtLeast(0)
                        )
                    }
                },
                enabled = pagerState.currentPage > 0,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                )
            }
            Text(
                text = prettyYearMonth(months[pagerState.currentPage].yearMonth),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage + 1).coerceAtMost(months.lastIndex)
                        )
                    }
                },
                enabled = pagerState.currentPage < months.lastIndex,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(MONTH_PAGE_HEIGHT),
        ) { page ->
            val month = months[page]

            BoxWithConstraints(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    DaysOfWeek(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))

                    month.weeks.forEach { week ->
                        Week(
                            calendarUiState = calendarUiState,
                            week = week,
                            onDayClicked = onDayClicked,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Preview(name = "EN locale", locale = "en")
@Composable
fun DayPreview() {
    val context = LocalContext.current

    val state = remember { CalendarState(context) }

    BuckwheatTheme {
        Surface {
            DatePicker(
                state,
                onDayClicked = { state.setSelectedDay(it) },
            )
        }
    }
}

@Preview(name = "RU locale", locale = "ru")
@Composable
fun DayPreviewRu() {
    val context = LocalContext.current

    val state = remember { CalendarState(context) }

    BuckwheatTheme {
        Surface {
            DatePicker(
                state,
                onDayClicked = { state.setSelectedDay(it) },
            )
        }
    }
}
