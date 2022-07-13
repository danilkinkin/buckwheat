package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R

class TopAdapter() : RecyclerView.Adapter<TopAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_top, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 1
}