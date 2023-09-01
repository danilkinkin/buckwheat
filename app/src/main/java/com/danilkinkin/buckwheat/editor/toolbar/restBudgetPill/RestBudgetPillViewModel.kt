package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.isZero
import com.danilkinkin.buckwheat.util.numberFormat
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

    fun calculateValues(currentSpent: BigDecimal) {
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

            if (dailyBudget.isZero()) return@launch

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
                restFromDayBudget.coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
            )

            val formattedBudgetNewDailyValue = numberFormat(
                newDailyBudget
                    .setScale(0, RoundingMode.HALF_EVEN)
                    .coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
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