package com.danilkinkin.buckwheat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.countDays
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.lang.Math.max
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
        val restDays = countDays(drawsModel.toDate)
        val spentDays = abs(countDays(drawsModel.lastReCalcBudgetDate!!))

        val passBudget = drawsModel.wholeBudget.value!! - drawsModel.budgetOfCurrentDay.value!!
        val requireDistributeBudget = floor((drawsModel.restBudget.value!! / (restDays + spentDays - 1)) * (spentDays - 1).coerceAtLeast(
            0
        ) + drawsModel.budgetOfCurrentDay.value!!)

        Log.d(TAG, "passBudget = $passBudget requireDistributeBudget = $requireDistributeBudget wholeBudget = ${drawsModel.wholeBudget.value!!}")
        Log.d(TAG, "restDays = $restDays spentDays = $spentDays")

        val budgetPerDaySplit = floor(drawsModel.wholeBudget.value!! / restDays)
        val budgetPerDayAdd = floor(drawsModel.restBudget.value!! / restDays)

        restBudgetOfDayTextView.text = "$requireDistributeBudget ₽"

        if (appModel.isDebug.value == true) {
            debugTextView.visibility = View.VISIBLE
            debugTextView.text = "Осталось дней = $restDays " +
                    "\nПрошло дней с последнего пересчета = $spentDays " +
                    "\nВесь бюджет = ${drawsModel.wholeBudget.value!!}" +
                    "\nБюджет на сегодня = ${drawsModel.budgetOfCurrentDay.value!!}" +
                    "\nОставшийся бюджет = ${drawsModel.restBudget.value!!}"


            splitRestDaysDebugTextView.visibility = View.VISIBLE
            splitRestDaysDebugTextView.text = "${drawsModel.wholeBudget.value!!} / $restDays = $budgetPerDaySplit"

            addCurrentDayDebugTextView.visibility = View.VISIBLE
            addCurrentDayDebugTextView.text = "${drawsModel.restBudget.value!!} / $restDays = $budgetPerDayAdd " +
                    "\n${requireDistributeBudget} + ${budgetPerDayAdd} = ${requireDistributeBudget + budgetPerDayAdd}"
        }

        splitRestDaysDescriptionTextView.text = context!!.getString(
            R.string.split_rest_days_description,
            "$budgetPerDaySplit ₽",
        )

        addCurrentDayDescriptionTextView.text = context!!.getString(
            R.string.add_current_day_description,
            "${requireDistributeBudget + budgetPerDayAdd} ₽",
            "$budgetPerDayAdd ₽",
        )

        splitRestDaysCardView.setOnClickListener {
            drawsModel.reCalcBudget(budgetPerDaySplit)

            dismiss()
        }

        addCurrentDayCardView.setOnClickListener {
            drawsModel.reCalcBudget(requireDistributeBudget + budgetPerDayAdd)

            dismiss()
        }
    }
}