package com.danilkinkin.buckwheat.utils

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.danilkinkin.buckwheat.MainActivity
import com.google.android.material.textfield.TextInputEditText
import java.lang.Integer.min
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.max


val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

enum class CurrencyType { FROM_LIST, CUSTOM, NONE }
class ExtendCurrency(val value: String? = null, val type: CurrencyType) {
    companion object {
        fun getInstance(value: String?): ExtendCurrency {


            val currency = try {
                Currency.getInstance(value)
            } catch (e: Exception) {
                null
            }

            if (currency !== null) return ExtendCurrency(
                value = value,
                type = CurrencyType.FROM_LIST
            )
            if (value === null || value.isNullOrEmpty() || value == "null") return ExtendCurrency(
                value = null,
                type = CurrencyType.NONE
            )

            return ExtendCurrency(value = value, type = CurrencyType.CUSTOM)
        }
    }
}

fun Double.round(scale: Int): Double =
    BigDecimal(this).setScale(scale, RoundingMode.HALF_EVEN).toDouble()

fun prettyCandyCanes(
    value: BigDecimal,
    forceShowAfterDot: Boolean = false,
    currency: ExtendCurrency = MainActivity.getInstance().model.currency,
): String {
    val formatter = if (currency.type === CurrencyType.FROM_LIST) currencyFormat else numberFormat

    formatter.maximumFractionDigits = if (forceShowAfterDot) 5 else 2
    formatter.minimumFractionDigits = if (forceShowAfterDot) 1 else 0

    if (currency.type === CurrencyType.FROM_LIST) formatter.currency =
        Currency.getInstance(currency.value)

    var formattedValue = formatter.format(value)

    if (currency.type === CurrencyType.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return formattedValue
}

class CurrencyTextWatcher(
    editText: TextInputEditText,
    private val forceShowAfterDot: Boolean = false,
    private val currency: ExtendCurrency = MainActivity.getInstance().model.currency,
    private val onChange: (value: String) -> Unit,
) : TextWatcher {
    private val editTextWeakReference: WeakReference<TextInputEditText>
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        val editText: TextInputEditText = editTextWeakReference.get()!!
        if (editText.text.toString() == "") {
            return
        }
        editText.removeTextChangedListener(this)
        val parsed = parseCurrencyValue(editText.text.toString(), currency)
        val formatted = prettyCandyCanes(parsed, forceShowAfterDot, currency)
        val selectionStart = editText.selectionStart
        val prevText = editText.text.toString()

        val offset = max(offsetDiff(formatted, prevText) + 1, 0)
        val newSelection = formatted.length - (prevText.length - selectionStart)

        onChange(parsed.toString())

        editText.setText(formatted)

        if (offset == selectionStart) {
            editText.setSelection(max(min(newSelection - 1, formatted.length), 0))
        } else {
            editText.setSelection(max(min(newSelection, formatted.length), 0))
        }
        editText.addTextChangedListener(this)
    }

    companion object {
        fun offsetDiff(stringA: String, stringB: String): Int {
            val minLength = if (stringA.length < stringB.length) {
                stringA.length
            } else {
                stringB.length
            }

            var diffStartIndex = 0

            for (i in 0 until minLength) {
                if (stringA[i] == stringB[i]) {
                    diffStartIndex = i
                } else {
                    return diffStartIndex
                }
            }

            return -1
        }

        fun parseCurrencyValue(value: String, currency: ExtendCurrency): BigDecimal {
            try {
                val replaceRegex = java.lang.String.format(
                    "[%s,.\\s]",
                    when (currency.type) {
                        CurrencyType.CUSTOM -> currency.value
                        CurrencyType.NONE -> ""
                        CurrencyType.FROM_LIST -> Currency.getInstance(currency.value).symbol
                    }
                )
                val currencyValue = value.replace(replaceRegex.toRegex(), "")

                return currencyValue.toBigDecimal()
            } catch (e: java.lang.Exception) {
                Log.e("MyApp", e.message, e)
            }

            return 0.0.toBigDecimal()
        }
    }

    init {
        editTextWeakReference = WeakReference(editText)
    }
}