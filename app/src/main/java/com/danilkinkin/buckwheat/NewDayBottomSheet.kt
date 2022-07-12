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
import kotlin.math.floor

class NewDayBottomSheet: BottomSheetDialogFragment() {
    companion object {
        val TAG = NewDayBottomSheet::class.simpleName
    }

    private lateinit var model: AppViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet_new_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: AppViewModel by activityViewModels()
        val drawsModel: DrawsViewModel by activityViewModels()

        this.model = model
        this.drawsModel = drawsModel

        build()
    }

    fun build() {
        val restDays = countDays(drawsModel.toDate)
        val spentDays = countDays(drawsModel.lastReCalcBudgetDate!!)

        val passBudget = drawsModel.wholeBudget.value!! - drawsModel.budgetOfCurrentDay.value!!
        val requireDistributeBudget = floor((passBudget / (restDays + spentDays)) * spentDays + drawsModel.budgetOfCurrentDay.value!!)

        Log.d(TAG, "passBudget = $passBudget requireDistributeBudget = $requireDistributeBudget wholeBudget = ${drawsModel.wholeBudget.value!!}")
        Log.d(TAG, "restDays = $restDays spentDays = $spentDays")

        val budgetPerDaySplit = floor(drawsModel.wholeBudget.value!! / restDays)
        val budgetPerDayAdd = floor((drawsModel.wholeBudget.value!! - requireDistributeBudget) / restDays)

        restBudgetOfDayTextView.text = "$requireDistributeBudget ₽"

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