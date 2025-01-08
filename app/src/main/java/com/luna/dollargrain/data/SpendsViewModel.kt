package com.luna.dollargrain.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.di.SpendsRepository
import com.luna.dollargrain.util.countDaysToToday
import com.luna.dollargrain.util.isToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

enum class RestedBudgetDistributionMethod { REST, ADD_TODAY, ASK, ADD_SAVINGS }

@HiltViewModel
class SpendsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var tags = spendsRepository.getAllTags()
    var transactions = spendsRepository.getAllTransactions()
    var spends = spendsRepository.getAllSpends()
    var budget = spendsRepository.getBudget().asLiveData()
    var spent = spendsRepository.getSpent().asLiveData()
    var savings = spendsRepository.getSavings().asLiveData()
    var dailyBudget = spendsRepository.getDailyBudget().asLiveData()
    var spentFromDailyBudget = spendsRepository.getSpentFromDailyBudget().asLiveData()
    var startPeriodDate = spendsRepository.getStartPeriodDate().asLiveData()
    var finishPeriodDate = spendsRepository.getFinishPeriodDate().asLiveData()
    var finishPeriodActualDate = spendsRepository.getFinishPeriodActualDate().asLiveData()
    var lastChangeDailyBudgetDate = spendsRepository.getLastChangeDailyBudgetDate().asLiveData()

    var currency = spendsRepository.getCurrency().asLiveData()
    var restedBudgetDistributionMethod =
        spendsRepository.getRestedBudgetDistributionMethod().asLiveData()
    var hideOverspendingWarn = spendsRepository.getHideOverspendingWarn().asLiveData()

    var requireDistributionRestedBudget = MutableLiveData(false)
    var requireSetBudget = MutableLiveData(false)
    var periodFinished = MutableLiveData(false)
    var lastRemovedTransaction: MutableLiveData<Transaction> = MutableLiveData()

    init {
        runChangeDayAction()
        runScheduledDetectChangeDayTask()
    }

    // Budget handling

    fun setBudget(newBudget: BigDecimal, newFinishDate: Date) {
        viewModelScope.launch {
            spendsRepository.setBudget(newBudget, newFinishDate)

            requireSetBudget.value = false
            periodFinished.value = false
        }
    }

    fun changeBudget(newBudget: BigDecimal, newFinishDate: Date) {
        viewModelScope.launch {
            spendsRepository.changeBudget(newBudget, newFinishDate)

            requireSetBudget.value = false
            periodFinished.value = false
        }
    }

    fun finishBudget() {
        viewModelScope.launch {
            spendsRepository.finishBudget(Date())

            requireSetBudget.value = false
            periodFinished.value = true
        }
    }

    fun setDailyBudget(newDailyBudget: BigDecimal) {
        viewModelScope.launch {
            spendsRepository.setDailyBudget(newDailyBudget)
        }
    }

    // Spend handling

    fun addSpent(transactionForAdd: Transaction) {
        viewModelScope.launch {
            spendsRepository.addSpent(transactionForAdd)
        }
    }

    fun removeSpent(transactionForRemove: Transaction, silent: Boolean = false) {
        viewModelScope.launch {
            spendsRepository.removeSpent(transactionForRemove)

            if (!silent) {
                lastRemovedTransaction.value = transactionForRemove
            }
        }
    }

    fun undoRemoveSpent() {
        viewModelScope.launch {
            lastRemovedTransaction.value?.let {
                spendsRepository.addSpent(it)
            }
        }
    }

    // Other

    fun changeDisplayCurrency(currency: ExtendCurrency) {
        viewModelScope.launch {
            spendsRepository.changeDisplayCurrency(currency)
        }
    }

    fun changeRestedBudgetDistributionMethod(method: RestedBudgetDistributionMethod) {
        viewModelScope.launch {
            spendsRepository.changeRestedBudgetDistributionMethod(method)
        }
    }

    fun hideOverspendingWarn(hide: Boolean) {
        viewModelScope.launch {
            spendsRepository.hideOverspendingWarn(hide)
        }
    }

    // Need to be refactored

    fun howMuchBudgetRest(): LiveData<BigDecimal> {
        val data = MutableLiveData<BigDecimal>()

        viewModelScope.launch {
            data.value = spendsRepository.howMuchBudgetRest()
        }

        return data
    }

    // Background tasks
    private fun runChangeDayAction() {
        viewModelScope.launch {
            val lastChangeDailyBudgetDate = spendsRepository.getLastChangeDailyBudgetDate().first()
            val finishPeriodDate = spendsRepository.getFinishPeriodDate().first()
            val finishPeriodActualDate = spendsRepository.getFinishPeriodActualDate().first()
            val dailyBudget = spendsRepository.getDailyBudget().first()
            val spentFromDailyBudget = spendsRepository.getSpentFromDailyBudget().first()
            val restedBudgetDistributionMethod =
                spendsRepository.getRestedBudgetDistributionMethod().first()

            val finishDayNotReached = if (finishPeriodActualDate === null) {
                finishPeriodDate !== null
                        && countDaysToToday(finishPeriodDate) > 0
            } else {
                countDaysToToday(finishPeriodActualDate) > 0
            }

            val finishTimeReached = if (finishPeriodActualDate === null) {
                finishPeriodDate !== null
                        && finishPeriodDate.time <= Date().time
            } else {
                finishPeriodActualDate.time <= Date().time
            }

            when {
                lastChangeDailyBudgetDate !== null
                        && !isToday(lastChangeDailyBudgetDate)
                        && finishDayNotReached -> {
                    if (dailyBudget - spentFromDailyBudget > BigDecimal.ZERO) {
                        when (restedBudgetDistributionMethod) {
                            RestedBudgetDistributionMethod.ASK -> {
                                requireDistributionRestedBudget.value = true
                            }

                            RestedBudgetDistributionMethod.REST -> {
                                val whatBudgetForDay =
                                    spendsRepository.whatBudgetForDay(applyTodaySpends = true)
                                setDailyBudget(whatBudgetForDay)
                            }

                            RestedBudgetDistributionMethod.ADD_TODAY -> {
                                val notSpent = spendsRepository.howMuchNotSpent(
                                    excludeSkippedPart = true,
                                )

                                setDailyBudget(notSpent)
                            }

                            // TODO: add some way to store savings
                            RestedBudgetDistributionMethod.ADD_SAVINGS -> {
                                val notSpent = spendsRepository.howMuchNotSpent(
                                    excludeSkippedPart = true,
                                )
                            }
                        }
                    } else {
                        val whatBudgetForDay =
                            spendsRepository.whatBudgetForDay(applyTodaySpends = true)
                        setDailyBudget(whatBudgetForDay)
                    }
                }

                lastChangeDailyBudgetDate === null -> {
                    requireSetBudget.value = true
                }

                finishTimeReached -> {
                    periodFinished.value = true
                }
            }

            // Bug fix https://github.com/luna/buckwheat/issues/28
            if (dailyBudget - spentFromDailyBudget > BigDecimal.ZERO) {
                hideOverspendingWarn(false)
            }
        }
    }

    private fun runScheduledDetectChangeDayTask() {
        var currentDay = Date()

        viewModelScope.launch {
            while (true) {
                delay(5000L)

                if (isToday(currentDay)) continue

                currentDay = Date()
                runChangeDayAction()
            }
        }
    }
}
