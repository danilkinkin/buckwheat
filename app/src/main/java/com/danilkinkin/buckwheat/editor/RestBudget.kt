package com.danilkinkin.buckwheat.editor

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import java.math.BigDecimal

@Composable
fun RestBudget(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val roundValues by spendsViewModel.roundValues.observeAsState(true)

    var budgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var restBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }

    fun calculateValues(
        budget: Boolean = true,
        restBudget: Boolean = true,
    ) {
        val spentFromDailyBudget = spendsViewModel.spentFromDailyBudget.value!!
        val dailyBudget = spendsViewModel.dailyBudget.value!!

        if (budget) budgetValue = (dailyBudget - spentFromDailyBudget).coerceAtLeast(
            BigDecimal(0)
        )

        if (restBudget) {
            val newBudget = dailyBudget - spentFromDailyBudget - spendsViewModel.currentSpent

            overdaft = newBudget < BigDecimal(0)

            restBudgetValue = newBudget.coerceAtLeast(BigDecimal(0))

            val newPerDayBudget = spendsViewModel.calcBudgetPerDaySplit(
                applyCurrentSpent = true,
                excludeCurrentDay = true,
            )

            endBudget = newPerDayBudget < BigDecimal(0)

            budgetPerDaySplit = prettyCandyCanes(
                newPerDayBudget.coerceAtLeast(BigDecimal(0)),
                currency = currency,
            )
        }
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues(budget = !editing, restBudget = false)
    }

    observeLiveData(spendsViewModel.stage) {
        editing = false

        when (it) {
            SpendsViewModel.Stage.IDLE, null -> {
            }
            SpendsViewModel.Stage.CREATING_SPENT -> {
                calculateValues(budget = false)
            }
            SpendsViewModel.Stage.EDIT_SPENT -> {
                calculateValues(budget = false)

                editing = true
            }
            SpendsViewModel.Stage.COMMITTING_SPENT -> {
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        val alpha: Float by animateFloatAsState(
            if (restBudgetValue > 0.toBigDecimal()) 1f else 0f,
            tween(
                durationMillis = 150,
                easing = EaseInOutQuad,
            ),
        )

        if (editing) {
            TextWithLabel(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .alpha(alpha)
                    .offset(x = (-30).dp * (1f - alpha)),
                value = prettyCandyCanes(
                    restBudgetValue,
                    currency = currency,
                ),
                label = stringResource(id = R.string.rest_budget_for_today),
                fontSizeValue = MaterialTheme.typography.headlineSmall.fontSize,
                fontSizeLabel = MaterialTheme.typography.labelMedium.fontSize,
                horizontalAlignment = Alignment.End,
            )
        } else {
            TextWithLabel(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .alpha(alpha)
                    .offset(x = (-30).dp * (1f - alpha)),
                value = prettyCandyCanes(
                    budgetValue,
                    currency = currency,
                ),
                label = stringResource(id = R.string.budget_for_today),
                fontSizeValue = MaterialTheme.typography.headlineSmall.fontSize,
                fontSizeLabel = MaterialTheme.typography.labelMedium.fontSize,
                horizontalAlignment = Alignment.End,
            )
        }
        BudgetEndWarn(
            overdaft = overdaft,
            forceShow = restBudgetValue <= 0.toBigDecimal(),
            endBudget = endBudget,
            budgetPerDaySplit = budgetPerDaySplit,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp),
            onClick = {
                if (endBudget) {
                    appViewModel.openSheet(PathState(BUDGET_IS_OVER_DESCRIPTION_SHEET))
                } else {
                    appViewModel.openSheet(PathState(NEW_DAY_BUDGET_DESCRIPTION_SHEET))
                }
            }
        )
    }
}