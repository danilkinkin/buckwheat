package com.danilkinkin.buckwheat.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.di.SpendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Everything the ledger screens read: spends, categories and the display currency.
 *
 * The app is a plain expense tracker, so there is no budget state, daily allowance or period to
 * keep in sync here. Those preferences still exist in storage for the widget, which reads them
 * through [SpendsRepository] directly.
 */
@HiltViewModel
class SpendsViewModel @Inject constructor(
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var spends = spendsRepository.getAllSpends()
    var categories = spendsRepository.getAllCategories()
    var currency = spendsRepository.getCurrency().asLiveData()

    var lastRemovedTransaction: MutableLiveData<Transaction> = MutableLiveData()

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

    // Categories

    fun addCategory(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            spendsRepository.addCategory(name, icon, color)
        }
    }

    fun deleteCategory(category: Category, onDeleted: (List<Int>) -> Unit) {
        viewModelScope.launch {
            val affected = spendsRepository.deleteCategory(category.id)
            onDeleted(affected)
        }
    }

    fun restoreCategory(category: Category, transactionIds: List<Int>) {
        viewModelScope.launch {
            spendsRepository.restoreCategory(category, transactionIds)
        }
    }

    // Currency

    fun changeDisplayCurrency(currency: ExtendCurrency) {
        viewModelScope.launch {
            spendsRepository.changeDisplayCurrency(currency)
        }
    }
}
