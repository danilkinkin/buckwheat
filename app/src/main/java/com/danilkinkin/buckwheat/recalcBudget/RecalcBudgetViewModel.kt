package com.danilkinkin.buckwheat.recalcBudget

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.SpendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class RecalcBudgetViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    val newDailyBudgetIfSplitPerDay: MutableLiveData<BigDecimal> = MutableLiveData()
    val newDailyBudgetIfAddToday: MutableLiveData<BigDecimal> = MutableLiveData()
    val howMuchNotSpent: MutableLiveData<BigDecimal> = MutableLiveData()

    fun calculateSplitOnRestDays() = viewModelScope.launch {
        val whatBudgetForDay = spendsRepository.whatBudgetForDay(applyTodaySpends = true)

        newDailyBudgetIfSplitPerDay.value = whatBudgetForDay.setScale(0, RoundingMode.HALF_EVEN)
    }

    fun calculateAddToToday() = viewModelScope.launch {
        val notSpent = spendsRepository.howMuchNotSpent()
        val dailyBudget = spendsRepository.getDailyBudget().first()
        val budgetPerDayAdd = spendsRepository.whatBudgetForDay(
            excludeCurrentDay = false,
            applyTodaySpends = true,
            notCommittedSpent = notSpent - dailyBudget,
        )

        howMuchNotSpent.value = notSpent - dailyBudget
        newDailyBudgetIfAddToday.value = budgetPerDayAdd.setScale(0, RoundingMode.HALF_EVEN)
    }
}