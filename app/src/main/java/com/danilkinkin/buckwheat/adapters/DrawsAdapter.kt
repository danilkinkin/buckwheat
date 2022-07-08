package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.entities.Draw
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat

class DrawsAdapter: ListAdapter<Draw, DrawsAdapter.DrawViewHolder>(DrawDiffCallback) {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class DrawViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val valueTextView: MaterialTextView = itemView.findViewById(R.id.value)
        private val dateTextView: MaterialTextView = itemView.findViewById(R.id.date_input)
        var currentDraw: Draw? = null

        fun bind(draw: Draw) {
            currentDraw = draw

            valueTextView.text = "${draw.value} ₽"

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            dateTextView.text = dateFormat.format(draw.date.time).toString()
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DrawViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_draw, viewGroup, false)

        return DrawViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: DrawViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
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