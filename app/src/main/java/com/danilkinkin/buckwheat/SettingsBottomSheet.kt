package com.danilkinkin.buckwheat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*


class SettingsBottomSheet: BottomSheetDialogFragment() {
    companion object {
        val TAG = SettingsBottomSheet::class.simpleName
    }

    private lateinit var model: AppViewModel
    private lateinit var drawsModel: DrawsViewModel

    var budgetValue: Double? = null
    var dateToValue: Date? = null

    val budgetInput: TextInputEditText by lazy {
        requireView().findViewById(R.id.budget_input)
    }

    val dateRangeInput: AutoCompleteTextView by lazy {
        requireView().findViewById(R.id.date_input)
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

    fun build() {
        budgetValue = drawsModel.budgetValue.value
        dateToValue = drawsModel.toDate
        budgetInput.setText(budgetValue.toString())

        budgetInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                budgetValue = try {
                    p0.toString().toDouble()
                } catch (e: Exception) {
                    budgetInput.setText((0.0).toString())

                    0.0
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

        dateRangeInput.setText(dateFormat.format(dateToValue!!).toString())

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
                Log.d(TAG, dataPicker.selection.toString())

                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

                dateRangeInput.setText(dateFormat.format(dataPicker.selection!!.second).toString())

                dateToValue = Date(dataPicker.selection!!.second)
            }

            dataPicker
                .show(parentFragmentManager, "dataPicker")
        }

        requireView().findViewById<MaterialButton>(R.id.apply).setOnClickListener {
            drawsModel.changeBudget(budgetValue!!, dateToValue!!)

            dismiss()
        }
    }
}