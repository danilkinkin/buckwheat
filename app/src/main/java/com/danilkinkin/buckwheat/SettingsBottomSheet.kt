package com.danilkinkin.buckwheat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.adapters.CurrencyAdapter
import com.danilkinkin.buckwheat.utils.countDays
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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
    var currencyValue: String? = null

    val budgetInput: TextInputEditText by lazy {
        requireView().findViewById(R.id.budget_input)
    }

    private val dateRangeInput: AutoCompleteTextView by lazy {
        requireView().findViewById(R.id.date_input)
    }

    private val currencyInput: AutoCompleteTextView by lazy {
        requireView().findViewById(R.id.currency_input)
    }

    private val totalDescriptionTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.total_description)
    }

    private val openSiteBtn: MaterialCardView by lazy {
        requireView().findViewById(R.id.site)
    }

    private val reportBugBtn: MaterialCardView by lazy {
        requireView().findViewById(R.id.report_bug)
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
            prettyCandyCanes(
                if (days != 0) { floor(budgetValue / days) } else { budgetValue },
                currency = if (currencyValue !== null) Currency.getInstance(currencyValue) else null,
            ),
            "$days",
        )
    }

    fun build() {
        budgetValue = drawsModel.budget.value ?: 0.0
        dateToValue = drawsModel.finishDate
        currencyValue = drawsModel.currency?.currencyCode

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

        val adapter = CurrencyAdapter(
            context!!,
            Currency.getAvailableCurrencies().toMutableList(),
        )

        currencyInput.setAdapter(adapter)
        currencyInput.setOnItemClickListener { parent, _, position, _ ->
            val currency = adapter.getItem(position)

            currencyInput.setText(currency.displayName)
            currencyValue = currency.currencyCode

            reCalcBudget()
        }
        currencyInput.setText(drawsModel.currency?.displayName)

        openSiteBtn.setOnClickListener {
            val url = "https://danilkinkin.com"
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(url)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val clipboard = getSystemService(context!!, ClipboardManager::class.java) as ClipboardManager

                clipboard.setPrimaryClip(ClipData.newPlainText( "url", url))

                Toast
                    .makeText(context, context!!.getString(R.string.copy_in_clipboard), Toast.LENGTH_LONG)
                    .show()
            }
        }

        reportBugBtn.setOnClickListener {
            val url = "https://github.com/danilkinkin/buckweat/issues"
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(url)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val clipboard = getSystemService(context!!, ClipboardManager::class.java) as ClipboardManager

                clipboard.setPrimaryClip(ClipData.newPlainText( "url", url))

                Toast
                    .makeText(context, context!!.getString(R.string.copy_in_clipboard), Toast.LENGTH_LONG)
                    .show()
            }
        }

        requireView().findViewById<MaterialButton>(R.id.apply).setOnClickListener {
            drawsModel.changeCurrency(currencyValue)
            drawsModel.changeBudget(budgetValue, dateToValue)

            dismiss()
        }
    }
}