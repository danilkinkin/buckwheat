package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.entities.Draw
import com.danilkinkin.buckwheat.utils.prettyDate
import com.google.android.material.textview.MaterialTextView

class DrawsAdapter: ListAdapter<Draw, DrawsAdapter.DrawViewHolder>(DrawDiffCallback) {
    class DrawViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val valueTextView: MaterialTextView = itemView.findViewById(R.id.value)
        private val dateTextView: MaterialTextView = itemView.findViewById(R.id.date_input)
        var currentDraw: Draw? = null

        fun bind(draw: Draw) {
            currentDraw = draw

            valueTextView.text = "${draw.value} ₽"

            dateTextView.text = prettyDate(draw.date)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DrawViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_draw, viewGroup, false)

        return DrawViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: DrawViewHolder, position: Int) {
        val draw = getItem(position)
        viewHolder.bind(draw)
    }

}

object DrawDiffCallback : DiffUtil.ItemCallback<Draw>() {
    override fun areItemsTheSame(oldItem: Draw, newItem: Draw): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Draw, newItem: Draw): Boolean {
        return oldItem.uid == newItem.uid
    }
}