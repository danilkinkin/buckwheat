package com.luna.dollargrain.editor.toolbar.restBudgetPill

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.dollargrain.di.SpendsRepository
import com.luna.dollargrain.util.numberFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class DaileBudgetState {
    NOT_SET,
    OVERDRAFT,
    BUDGET_END,
    NORMAL,
}

@HiltViewModel
class RestBudgetPillViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var state = MutableLiveData(DaileBudgetState.NOT_SET)
        private set
    var percentWithNewSpent = MutableLiveData(1f)
        private set
    var percentWithoutNewSpent = MutableLiveData(1f)
        private set
    var todayBudget = MutableLiveData("")
        private set
    var newDailyBudget = MutableLiveData("")
        private set

    fun calculateValues(context: Context, currentSpent: BigDecimal) {
        val ths = this

        viewModelScope.launch {
            val budget = spendsRepository.getBudget().first()
            val spentFromDailyBudget = spendsRepository.getSpentFromDailyBudget().first()
            val dailyBudget = spendsRepository.getDailyBudget().first()
            val currency = spendsRepository.getCurrency().first()

            if (spendsRepository.getFinishPeriodDate().first() == null) {
                ths.state.value = DaileBudgetState.NOT_SET
                return@launch
            }

            if (dailyBudget <= BigDecimal.ZERO) {
                ths.state.value = DaileBudgetState.BUDGET_END
                ths.percentWithNewSpent.value = 0f
                ths.percentWithoutNewSpent.value = 0f
                ths.newDailyBudget.value = ""
                ths.todayBudget.value = ""

                return@launch
            }

            val restFromDayBudget = dailyBudget - spentFromDailyBudget - currentSpent
            val newDailyBudget = spendsRepository.whatBudgetForDay(
                excludeCurrentDay = true,
                applyTodaySpends = true,
                notCommittedSpent = currentSpent,
            )

            val isOverdraft = restFromDayBudget < BigDecimal.ZERO
            val isBudgetEnd = newDailyBudget <= BigDecimal.ZERO


            val percentWithNewSpent = restFromDayBudget
                .divide(dailyBudget, 2, RoundingMode.HALF_EVEN)
                .coerceAtLeast(BigDecimal.ZERO)

            val percentWithoutNewSpent = (restFromDayBudget + currentSpent)
                .divide(dailyBudget, 2, RoundingMode.HALF_EVEN)
                .coerceAtLeast(BigDecimal.ZERO)

            val formattedBudgetTodayValue = numberFormat(
                context,
                restFromDayBudget.coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
                trimDecimalPlaces = true,
            )

            val formattedBudgetNewDailyValue = numberFormat(
                context,
                newDailyBudget
                    .setScale(0, RoundingMode.HALF_EVEN)
                    .coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
                trimDecimalPlaces = true,
            )

            ths.state.value = when {
                isBudgetEnd -> DaileBudgetState.BUDGET_END
                isOverdraft -> DaileBudgetState.OVERDRAFT
                else -> DaileBudgetState.NORMAL
            }
            ths.percentWithNewSpent.value = percentWithNewSpent.toFloat()
            ths.percentWithoutNewSpent.value = percentWithoutNewSpent.toFloat()
            ths.newDailyBudget.value = formattedBudgetNewDailyValue
            ths.todayBudget.value = formattedBudgetTodayValue
        }
    }
}