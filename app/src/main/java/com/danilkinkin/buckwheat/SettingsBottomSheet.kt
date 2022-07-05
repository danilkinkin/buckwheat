package com.danilkinkin.buckwheat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat

class SettingsBottomSheet: BottomSheetDialogFragment() {
    companion object {
        val TAG = SettingsBottomSheet::class.simpleName
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

        requireView().findViewById<MaterialToolbar>(R.id.top_bar).setNavigationOnClickListener {
            this.dismiss()
        }

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
            }

            dataPicker
                .show(parentFragmentManager, "dataPicker")
        }
    }
}