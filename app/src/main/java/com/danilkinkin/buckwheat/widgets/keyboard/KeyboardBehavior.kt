package com.danilkinkin.buckwheat.widgets.keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.getNavigationBarHeight
import com.danilkinkin.buckwheat.utils.toDP
import kotlin.math.max
import kotlin.math.min


class KeyboardBehavior<V: View>: CoordinatorLayout.Behavior<V> {

    companion object {
        val TAG = KeyboardBehavior::class.simpleName
    }

    private var navigationBarHeight: Int? = null

    /**
     * Конструктор для создания экземпляра FancyBehavior через разметку.
     *
     * @param context The {@link Context}.
     * @param attrs The {@link AttributeSet}.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor() : super()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency.id == R.id.editor_container
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        navigationBarHeight = getNavigationBarHeight(child)

        child.updateLayoutParams {
            height = parent.width - 16.toDP() + navigationBarHeight!!

        }

        child.setPadding(
            16.toDP(),
            16.toDP(),
            16.toDP(),
            navigationBarHeight!!,
        )

        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        child.translationY = dependency.translationY

        val height = 226.toDP() + child.paddingTop + child.paddingBottom

        child.findViewById<MotionLayout>(R.id.root)?.progress = max(
            min(
                1 - ((parent.height - (dependency.bottom + dependency.translationY) - height) / (parent.width - height)),
                0.999999F,
            ),
            0.000001F,
        )

        return super.onDependentViewChanged(parent, child, dependency)
    }
}