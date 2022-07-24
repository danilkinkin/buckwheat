package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.prettyDate
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.google.android.material.textview.MaterialTextView

class TopAdapter(private val model: SpentViewModel) : RecyclerView.Adapter<TopAdapter.ViewHolder>() {
    class ViewHolder(view: View, private val model: SpentViewModel) : RecyclerView.ViewHolder(view) {
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

            update()
        }

        fun update() {
            itemView.findViewById<MaterialTextView>(R.id.value).text = prettyCandyCanes(model.budget.value!!)
            itemView.findViewById<MaterialTextView>(R.id.start_date).text = prettyDate(
                model.startDate,
                showTime = false,
                forceShowDate = true,
            )
            itemView.findViewById<MaterialTextView>(R.id.finish_date).text = prettyDate(
                model.finishDate,
                showTime = false,
                forceShowDate = true,
            )
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_top, viewGroup, false)

        return ViewHolder(view, model)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.update()
    }

    override fun getItemCount() = 1
}