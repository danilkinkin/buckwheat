package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.KeyboardFragment
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.toDP
import java.lang.Integer.min
import java.lang.Math.max

private var keyboardView: View? = null

class KeyboardAdapter(
    private val fragmentManager: FragmentManager,
    val lockScroll: (lock: Boolean) -> Unit,
) : RecyclerView.Adapter<KeyboardAdapter.KeyboardHolder>() {
    private var isStartInKeyboard = false
    private var isStartInOutside = false

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class KeyboardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val flContainer: ConstraintLayout

        init {
            // Define click listener for the ViewHolder's View.
            flContainer = view.findViewById(R.id.container)

            keyboardView = view

            val wrapperView = view.findViewById<ConstraintLayout>(R.id.wrapper)

            wrapperView.doOnLayout {
                val layout = wrapperView.layoutParams

                layout.height = wrapperView.width

                wrapperView.layoutParams = layout
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): KeyboardHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_keyboard, viewGroup, false)


        viewGroup.setOnTouchListener { viewClick, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                isStartInKeyboard = false
                isStartInOutside = false
            }

            if (motionEvent.y < view.y || isStartInOutside) {
                if (motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (!isStartInKeyboard) isStartInOutside = true

                    if (isStartInOutside) this.lockScroll(false)
                }

                return@setOnTouchListener false
            }

            when (motionEvent.action){
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (!isStartInOutside) isStartInKeyboard = true
                }
                MotionEvent.ACTION_UP -> {
                    viewClick.performClick()
                }
            }

            if (isStartInKeyboard) this.lockScroll(true)

            return@setOnTouchListener false
        }

        return KeyboardHolder(view)
    }

    override fun onViewAttachedToWindow(holder: KeyboardHolder) {
        attachFragmentToContainer()

        super.onViewAttachedToWindow(holder)
    }

    fun scrollUpdate(parent: RecyclerView) {
        keyboardView?.let {
            val containerView = it.findViewById<ConstraintLayout>(R.id.container)

            val layoutContainer = containerView.layoutParams

            layoutContainer.height = min(max(parent.height - it.top, 300.toDP()), containerView.width)

            containerView.layoutParams = layoutContainer
        }
    }

    fun attachFragmentToContainer() {
        val fragment = if (fragmentManager.fragments.firstOrNull { it is KeyboardFragment } == null)
            KeyboardFragment()
        else
            null

        if (fragment != null) {
            fragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commitNowAllowingStateLoss()
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: KeyboardHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 1

}
