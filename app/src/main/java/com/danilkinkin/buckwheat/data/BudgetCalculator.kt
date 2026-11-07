package com.danilkinkin.buckwheat.data

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Every piece of budget arithmetic, with no Android dependency, so it can be unit tested on
 * the JVM without an emulator.
 *
 * Two problems motivated pulling this out of `SpendsRepository`:
 *
 *  1. The home screen widget carried its own copy of these formulas with a different rounding
 *     mode (`setScale(0, FLOOR)` vs `setScale(2, HALF_EVEN)`), so the widget and the app could
 *     report different daily budgets for the same day.
 *  2. The widget's copy divided without an explicit scale. `BigDecimal.divide(other)` throws
 *     `ArithmeticException` whenever the quotient does not terminate, which happens as soon as
 *     the remaining days do not divide the remaining budget evenly (for example 900 / 7).
 */
object BudgetCalculator {
    /** Money is always stored and compared at 2 decimals. */
    const val SCALE = 2

    /** Banker's rounding, matching the rest of the app. */
    val ROUNDING: RoundingMode = RoundingMode.HALF_EVEN

    fun money(value: BigDecimal): BigDecimal = value.setScale(SCALE, ROUNDING)

    /** Budget left to spend in the period. */
    fun restBudget(
        budget: BigDecimal,
        spent: BigDecimal,
        spentFromDailyBudget: BigDecimal,
    ): BigDecimal = budget - spent - spentFromDailyBudget

    /**
     * Budget available for a single day.
     *
     * @param restDays days left in the period, inclusive - see
     * `com.danilkinkin.buckwheat.util.countDays`
     * @param excludeCurrentDay drop today from the split
     * @param applyTodaySpends subtract what was already spent today instead of a whole day
     * @param notCommittedSpent money reserved by an edit that has not been committed yet
     */
    fun budgetForDay(
        budget: BigDecimal,
        spent: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
        restDays: Int,
        excludeCurrentDay: Boolean = false,
        applyTodaySpends: Boolean = false,
        notCommittedSpent: BigDecimal = BigDecimal.ZERO,
    ): BigDecimal {
        val effectiveRestDays = restDays - if (excludeCurrentDay) 1 else 0
        val restBudget = budget - spent - notCommittedSpent

        val rest = when {
            applyTodaySpends -> restBudget - spentFromDailyBudget
            excludeCurrentDay -> restBudget - dailyBudget
            else -> restBudget
        }

        return rest.divide(effectiveRestDays.coerceAtLeast(1).toBigDecimal(), SCALE, ROUNDING)
    }

    /**
     * How much of today's budget was not spent, including the part that rolls over from days
     * that were skipped.
     */
    fun notSpent(
        budget: BigDecimal,
        spent: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
        restDays: Int,
        skippedDays: Int,
        excludeSkippedPart: Boolean = false,
    ): BigDecimal {
        // Nothing to roll over into once the period is over.
        if (restDays == 0) return (budget - spent) - spentFromDailyBudget

        return carriedOver(
            budget = budget,
            spent = spent,
            dailyBudget = dailyBudget,
            restDays = restDays,
            skippedDays = skippedDays,
            excludeSkippedPart = excludeSkippedPart,
        ) + (dailyBudget - spentFromDailyBudget)
    }

    /**
     * The budget for the next day, without the part that is already spent today.
     */
    fun nextDailyBudget(
        budget: BigDecimal,
        spent: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
        restDays: Int,
        skippedDays: Int,
        excludeSkippedPart: Boolean = false,
    ): BigDecimal {
        if (restDays == 0) return (budget - spent) - spentFromDailyBudget

        return carriedOver(
            budget = budget,
            spent = spent,
            dailyBudget = dailyBudget,
            restDays = restDays,
            skippedDays = skippedDays,
            excludeSkippedPart = excludeSkippedPart,
        )
    }

    private fun carriedOver(
        budget: BigDecimal,
        spent: BigDecimal,
        dailyBudget: BigDecimal,
        restDays: Int,
        skippedDays: Int,
        excludeSkippedPart: Boolean,
    ): BigDecimal {
        val restBudget = budget - spent

        return if (excludeSkippedPart) {
            (restBudget - dailyBudget * skippedDays.toBigDecimal())
                .divide(restDays.coerceAtLeast(1).toBigDecimal(), SCALE, ROUNDING)
                .multiply(skippedDays.coerceAtLeast(0).toBigDecimal())
        } else {
            (restBudget - dailyBudget)
                .divide(
                    (restDays + skippedDays - 1).coerceAtLeast(1).toBigDecimal(),
                    SCALE,
                    ROUNDING,
                )
                .multiply(skippedDays.coerceAtLeast(0).toBigDecimal())
        }
    }
}
