package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.prettyDate
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.textview.MaterialTextView

class TopAdapter(private val model: DrawsViewModel) : RecyclerView.Adapter<TopAdapter.ViewHolder>() {
    class ViewHolder(view: View, model: DrawsViewModel) : RecyclerView.ViewHolder(view) {
        init {
            val resourceId = view.resources.getIdentifier("status_bar_height", "dimen", "android")
            val topBarHeight = if (resourceId > 0) {
                view.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }

            val helperView = view.findViewById<View>(R.id.root)

            helperView.setPadding(
                0,
                topBarHeight,
                0,
                0,
            )

            view.findViewById<MaterialTextView>(R.id.value).text = "${prettyCandyCanes(model.wholeBudget.value!!)} ₽"
            view.findViewById<MaterialTextView>(R.id.date_input).text = prettyDate(model.startDate, forceShowDate = true)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_top, viewGroup, false)

        return ViewHolder(view, model)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

    }

    override fun getItemCount() = 1
}