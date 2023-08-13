package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class RestBudgetPillViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var isOverdraft = MutableLiveData(false)
        private set
    var isBudgetEnd = MutableLiveData(false)
        private set
    var percentWithNewSpent = MutableLiveData(0f)
        private set
    var percentWithoutNewSpent = MutableLiveData(0f)
        private set
    var todayBudget = MutableLiveData("")
        private set
    var newDailyBudget = MutableLiveData("")
        private set

    fun calculateValues(currentSpent: BigDecimal) {
        val ths = this

        viewModelScope.launch {
            val spentFromDailyBudget = spendsRepository.getSpentFromDailyBudget().first()
            val dailyBudget = spendsRepository.getDailyBudget().first()
            val currency = spendsRepository.getCurrency().first()

            spendsRepository.getFinishPeriodDate().first() ?: return@launch

            val restFromDayBudget = dailyBudget - spentFromDailyBudget - currentSpent
            val newDailyBudget = spendsRepository.whatBudgetForDay(
                excludeCurrentDay = false,
                notCommittedSpent = currentSpent,
            )

            val isOverdraft = restFromDayBudget < BigDecimal.ZERO
            val isBudgetEnd = newDailyBudget <= BigDecimal.ZERO


            val percentWithNewSpent = restFromDayBudget
                .divide(dailyBudget, 5, RoundingMode.HALF_EVEN)
                .coerceAtLeast(BigDecimal.ZERO)

            val percentWithoutNewSpent = (restFromDayBudget + currentSpent)
                .divide(dailyBudget, 5, RoundingMode.HALF_EVEN)
                .coerceAtLeast(BigDecimal.ZERO)

            val formattedBudgetTodayValue = prettyCandyCanes(
                restFromDayBudget.coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
            )

            val formattedBudgetNewDailyValue = prettyCandyCanes(
                newDailyBudget.coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
            )

            ths.isOverdraft.value = isOverdraft
            ths.isBudgetEnd.value = isBudgetEnd
            ths.percentWithNewSpent.value = percentWithNewSpent.toFloat()
            ths.percentWithoutNewSpent.value = percentWithoutNewSpent.toFloat()
            ths.newDailyBudget.value = formattedBudgetNewDailyValue
            ths.todayBudget.value = formattedBudgetTodayValue
        }
    }
}