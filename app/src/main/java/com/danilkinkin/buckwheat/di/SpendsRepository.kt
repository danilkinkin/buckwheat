package com.danilkinkin.buckwheat.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import com.danilkinkin.buckwheat.budgetDataStore
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.dao.CategoryDao
import com.danilkinkin.buckwheat.data.dao.SubscriptionDao
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.data.nextSubscriptionDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

val currencyStoreKey = stringPreferencesKey("currency")

/**
 * Storage for the ledger: spends, categories, subscriptions and the display currency.
 *
 * This used to also carry a budget period, daily allowance and roll-up calculations. Those are
 * gone; only the DataStore file name (`budgetDataStore`) is kept so existing installs keep their
 * currency setting.
 */
class SpendsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val subscriptionDao: SubscriptionDao,
) {
    fun getAllSpends(): LiveData<List<Transaction>> = transactionDao.getAll(TransactionType.SPENT)

    fun getAllCategories(): LiveData<List<Category>> = categoryDao.getAll()

    suspend fun addCategory(name: String, emoji: String, color: Long): Long {
        return categoryDao.insert(
            Category(
                name = name.trim(),
                emoji = emoji.trim(),
                color = color,
            )
        )
    }

    suspend fun deleteCategory(categoryId: Int) {
        withContext(Dispatchers.IO) {
            // Expenses outlive the category: they become Uncategorised instead of disappearing.
            transactionDao.clearCategory(categoryId)
            categoryDao.deleteById(categoryId)
        }
    }

    // Subscriptions

    fun getAllSubscriptions(): LiveData<List<Subscription>> = subscriptionDao.getAll()

    suspend fun addSubscription(
        name: String,
        amount: BigDecimal,
        period: SubscriptionPeriod,
        nextDue: Date,
        categoryId: Int?,
    ): Long {
        return subscriptionDao.insert(
            Subscription(
                name = name.trim(),
                amount = amount,
                period = period.name,
                anchorDay = nextDue.toLocalDate().dayOfMonth,
                nextDue = nextDue,
                categoryId = categoryId,
            )
        )
    }

    suspend fun updateSubscription(subscription: Subscription) {
        withContext(Dispatchers.IO) {
            subscriptionDao.update(subscription)
        }
    }

    suspend fun deleteSubscription(subscriptionId: Int) {
        withContext(Dispatchers.IO) {
            subscriptionDao.deleteById(subscriptionId)
        }
    }

    /**
     * Posts the due occurrence as a normal SPENT transaction dated to the due day, then advances
     * the subscription. The transaction check keeps a double tap from posting twice.
     */
    suspend fun markSubscriptionPaid(subscription: Subscription) {
        withContext(Dispatchers.IO) {
            val dueDate = subscription.nextDue
            val alreadyPosted =
                transactionDao.countMatching(dueDate, subscription.amount, subscription.name) > 0

            if (!alreadyPosted) {
                transactionDao.insert(
                    Transaction(
                        TransactionType.SPENT,
                        subscription.amount,
                        dueDate,
                        subscription.name,
                        subscription.categoryId,
                    )
                )
            }

            val next = nextSubscriptionDate(
                period = subscription.period.toSubscriptionPeriod(),
                anchorDay = subscription.anchorDay,
                from = dueDate.toLocalDate(),
            )

            subscriptionDao.update(subscription.copy(nextDue = next.toDate()))
        }
    }

    // Currency

    fun getCurrency() = context.budgetDataStore.data.map {
        it[currencyStoreKey]?.let { value ->
            ExtendCurrency.getInstance(value)
        } ?: ExtendCurrency(value = null, type = ExtendCurrency.Type.NONE)
    }

    suspend fun changeDisplayCurrency(currency: ExtendCurrency) {
        context.budgetDataStore.edit {
            it[currencyStoreKey] = currency.value ?: ""
        }
    }

    // Spends

    suspend fun addSpent(newTransaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.insert(newTransaction)
        }
    }

    suspend fun removeSpent(transactionForRemove: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteById(transactionForRemove.uid)
        }
    }
}
