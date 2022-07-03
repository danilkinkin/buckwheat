package com.danilkinkin.buckwheat.decorators

import android.graphics.Canvas
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.adapters.KeyboardAdapter
import com.danilkinkin.buckwheat.utils.toDP
import java.lang.Integer.max
import kotlin.math.min

class KeyboardDecorator: RecyclerView.ItemDecoration() {
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val view = parent.children.last()

        if (parent.getChildViewHolder(view) is KeyboardAdapter.KeyboardHolder) {

            val layout = view.layoutParams

            layout.height = view.width

            view.layoutParams = layout


            val containerView = view.findViewById<ConstraintLayout>(R.id.container)

            val layoutContainer = containerView.layoutParams

            layoutContainer.height = min(max(parent.height - view.top, 250.toDP()), view.width)

            containerView.layoutParams = layoutContainer
        }
    }
}