package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.countDaysToToday
import kotlin.math.abs

const val DEBUG_MENU_SHEET = "debugMenu"

@Composable
fun DebugMenu(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    Surface {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Debug menu",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Header("Actions")
            ButtonRow(
                text = "Open daily summary screen",
                iconInset = false,
                onClick = {
                    appViewModel.openSheet(PathState(RECALCULATE_DAILY_BUDGET_SHEET))
                    onClose()
                },
            )
            ButtonRow(
                text = "Open period summary screen",
                iconInset = false,
                onClick = {
                    appViewModel.openSheet(PathState(FINISH_PERIOD_SHEET))
                    onClose()
                },
            )
            ButtonRow(
                text = "Open onboarding screen",
                iconInset = false,
                onClick = {
                    appViewModel.openSheet(PathState(ON_BOARDING_SHEET))
                    onClose()
                },
            )
            ButtonRow(
                text = "Force crash app",
                iconInset = false,
                onClick = {
                    throw Error("Test crash app")
                },
            )
            Header("Debug budget")
            Spacer(Modifier.height(16.dp))
            MonospaceText("Начало --------------- ${spendsViewModel.startDate.value!!}")
            MonospaceText("Конец ---------------- ${spendsViewModel.finishDate.value!!}")
            MonospaceText("Последний пересчет --- ${spendsViewModel.lastReCalcBudgetDate}")
            Spacer(Modifier.height(16.dp))

            val days = countDays(spendsViewModel.finishDate.value!!, spendsViewModel.startDate.value!!)
            val restDays = countDaysToToday(spendsViewModel.finishDate.value!!)
            MonospaceText("Всего дней -------------------- $days")
            MonospaceText("Прошло дней ------------------- ${days - restDays}")
            MonospaceText("Осталось дней ----------------- $restDays")
            MonospaceText("Дней с последнего пересчета --- ${abs(countDaysToToday(spendsViewModel.lastReCalcBudgetDate!!))}")
            Spacer(Modifier.height(16.dp))

            val spentFromDailyBudget = spendsViewModel.spentFromDailyBudget.value!!

            MonospaceText("Весь бюджет ------------------- ${spendsViewModel.budget.value!!}")
            MonospaceText("Потрачено из бюджета ---------- ${spendsViewModel.spent.value!! + spentFromDailyBudget}")
            MonospaceText("Оставшийся бюджет ------------- ${spendsViewModel.calcResetBudget()}")
            Spacer(Modifier.height(16.dp))


            val dailyBudget = spendsViewModel.dailyBudget.value!!
            val currentSpent = spendsViewModel.currentSpent

            val restTodayBudget = dailyBudget - spentFromDailyBudget - currentSpent

            MonospaceText("Бюджет на сегодня ------------- $dailyBudget")
            MonospaceText("Потрачено из дн. бюджета ------ $spentFromDailyBudget")
            MonospaceText("Текущяя трата ----------------- $currentSpent")
            MonospaceText("Осталось на сегодня ----------- $restTodayBudget")
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun Header(title: String) {
    Divider()
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 0.dp),
    )
}

@Composable
fun MonospaceText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 0.dp),
        fontFamily = FontFamily.Monospace
    )
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        DebugMenu()
    }
}