package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.countDaysToToday
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.rememberNavigationBarHeight
import java.math.BigDecimal

const val DEBUG_MENU_SHEET = "debugMenu"

@Composable
fun DebugMenu(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = rememberNavigationBarHeight().coerceAtLeast(16.dp)

    val startPeriodDate by spendsViewModel.startPeriodDate.observeAsState()
    val finishPeriodDate by spendsViewModel.finishPeriodDate.observeAsState()
    val lastChangeDailyBudgetDate by spendsViewModel.lastChangeDailyBudgetDate.observeAsState()

    val wholeDays = startPeriodDate?.let { start -> finishPeriodDate?.let { finish -> countDays(finish, start) } } ?: 0
    val restDays = finishPeriodDate?.let { countDaysToToday(it) } ?: 0
    val spentDays = wholeDays - restDays
    val countDaysFromLastChangeDailyBudget = lastChangeDailyBudgetDate?.let { countDaysToToday(it) } ?: 0

    val budget by spendsViewModel.budget.observeAsState(BigDecimal.ZERO)
    val spent by spendsViewModel.spent.observeAsState(BigDecimal.ZERO)
    val spentFromDailyBudget by spendsViewModel.spentFromDailyBudget.observeAsState(BigDecimal.ZERO)
    val howMuchBudgetRest by spendsViewModel.howMuchBudgetRest().observeAsState(BigDecimal.ZERO)


    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
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
            MonospaceText("Начало ------------- ${startPeriodDate?.let { prettyDate(
                date = it,
                pattern = "dd.MM.yyyy HH:mm:ss",
                simplifyIfToday = false,
            ) }}")
            MonospaceText("Конец -------------- ${finishPeriodDate?.let { prettyDate(
                date = it,
                pattern = "dd.MM.yyyy HH:mm:ss",
                simplifyIfToday = false,
            ) }}")
            MonospaceText("Посл. пересчет ----- ${lastChangeDailyBudgetDate?.let { prettyDate(
                date = it,
                pattern = "dd.MM.yyyy HH:mm:ss",
                simplifyIfToday = false,
            ) }}")
            Spacer(Modifier.height(16.dp))


            MonospaceText("Всего дней -------------------- $wholeDays")
            MonospaceText("Прошло дней ------------------- $spentDays")
            MonospaceText("Осталось дней ----------------- $restDays")
            MonospaceText("Дней с последнего пересчета --- $countDaysFromLastChangeDailyBudget")
            Spacer(Modifier.height(16.dp))


            MonospaceText("Весь бюджет ------------------- $budget")
            MonospaceText("Потрачено из бюджета ---------- ${spent + spentFromDailyBudget}")
            MonospaceText("Оставшийся бюджет ------------- $howMuchBudgetRest")
            Spacer(Modifier.height(16.dp))


            val dailyBudget = spendsViewModel.dailyBudget.value!!
            val currentSpent = editorViewModel.currentSpent

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