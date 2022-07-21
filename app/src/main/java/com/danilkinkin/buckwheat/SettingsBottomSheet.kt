package com.danilkinkin.buckwheat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.countDays
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


class SettingsBottomSheet: BottomSheetDialogFragment() {
    companion object {
        val TAG = SettingsBottomSheet::class.simpleName
    }

    private lateinit var model: AppViewModel
    private lateinit var drawsModel: DrawsViewModel

    var budgetValue: Double = 0.0
    var dateToValue: Date = Date()

    val budgetInput: TextInputEditText by lazy {
        requireView().findViewById(R.id.budget_input)
    }

    val dateRangeInput: AutoCompleteTextView by lazy {
        requireView().findViewById(R.id.date_input)
    }

    val totalDescriptionTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.total_description)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: AppViewModel by activityViewModels()
        val drawsModel: DrawsViewModel by activityViewModels()

        this.model = model
        this.drawsModel = drawsModel

        build()
    }

    fun reCalcBudget() {
        val days = countDays(dateToValue)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

        dateRangeInput.setText(dateFormat.format(dateToValue).toString())

        totalDescriptionTextView.text = context!!.getString(
            R.string.total_description,
            "${prettyCandyCanes(if (days != 0) { floor(budgetValue / days) } else { budgetValue })} ₽",
            "$days",
        )
    }

    fun build() {
        budgetValue = drawsModel.budget.value ?: 0.0
        dateToValue = drawsModel.finishDate
        budgetInput.setText(prettyCandyCanes(budgetValue))

        budgetInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                budgetValue = try {
                    p0.toString().toDouble()
                } catch (e: Exception) {
                    budgetInput.setText(prettyCandyCanes(0.0))

                    0.0
                }

                reCalcBudget()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        reCalcBudget()

        dateRangeInput.setOnClickListener {
            val dataPicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setSelection(
                    androidx.core.util.Pair(
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds() + 15 * 24 * 60 * 60 * 1000,
                    )
                )
                .build()

            dataPicker.addOnPositiveButtonClickListener {
                dateToValue = Date(dataPicker.selection!!.second)

                reCalcBudget()
            }

            dataPicker
                .show(parentFragmentManager, "dataPicker")
        }

        requireView().findViewById<MaterialButton>(R.id.apply).setOnClickListener {
            drawsModel.changeBudget(budgetValue, dateToValue)

            dismiss()
        }
    }
}