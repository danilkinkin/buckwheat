package com.danilkinkin.buckwheat.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

fun getCurrencies(): MutableList<Currency> {
    val currencies = Currency.getAvailableCurrencies().toMutableList()

    currencies.sortBy { it.displayName }

    return currencies
}


class CurrencyAdapter (
    context: Context,
    layoutId: Int,
    private val items: MutableList<Currency>,
) : ArrayAdapter<Currency>(context, layoutId, items), Filterable {

    constructor(context: Context) : this(context, android.R.layout.simple_list_item_single_choice, getCurrencies())

    override fun getCount(): Int = items.size

    override fun getItem(p0: Int): Currency {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return items[p0].numericCode.toLong()
    }

    fun findItemPosition(code: String): Int {
        return items.indexOfFirst { it.currencyCode == code }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if (view == null) {
            val inflater = (context as Activity).layoutInflater
            view = inflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false)
        }

        try {
            val currency: Currency = getItem(position)
            val cityAutoCompleteView = view!!.findViewById<TextView>(android.R.id.text1)
            cityAutoCompleteView.text = currency.displayName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return view!!
    }
}