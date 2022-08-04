package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.widgets.editor.EditorFragment
import com.danilkinkin.buckwheat.R

class EditorAdapter(
    private val fragmentManager: FragmentManager,
    private val recyclerView: RecyclerView,
) : RecyclerView.Adapter<EditorAdapter.EditorViewHolder>() {
    class EditorViewHolder(view: View, recyclerView: RecyclerView) : RecyclerView.ViewHolder(view) {
        /* val flContainer: ConstraintLayout

        init {
            flContainer = view.findViewById(R.id.container)

            val layout = flContainer.layoutParams

            layout.height = recyclerView.width

            flContainer.layoutParams = layout
        } */
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EditorViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_editor, viewGroup, false)

        return EditorViewHolder(view, recyclerView)
    }

    override fun onViewAttachedToWindow(holder: EditorViewHolder) {
        attachFragmentToContainer()

        super.onViewAttachedToWindow(holder)
    }

    private fun attachFragmentToContainer() {
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

    override fun onBindViewHolder(viewHolder: EditorViewHolder, position: Int) {

    }

    override fun getItemCount() = 1

}