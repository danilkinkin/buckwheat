package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.entities.Spent
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.prettyDate
import com.google.android.material.textview.MaterialTextView

class SpendsAdapter: ListAdapter<Spent, SpendsAdapter.DrawViewHolder>(DrawDiffCallback) {
    class DrawViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val valueTextView: MaterialTextView = itemView.findViewById(R.id.value)
        private val dateTextView: MaterialTextView = itemView.findViewById(R.id.date_input)
        var currentSpent: Spent? = null

        fun bind(spent: Spent) {
            currentSpent = spent

            valueTextView.text = prettyCandyCanes(spent.value)

            dateTextView.text = prettyDate(spent.date)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DrawViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_spent, viewGroup, false)

        return DrawViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: DrawViewHolder, position: Int) {
        val spent = getItem(position)
        viewHolder.bind(spent)
    }

}

object DrawDiffCallback : DiffUtil.ItemCallback<Spent>() {
    override fun areItemsTheSame(oldItem: Spent, newItem: Spent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Spent, newItem: Spent): Boolean {
        return oldItem.uid == newItem.uid
    }
}