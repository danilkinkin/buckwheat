package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.EditorFragment
import com.danilkinkin.buckwheat.R

class EditorAdapter(
    private val fragmentManager: FragmentManager,
    private val recyclerView: RecyclerView,
) : RecyclerView.Adapter<EditorAdapter.EditorViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class EditorViewHolder(view: View, recyclerView: RecyclerView) : RecyclerView.ViewHolder(view) {
        val flContainer: ConstraintLayout

        init {
            // Define click listener for the ViewHolder's View.
            flContainer = view.findViewById(R.id.container)

            val layout = flContainer.layoutParams

            layout.height = recyclerView.height - recyclerView.width

            flContainer.layoutParams = layout
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EditorViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_editor, viewGroup, false)

        return EditorViewHolder(view, recyclerView)
    }

    override fun onViewAttachedToWindow(holder: EditorViewHolder) {
        attachFragmentToContainer()

        super.onViewAttachedToWindow(holder)
    }

    fun attachFragmentToContainer() {
        val fragment = if (fragmentManager.fragments.firstOrNull { it is EditorFragment } == null)
            EditorFragment()
        else
            null

        if (fragment != null) {
            fragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commitNowAllowingStateLoss()
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: EditorViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 1

}