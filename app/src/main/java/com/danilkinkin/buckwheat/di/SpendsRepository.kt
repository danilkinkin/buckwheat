package com.danilkinkin.buckwheat.di

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import com.danilkinkin.buckwheat.budgetDataStore
import com.danilkinkin.buckwheat.data.BudgetCalculator
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.data.nextSubscriptionDate
import com.danilkinkin.buckwheat.util.DAY
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.dao.CategoryDao
import com.danilkinkin.buckwheat.data.dao.SubscriptionDao
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.errorForReport
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.roundToDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.lang.Long.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import javax.inject.Inject

val currencyStoreKey = stringPreferencesKey("currency")
val restedBudgetDistributionMethodStoreKey = stringPreferencesKey("restedBudgetDistributionMethod")
val hideOverspendingWarnStoreKey = booleanPreferencesKey("hideOverspendingWarn")

val budgetStoreKey = stringPreferencesKey("budget")
val spentStoreKey = stringPreferencesKey("spent")
val dailyBudgetStoreKey = stringPreferencesKey("dailyBudget")
val spentFromDailyBudgetStoreKey = stringPreferencesKey("spentFromDailyBudget")
val lastChangeDailyBudgetDateStoreKey = longPreferencesKey("lastChangeDailyBudgetDate")
val startPeriodDateStoreKey = longPreferencesKey("startPeriodDate")
val finishPeriodDateStoreKey = longPreferencesKey("finishPeriodDate")
val finishPeriodActualDateStoreKey = longPreferencesKey("finishPeriodActualDate")

class SpendsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val subscriptionDao: SubscriptionDao,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
) {
    fun getAllTransactions(): LiveData<List<Transaction>> = transactionDao.getAll()
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

    fun getAllTags(): LiveData<List<String>> = transactionDao.getAll().map { transactions ->
        transactions
            .asSequence()
            .filter { transaction -> transaction.comment.isNotEmpty() }
            .groupBy { it.comment }
            .map { it.key to it.value.size }
            .sortedBy { -it.second }
            .map { it.first }
            .distinct()
            .toList()
    }

    fun getBudget() = context.budgetDataStore.data.map {
        (it[budgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO).setScale(2)
    }

    fun getSpent() = context.budgetDataStore.data.map {
        (it[spentStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO).setScale(2)
    }

    fun getDailyBudget() = context.budgetDataStore.data.map {
        (it[dailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO).setScale(2)
    }

    fun getSpentFromDailyBudget() = context.budgetDataStore.data.map {
        (it[spentFromDailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO).setScale(2)
    }

    fun getStartPeriodDate() = context.budgetDataStore.data.map {
        it[startPeriodDateStoreKey]?.let { value -> Date(value) } ?: getCurrentDateUseCase()
    }

    fun getFinishPeriodDate() = context.budgetDataStore.data.map {
        it[finishPeriodDateStoreKey]?.let { value -> Date(value) }
    }

    fun getFinishPeriodActualDate() = context.budgetDataStore.data.map {
        it[finishPeriodActualDateStoreKey]?.let { value -> Date(value) }
    }

    fun getLastChangeDailyBudgetDate() = context.budgetDataStore.data.map {
        it[lastChangeDailyBudgetDateStoreKey]?.let { value -> Date(value) }
    }

    fun getCurrency() = context.budgetDataStore.data.map {
        it[currencyStoreKey]?.let { value ->
            ExtendCurrency.getInstance(value)
        } ?: ExtendCurrency(value = null, type = ExtendCurrency.Type.NONE)
    }

    fun getRestedBudgetDistributionMethod() = context.budgetDataStore.data.map { it ->
        it[restedBudgetDistributionMethodStoreKey]?.let {
            RestedBudgetDistributionMethod.valueOf(it)
        } ?: RestedBudgetDistributionMethod.ASK
    }

    fun getHideOverspendingWarn() = context.budgetDataStore.data.map {
        it[hideOverspendingWarnStoreKey] ?: false
    }


    suspend fun changeDisplayCurrency(currency: ExtendCurrency) {
        context.budgetDataStore.edit {
            it[currencyStoreKey] = currency.value ?: ""
        }
    }

    suspend fun changeRestedBudgetDistributionMethod(method: RestedBudgetDistributionMethod) {
        context.budgetDataStore.edit {
            it[restedBudgetDistributionMethodStoreKey] = method.toString()
        }
    }

    suspend fun hideOverspendingWarn(hide: Boolean) {
        context.budgetDataStore.edit {
            it[hideOverspendingWarnStoreKey] = hide
        }
    }

    suspend fun setBudget(newBudget: BigDecimal, newFinishDate: Date) {
        context.budgetDataStore.edit {
            it[budgetStoreKey] = newBudget.toString()
            it[spentStoreKey] = BigDecimal.ZERO.toString()
            it[dailyBudgetStoreKey] = BigDecimal.ZERO.toString()
            it[spentFromDailyBudgetStoreKey] = BigDecimal.ZERO.toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(getCurrentDateUseCase()).time
            it[startPeriodDateStoreKey] = roundToDay(getCurrentDateUseCase()).time
            it[finishPeriodDateStoreKey] = Date(roundToDay(newFinishDate).time + DAY - 1000).time
            it.remove(finishPeriodActualDateStoreKey)

        }

        withContext(Dispatchers.IO) {
            transactionDao.deleteAll()
            transactionDao.insert(
                Transaction(
                    TransactionType.INCOME,
                    newBudget,
                    getCurrentDateUseCase(),
                )
            )
        }

        setDailyBudget(whatBudgetForDay())

        hideOverspendingWarn(false)
    }

    suspend fun changeBudget(newBudget: BigDecimal, newFinishDate: Date) {
        context.budgetDataStore.edit {
            it[budgetStoreKey] = newBudget.toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(getCurrentDateUseCase()).time
            it[finishPeriodDateStoreKey] = Date(roundToDay(newFinishDate).time + DAY - 1000).time
            it.remove(finishPeriodActualDateStoreKey)

        }

        withContext(Dispatchers.IO) {
            val incomeTransaction =
                transactionDao.getAll(TransactionType.INCOME).asFlow().first().first()

            transactionDao.update(incomeTransaction.copy(value = newBudget))
        }

        updateDailyBudget(whatBudgetForDay())
    }

    suspend fun finishBudget(finishDate: Date) {
        context.budgetDataStore.edit {
            it[finishPeriodActualDateStoreKey] = finishDate.time

        }
    }

    suspend fun updateDailyBudget(newDailyBudget: BigDecimal) {
        context.budgetDataStore.edit {
            it[dailyBudgetStoreKey] = newDailyBudget.toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(getCurrentDateUseCase()).time

        }


        withContext(Dispatchers.IO) {
            val setDailyBudgetTransaction =
                transactionDao.getAll(TransactionType.SET_DAILY_BUDGET).asFlow().first().last()

            transactionDao.update(setDailyBudgetTransaction.copy(value = newDailyBudget))
        }
    }

    suspend fun setDailyBudget(newDailyBudget: BigDecimal) {
        context.budgetDataStore.edit {
            val spent: BigDecimal = it[spentStoreKey]?.toBigDecimal()!!
            val spentFromDailyBudget: BigDecimal =
                it[spentFromDailyBudgetStoreKey]?.toBigDecimal()!!

            it[dailyBudgetStoreKey] = newDailyBudget.toString()
            it[spentStoreKey] = (spent + spentFromDailyBudget).toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(getCurrentDateUseCase()).time
            it[spentFromDailyBudgetStoreKey] = BigDecimal.ZERO.toString()

        }

        withContext(Dispatchers.IO) {
            transactionDao.insert(
                Transaction(
                    TransactionType.SET_DAILY_BUDGET,
                    newDailyBudget,
                    getCurrentDateUseCase(),
                )
            )
        }
    }

    suspend fun whatBudgetForDay(
        excludeCurrentDay: Boolean = false,
        applyTodaySpends: Boolean = false,
        notCommittedSpent: BigDecimal = BigDecimal.ZERO
    ): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val dailyBudget = getDailyBudget().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()
        // A budget is optional: without a period there is no daily allowance to report.
        val finishPeriodDate = getFinishPeriodDate().first()
            ?: return BigDecimal.ZERO.setScale(2)


        val restDays = countDays(finishPeriodDate, getCurrentDateUseCase())

        val whatBudgetForDay = BudgetCalculator.budgetForDay(
            budget = budget,
            spent = spent,
            dailyBudget = dailyBudget,
            spentFromDailyBudget = spentFromDailyBudget,
            restDays = restDays,
            excludeCurrentDay = excludeCurrentDay,
            applyTodaySpends = applyTodaySpends,
            notCommittedSpent = notCommittedSpent,
        )

        return whatBudgetForDay
    }

    suspend fun howMuchBudgetRest(): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()

        return budget - spent - spentFromDailyBudget
    }

    suspend fun howMuchNotSpent(
        excludeSkippedPart: Boolean = false,
    ): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val dailyBudget = getDailyBudget().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()
        // No budget means nothing has been set aside, so nothing is "not spent" yet.
        val finishPeriodDate = getFinishPeriodDate().first()
            ?: return BigDecimal.ZERO.setScale(2)
        val lastChangeDailyBudgetDate =
            getLastChangeDailyBudgetDate().first() ?: getStartPeriodDate().first()


        val restDays = countDays(finishPeriodDate, getCurrentDateUseCase()).coerceAtLeast(0)
        val skippedDays = countDays(
            Date(min(getCurrentDateUseCase().time, finishPeriodDate.time)),
            lastChangeDailyBudgetDate
        ) - 1

        val howMuchNotSpent = BudgetCalculator.notSpent(
            budget = budget,
            spent = spent,
            dailyBudget = dailyBudget,
            spentFromDailyBudget = spentFromDailyBudget,
            restDays = restDays,
            skippedDays = skippedDays,
            excludeSkippedPart = excludeSkippedPart,
        )

        return howMuchNotSpent
    }

    suspend fun nextDayBudget(
        excludeSkippedPart: Boolean = false,
    ): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val dailyBudget = getDailyBudget().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()
        // No budget means there is no next day allowance to plan for.
        val finishPeriodDate = getFinishPeriodDate().first()
            ?: return BigDecimal.ZERO.setScale(2)
        val lastChangeDailyBudgetDate =
            getLastChangeDailyBudgetDate().first() ?: getStartPeriodDate().first()


        val restDays = countDays(finishPeriodDate, getCurrentDateUseCase()).coerceAtLeast(0)
        val skippedDays = countDays(
            Date(min(getCurrentDateUseCase().time, finishPeriodDate.time)),
            lastChangeDailyBudgetDate
        ) - 1

        val nextDailyBudget = BudgetCalculator.nextDailyBudget(
            budget = budget,
            spent = spent,
            dailyBudget = dailyBudget,
            spentFromDailyBudget = spentFromDailyBudget,
            restDays = restDays,
            skippedDays = skippedDays,
            excludeSkippedPart = excludeSkippedPart,
        )

        return nextDailyBudget
    }

    suspend fun addSpent(newTransaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.insert(newTransaction)
        }

        context.budgetDataStore.edit {
            // The ledger is the source of truth; the budget roll-up below is optional. Without
            // a configured period there is simply nothing to recalculate.
            val finishPeriodDate = it[finishPeriodDateStoreKey]
                ?.let { value -> Date(value) }
                ?: return@edit

            try {
                if (isSameDay(newTransaction.date, getCurrentDateUseCase())) {
                    val spentFromDailyBudget =
                        it[spentFromDailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
                    it[spentFromDailyBudgetStoreKey] =
                        (spentFromDailyBudget + newTransaction.value).toString()
                } else {
                    val dailyBudget = it[dailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
                    val spent = it[spentStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO

                    val restDays = countDays(finishPeriodDate, getCurrentDateUseCase())
                        .coerceAtLeast(1)

                    val spreadDeltaSpentPerRestDays = newTransaction.value
                        .divide(
                            restDays.toBigDecimal(),
                            2,
                            RoundingMode.HALF_EVEN,
                        )

                    it[dailyBudgetStoreKey] = (dailyBudget - spreadDeltaSpentPerRestDays).toString()
                    it[spentStoreKey] = (spent + newTransaction.value).toString()
                }
            } catch (e: Exception) {
                context.errorForReport = e.stackTraceToString()
            }
        }
    }

    suspend fun removeSpent(transactionForRemove: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteById(transactionForRemove.uid)
        }

        context.budgetDataStore.edit {
            // See addSpent: no budget period means no roll-up to undo.
            val finishPeriodDate = it[finishPeriodDateStoreKey]
                ?.let { value -> Date(value) }
                ?: return@edit

            if (isSameDay(transactionForRemove.date, getCurrentDateUseCase())) {
                val spentFromDailyBudget =
                    it[spentFromDailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO

                it[spentFromDailyBudgetStoreKey] =
                    (spentFromDailyBudget - transactionForRemove.value).toString()
            } else {
                val dailyBudget = it[dailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
                val spent = it[spentStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO

                val restDays =
                    countDays(finishPeriodDate, getCurrentDateUseCase()).coerceAtLeast(1)
                val spreadDeltaSpentPerRestDays = transactionForRemove.value
                    .divide(
                        restDays.toBigDecimal(),
                        2,
                        RoundingMode.HALF_EVEN,
                    )

                it[dailyBudgetStoreKey] = (dailyBudget + spreadDeltaSpentPerRestDays).toString()
                it[spentStoreKey] = (spent - transactionForRemove.value).toString()
            }
        }
    }
}