package com.danilkinkin.buckwheat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.countDays
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.lang.Math.max
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.floor

class NewDayBottomSheet: BottomSheetDialogFragment() {
    companion object {
        val TAG = NewDayBottomSheet::class.simpleName
    }

    private lateinit var appModel: AppViewModel
    private lateinit var drawsModel: DrawsViewModel

    private val restBudgetOfDayTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.rest_budget_of_day)
    }

    private val splitRestDaysDescriptionTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.spli_rest_days_description)
    }

    private val addCurrentDayDescriptionTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.add_current_day_description)
    }

    private val splitRestDaysCardView: MaterialCardView by lazy {
        requireView().findViewById(R.id.split_rest_days)
    }

    private val addCurrentDayCardView: MaterialCardView by lazy {
        requireView().findViewById(R.id.add_current_day)
    }

    private val debugTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.debug)
    }

    private val splitRestDaysDebugTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.spli_rest_days_debug)
    }

    private val addCurrentDayDebugTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.add_current_day_debug)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet_new_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appModel: AppViewModel by activityViewModels()
        val drawsModel: DrawsViewModel by activityViewModels()

        this.appModel = appModel
        this.drawsModel = drawsModel

        build()
    }

    fun build() {
        val restDays = countDays(drawsModel.finishDate)
        val skippedDays = abs(countDays(drawsModel.lastReCalcBudgetDate!!))

        val restBudget = (drawsModel.budget.value!! - drawsModel.spent.value!!) - drawsModel.dailyBudget.value!!
        val perDayBudget = restBudget / (restDays + skippedDays - 1).toBigDecimal()

        val requireDistributeBudget = perDayBudget * (skippedDays - 1).coerceAtLeast(0).toBigDecimal() + drawsModel.dailyBudget.value!! - drawsModel.spentFromDailyBudget.value!!

        val budgetPerDaySplit = ((restBudget + drawsModel.dailyBudget.value!! - drawsModel.spentFromDailyBudget.value!!) / restDays.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
        val budgetPerDayAdd = (restBudget / restDays.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
        val budgetPerDayAddDailyBudget = budgetPerDayAdd + requireDistributeBudget

        restBudgetOfDayTextView.text = prettyCandyCanes(requireDistributeBudget)

        if (appModel.isDebug.value == true) {
            debugTextView.visibility = View.VISIBLE
            debugTextView.text = "Осталось дней = $restDays " +
                    "\nПрошло дней с последнего пересчета = $skippedDays " +
                    "\nНачало = ${drawsModel.startDate} " +
                    "\nПоследний пересчет = ${drawsModel.lastReCalcBudgetDate} " +
                    "\nКонец = ${drawsModel.finishDate} " +
                    "\nВесь бюджет = ${drawsModel.budget.value!!}" +
                    "\nПотрачено из бюджета = ${drawsModel.spent.value!!}" +
                    "\nБюджет на сегодня = ${drawsModel.dailyBudget.value!!}" +
                    "\nПотрачено из дневного бюджета = ${drawsModel.spentFromDailyBudget.value!!}" +
                    "\nОставшийся бюджет = $restBudget" +
                    "\nОставшийся бюджет на по дням = $perDayBudget"


            splitRestDaysDebugTextView.visibility = View.VISIBLE
            splitRestDaysDebugTextView.text = "($restBudget + ${drawsModel.dailyBudget.value!!} - ${drawsModel.spentFromDailyBudget.value!!}) / $restDays = $budgetPerDaySplit"

            addCurrentDayDebugTextView.visibility = View.VISIBLE
            addCurrentDayDebugTextView.text = "$restBudget / $restDays = $budgetPerDayAdd " +
                    "\n${budgetPerDayAdd} + $requireDistributeBudget = $budgetPerDayAddDailyBudget"
        }

        splitRestDaysDescriptionTextView.text = context!!.getString(
            R.string.split_rest_days_description,
            prettyCandyCanes(budgetPerDaySplit),
        )

        addCurrentDayDescriptionTextView.text = context!!.getString(
            R.string.add_current_day_description,
            prettyCandyCanes(requireDistributeBudget + budgetPerDayAdd),
            prettyCandyCanes(budgetPerDayAdd),
        )

        splitRestDaysCardView.setOnClickListener {
            drawsModel.reCalcDailyBudget(budgetPerDaySplit)

            dismiss()
        }

        addCurrentDayCardView.setOnClickListener {
            drawsModel.reCalcDailyBudget(budgetPerDayAdd + requireDistributeBudget)

            dismiss()
        }
    }
}