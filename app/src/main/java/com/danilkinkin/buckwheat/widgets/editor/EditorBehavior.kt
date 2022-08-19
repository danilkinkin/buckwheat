package com.danilkinkin.buckwheat.widgets.editor

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.getNavigationBarHeight
import com.danilkinkin.buckwheat.utils.toDP
import com.danilkinkin.buckwheat.widgets.topsheet.TopSheetBehavior
import kotlin.math.abs


class EditorBehavior<V: View>: CoordinatorLayout.Behavior<V> {

    companion object {
        val TAG = EditorBehavior::class.simpleName
    }

    private var recyclerView: RecyclerView? = null
    private var isBeingDragged = false
    var initialX = 0
    var initialY = 0
    var lastY = 0

    /**
     * Конструктор для создания экземпляра FancyBehavior через разметку.
     *
     * @param context The {@link Context}.
     * @param attrs The {@link AttributeSet}.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor() : super()

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        child.updateLayoutParams {
            height = parent.height - (parent.width - 16.toDP() + getNavigationBarHeight(child))
        }

        recyclerView = parent.findViewById(R.id.recycle_view)

        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout, child: V, event: MotionEvent
    ): Boolean {
        if (
            parent.isPointInChildBounds(child, event.x.toInt(), event.y.toInt())
            && event.actionMasked == MotionEvent.ACTION_DOWN
        ) {
            initialX = event.x.toInt()
            initialY = event.y.toInt()
            lastY = event.y.toInt()
            isBeingDragged = true
        }

        if (isBeingDragged) {
            Log.d(TAG, "onInterceptTouchEvent action = ${event.actionMasked}")

            if (
                event.actionMasked == MotionEvent.ACTION_UP
                || event.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                isBeingDragged = false
            }

            return if (recyclerView !== null) {
                val topSheetBehavior = ((recyclerView!!.layoutParams as CoordinatorLayout.LayoutParams).behavior as TopSheetBehavior)

                val touchSlop = topSheetBehavior.viewDragHelper!!.touchSlop

                lastY = event.y.toInt()

                abs(initialY - event.y) > touchSlop || abs(initialX - event.x) > touchSlop
            } else {
                true
            }
        }

        return false
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout, child: V, event: MotionEvent
    ): Boolean {
        if (parent.isPointInChildBounds(child, event.x.toInt(), event.y.toInt())) {
            val topSheetBehavior = try {
                ((recyclerView!!.layoutParams as CoordinatorLayout.LayoutParams).behavior as TopSheetBehavior)
            } catch (e: Exception) {
                null
            }

            Log.d(TAG, "event.y = ${event.y.toInt()}")

            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {

                    topSheetBehavior?.drag(lastY - event.y.toInt())
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    topSheetBehavior?.finishDrag()
                }
            }
        }

        lastY = event.y.toInt()

        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency.id == R.id.recycle_view
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        child.translationY = dependency.bottom.toFloat()

        return true
    }
}