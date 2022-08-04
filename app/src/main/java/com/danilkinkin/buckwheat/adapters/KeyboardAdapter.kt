package com.danilkinkin.buckwheat.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.widgets.keyboard.KeyboardFragment
import com.danilkinkin.buckwheat.R

private var keyboardView: View? = null
private var fragmentKeyboard: KeyboardFragment? = null

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
            flContainer = view.findViewById(R.id.wrapper)

            keyboardView = view

            flContainer.doOnLayout {
                val layout = flContainer.layoutParams

                layout.height = flContainer.width

                flContainer.layoutParams = layout
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

    /* fun scrollUpdate(parent: RecyclerView) {
        val minHeight = 258.toDP()

        val value = (parent.height - keyboardView!!.top - minHeight) / (parent.width.toFloat() - minHeight)

        fragmentKeyboard?.anim(value.coerceAtLeast(0F))
    } */

    private fun attachFragmentToContainer() {
        val fragment = if (fragmentManager.fragments.firstOrNull { it is KeyboardFragment } == null) {
            fragmentKeyboard = KeyboardFragment()

            fragmentKeyboard
        } else
            null

        if (fragment != null) {
            fragmentManager.beginTransaction()
                .add(R.id.wrapper, fragment)
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
