package com.danilkinkin.buckwheat.decorators

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.adapters.DrawsAdapter
import com.danilkinkin.buckwheat.utils.toDP

class DrawsDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        dividerPaint.color = ContextCompat.getColor(context, com.google.android.material.R.color.material_divider_color)
        dividerPaint.strokeWidth = 1.5.toDP().toFloat()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        val left = parent.paddingLeft + 0.toDP()
        val right = parent.width - parent.paddingRight - 0.toDP()

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)

            if (
                parent.getChildViewHolder(child) is DrawsAdapter.DrawViewHolder
                || (nextChild !== null && parent.getChildViewHolder(nextChild) is DrawsAdapter.DrawViewHolder)
            ) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin

                c.drawLine(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    top.toFloat(),
                    dividerPaint,
                )
            }
        }
    }
}