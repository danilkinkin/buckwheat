package com.danilkinkin.buckwheat.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.data.entities.ArchivedTransaction
import com.danilkinkin.buckwheat.data.entities.Period
import com.danilkinkin.buckwheat.di.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val archivedPeriods: LiveData<List<Period>> = historyRepository.getAllPeriods()

    private val _selectedPeriodId = MutableLiveData<Int?>(null)
    val selectedPeriodId: LiveData<Int?> = _selectedPeriodId

    fun selectPeriod(periodId: Int) {
        _selectedPeriodId.value = periodId
    }

    fun clearSelection() {
        _selectedPeriodId.value = null
    }

    fun getPeriodById(periodId: Int): LiveData<Period?> {
        return historyRepository.getPeriodById(periodId)
    }

    fun getTransactionsForPeriod(periodId: Int): LiveData<List<ArchivedTransaction>> {
        return historyRepository.getTransactionsForPeriod(periodId)
    }

    fun deletePeriod(periodId: Int) {
        viewModelScope.launch {
            historyRepository.deletePeriod(periodId)
            if (_selectedPeriodId.value == periodId) {
                _selectedPeriodId.value = null
            }
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            historyRepository.deleteAllHistory()
            _selectedPeriodId.value = null
        }
    }
}
