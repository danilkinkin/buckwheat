package com.danilkinkin.buckwheat.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.di.SpendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var subscriptions = spendsRepository.getAllSubscriptions()

    fun add(
        name: String,
        amount: BigDecimal,
        period: SubscriptionPeriod,
        nextDue: Date,
        categoryId: Int?,
        serviceId: String?,
    ) {
        viewModelScope.launch {
            spendsRepository.addSubscription(name, amount, period, nextDue, categoryId, serviceId)
        }
    }

    fun update(subscription: Subscription) {
        viewModelScope.launch {
            spendsRepository.updateSubscription(subscription)
        }
    }

    fun delete(subscriptionId: Int) {
        viewModelScope.launch {
            spendsRepository.deleteSubscription(subscriptionId)
        }
    }

    fun markPaid(subscription: Subscription) {
        viewModelScope.launch {
            spendsRepository.markSubscriptionPaid(subscription)
        }
    }
}
