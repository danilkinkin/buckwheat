package com.danilkinkin.buckwheat

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.adapters.CurrencyAdapter
import com.danilkinkin.buckwheat.utils.*
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.danilkinkin.buckwheat.widgets.bottomsheet.BottomSheetFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class WalletBottomSheet : BottomSheetFragment() {
    companion object {
        val TAG = WalletBottomSheet::class.simpleName
    }

    private lateinit var model: AppViewModel
    private lateinit var spendsModel: SpentViewModel

    private var budgetValue: BigDecimal = 0.0.toBigDecimal()
    private var dateToValue: Date = Date()
    private var currencyValue: ExtendCurrency =
        ExtendCurrency(value = null, type = CurrencyType.NONE)

    private val budgetInput: TextInputEditText by lazy {
        requireView().findViewById(R.id.budget_input)
    }

    private val finishDateBtn: ConstraintLayout by lazy {
        requireView().findViewById(R.id.finish_date_edit_btn)
    }

    private val currencyFromListBtn: ConstraintLayout by lazy {
        requireView().findViewById(R.id.currency_from_list_btn)
    }

    private val currencyCustomBtn: ConstraintLayout by lazy {
        requireView().findViewById(R.id.currency_custom_btn)
    }

    private val currencyNoneBtn: ConstraintLayout by lazy {
        requireView().findViewById(R.id.currency_none_btn)
    }

    private val perDayTextView: MaterialTextView by lazy {
        requireView().findViewById(R.id.per_day_description)
    }

    private val applyBtn: MaterialButton by lazy {
        requireView().findViewById(R.id.apply)
    }

    private val dragHelperView: View by lazy {
        requireView().findViewById(R.id.drag_helper)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: AppViewModel by activityViewModels()
        val spendsModel: SpentViewModel by activityViewModels()

        this.model = model
        this.spendsModel = spendsModel

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.findViewById<LinearLayout>(R.id.content).setPadding(0, 0, 0, insets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        build()
    }

    private fun reCalcBudget() {
        val days = countDays(dateToValue)

        finishDateBtn.findViewById<TextView>(R.id.finish_date_label).text = String.format(
            requireContext().resources.getQuantityText(R.plurals.finish_date_dates, days)
                .toString(),
            prettyDate(dateToValue, showTime = false, forceShowDate = true),
            days,
        )

        perDayTextView.text = requireContext().getString(
            R.string.per_day,
            prettyCandyCanes(
                if (days != 0) {
                    (budgetValue / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
                } else {
                    budgetValue
                },
                currency = currencyValue,
            ),
        )

        applyBtn.isEnabled = days > 0 && budgetValue > BigDecimal(0)
        isCancelable = spendsModel.lastReCalcBudgetDate !== null
    }

    private fun recalcLabels(newCurrency: ExtendCurrency) {
        val fromListIcon = currencyFromListBtn.findViewById<ImageView>(R.id.currency_from_list_icon)
        val fromListLabel = currencyFromListBtn.findViewById<TextView>(R.id.currency_from_list_label)
        val customIcon = currencyCustomBtn.findViewById<ImageView>(R.id.currency_custom_icon)
        val customLabel = currencyCustomBtn.findViewById<TextView>(R.id.currency_custom_label)
        val noneIcon = currencyNoneBtn.findViewById<ImageView>(R.id.currency_none_icon)

        fromListIcon.visibility = View.INVISIBLE
        fromListLabel.text = requireContext().getString(R.string.currency_from_list)
        customIcon.visibility = View.INVISIBLE
        customLabel.text = requireContext().getString(R.string.currency_custom)
        noneIcon.visibility = View.INVISIBLE

        when (newCurrency.type) {
            CurrencyType.FROM_LIST -> {
                fromListIcon.visibility = View.VISIBLE
                fromListLabel.text = requireContext().getString(
                    R.string.currency_from_list_selected,
                    Currency.getInstance(newCurrency.value).symbol,
                )
            }
            CurrencyType.CUSTOM -> {
                customIcon.visibility = View.VISIBLE
                customLabel.text = requireContext().getString(
                    R.string.currency_custom_selected,
                    newCurrency.value,
                )
            }
            CurrencyType.NONE -> {
                noneIcon.visibility = View.VISIBLE
            }
        }

        reCalcBudget()
    }

    private fun build() {
        budgetValue = spendsModel.budget.value ?: 0.0.toBigDecimal()
        dateToValue = spendsModel.finishDate
        currencyValue = spendsModel.currency

        dragHelperView.visibility = if (spendsModel.lastReCalcBudgetDate !== null) {
            View.VISIBLE
        } else {
            View.GONE
        }

        budgetInput.setText(
            prettyCandyCanes(
                budgetValue,
                currency = ExtendCurrency(type = CurrencyType.NONE)
            )
        )

        budgetInput.addTextChangedListener(CurrencyTextWatcher(
            budgetInput,
            currency = ExtendCurrency(type = CurrencyType.NONE),
        ) {
            budgetValue = try {
                it.toBigDecimal()
            } catch (e: Exception) {
                budgetInput.setText(
                    prettyCandyCanes(
                        0.0.toBigDecimal(),
                        currency = ExtendCurrency(type = CurrencyType.NONE)
                    )
                )

                0.0.toBigDecimal()
            }

            reCalcBudget()
        })

        finishDateBtn.setOnClickListener {
            val alertDialog: AlertDialog

            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_date_range_picker, null, false)

            val startDate = view.findViewById<MaterialTextView>(R.id.start_date)
            val finishDate = view.findViewById<MaterialTextView>(R.id.finish_date)
            val calendar = view.findViewById<CalendarView>(R.id.calendar)

            var finishDateTemp = dateToValue

            fun recalcFinishDate() {
                val days = countDays(finishDateTemp, spendsModel.startDate)

                finishDate.text = String.format(
                    requireContext().resources.getQuantityText(R.plurals.finish_date_dates, days)
                        .toString(),
                    prettyDate(finishDateTemp, showTime = false, forceShowDate = true),
                    days,
                )
            }

            startDate.text =
                prettyDate(spendsModel.startDate, showTime = false, forceShowDate = true)
            recalcFinishDate()

            calendar.minDate = roundToDay(Date()).time + DAY
            calendar.date = dateToValue.time
            calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
                finishDateTemp = Calendar
                    .Builder()
                    .setTimeZone(TimeZone.getDefault())
                    .setDate(year, month, dayOfMonth)
                    .build()
                    .time

                recalcFinishDate()
            }

            alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_finish_date_title)
                .setView(view)
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    dateToValue = finishDateTemp

                    reCalcBudget()

                    dialog.dismiss()
                }
                .setOnCancelListener { recalcLabels(currencyValue) }
                .create()

            alertDialog.show()
        }

        reCalcBudget()
        recalcLabels(currencyValue)

        currencyFromListBtn.setOnClickListener {
            val adapter = CurrencyAdapter(requireContext())

            var alertDialog: AlertDialog? = null
            var value: String? = if (currencyValue.type === CurrencyType.FROM_LIST) {
                currencyValue.value
            } else {
                null
            }

            alertDialog = MaterialAlertDialogBuilder(requireContext())
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

            alertDialog!!.show()
            alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }

        currencyCustomBtn.setOnClickListener {
            var alertDialog: AlertDialog? = null

            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_currency, null, false)

            val input = view.findViewById<TextInputEditText>(R.id.currency_input)

            input.setText(if (currencyValue.type === CurrencyType.CUSTOM) currencyValue.value else "")

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        !value.isNullOrEmpty()
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.currency_custom_title)
                .setView(view)
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    recalcLabels(currencyValue)

                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    currencyValue =
                        ExtendCurrency(value = input.text.toString(), type = CurrencyType.CUSTOM)
                    recalcLabels(currencyValue)
                    dialog.dismiss()
                }
                .setOnCancelListener { recalcLabels(currencyValue) }
                .create()

            alertDialog!!.show()
            alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                currencyValue.type === CurrencyType.CUSTOM && !currencyValue.value.isNullOrEmpty()
        }

        currencyNoneBtn.setOnClickListener {
            currencyValue = ExtendCurrency(value = null, type = CurrencyType.NONE)

            recalcLabels(currencyValue)
        }

        applyBtn.setOnClickListener {
            spendsModel.changeCurrency(currencyValue)
            spendsModel.changeBudget(budgetValue, dateToValue)

            dismiss()
        }
    }
}