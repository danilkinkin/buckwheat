package com.danilkinkin.buckwheat

import androidx.appcompat.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.adapters.CurrencyAdapter
import com.danilkinkin.buckwheat.utils.*
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
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
    var currencyValue: ExtendCurrency = ExtendCurrency(value = null, type = CurrencyType.NONE)

    val budgetInput: TextInputEditText by lazy {
        requireView().findViewById(R.id.budget_input)
    }

    private val finishDateBtn: MaterialButton by lazy {
        requireView().findViewById(R.id.edit_finish_date_btn)
    }

    private val currencyToggleBtn: MaterialButtonToggleGroup by lazy {
        requireView().findViewById(R.id.currency_toggle_btn)
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

        finishDateBtn.text = prettyDate(dateToValue, showTime = false, forceShowDate = true)

        totalDescriptionTextView.text = context!!.getString(
            R.string.total_description,
            prettyCandyCanes(
                if (days != 0) { floor(budgetValue / days) } else { budgetValue },
                currency = currencyValue,
            ),
            "$days",
        )
    }

    fun build() {
        budgetValue = drawsModel.budget.value ?: 0.0
        dateToValue = drawsModel.finishDate
        currencyValue = drawsModel.currency

        budgetInput.setText(prettyCandyCanes(budgetValue, currency = ExtendCurrency(type = CurrencyType.NONE)))

        budgetInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                budgetValue = try {
                    p0.toString().toDouble()
                } catch (e: Exception) {
                    budgetInput.setText(prettyCandyCanes(0.0, currency = ExtendCurrency(type = CurrencyType.NONE)))

                    0.0
                }

                reCalcBudget()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        reCalcBudget()

        finishDateBtn.setOnClickListener {
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

        fun recalcLabels(newCurrency: ExtendCurrency) {
            currencyToggleBtn.findViewById<MaterialButton>(R.id.from_list).isChecked = false
            currencyToggleBtn.findViewById<MaterialButton>(R.id.from_list).text = context!!.getString(R.string.currency_from_list)

            currencyToggleBtn.findViewById<MaterialButton>(R.id.custom).isChecked = false
            currencyToggleBtn.findViewById<MaterialButton>(R.id.custom).text = context!!.getString(R.string.currency_custom)

            currencyToggleBtn.findViewById<MaterialButton>(R.id.none).isChecked = false

            when (newCurrency.type) {
                CurrencyType.FROM_LIST -> {
                    currencyToggleBtn.findViewById<MaterialButton>(R.id.from_list).isChecked = true
                    currencyToggleBtn.findViewById<MaterialButton>(R.id.from_list).text = context!!.getString(
                        R.string.currency_from_list_selected,
                        Currency.getInstance(newCurrency.value).symbol,
                    )
                }
                CurrencyType.CUSTOM -> {
                    currencyToggleBtn.findViewById<MaterialButton>(R.id.custom).isChecked = true
                    currencyToggleBtn.findViewById<MaterialButton>(R.id.custom).text = context!!.getString(
                        R.string.currency_custom_selected,
                        newCurrency.value,
                    )
                }
                CurrencyType.NONE -> {
                    currencyToggleBtn.findViewById<MaterialButton>(R.id.none).isChecked = true
                }
            }

            reCalcBudget()
        }

        recalcLabels(currencyValue)

        currencyToggleBtn.findViewById<MaterialButton>(R.id.from_list).setOnClickListener {
            val adapter = CurrencyAdapter(context!!)

            var alertDialog: AlertDialog? = null
            var value: String? = if (currencyValue.type === CurrencyType.FROM_LIST) {
                currencyValue.value
            } else {
                null
            }

            alertDialog = MaterialAlertDialogBuilder(context!!)
                .setTitle(R.string.select_currency_title)
                .setSingleChoiceItems(
                    adapter,
                    value?.let { adapter.findItemPosition(value!!) } ?: -1,
                ) { _: DialogInterface?, position: Int ->
                    val currency = adapter.getItem(position)

                    value = currency.currencyCode

                    alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    recalcLabels(currencyValue)

                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    currencyValue = ExtendCurrency(value, type = CurrencyType.FROM_LIST)

                    recalcLabels(currencyValue)

                    dialog.dismiss()
                }
                .setOnCancelListener { recalcLabels(currencyValue) }
                .create()

            alertDialog.show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }

        currencyToggleBtn.findViewById<MaterialButton>(R.id.custom).setOnClickListener {
            var alertDialog: AlertDialog? = null

            val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_custom_currency, null, false)

            val input = view.findViewById<TextInputEditText>(R.id.currency_input)

            input.setText(if (currencyValue.type === CurrencyType.CUSTOM) currencyValue.value else "")

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !value.isNullOrEmpty()
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            alertDialog = MaterialAlertDialogBuilder(context!!)
                .setTitle(R.string.currency_custom_title)
                .setView(view)
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                    recalcLabels(currencyValue)

                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    currencyValue = ExtendCurrency(value = input.text.toString(), type = CurrencyType.CUSTOM)
                    recalcLabels(currencyValue)
                    dialog.dismiss()
                }
                .setOnCancelListener { recalcLabels(currencyValue) }
                .create()

            alertDialog!!.show()
            alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = currencyValue.type === CurrencyType.CUSTOM && !currencyValue.value.isNullOrEmpty()
        }

        currencyToggleBtn.findViewById<MaterialButton>(R.id.none).setOnClickListener {
            currencyValue = ExtendCurrency(value = null, type = CurrencyType.NONE)

            recalcLabels(currencyValue)
        }

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