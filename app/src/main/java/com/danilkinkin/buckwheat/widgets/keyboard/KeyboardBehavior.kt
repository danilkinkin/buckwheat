package com.danilkinkin.buckwheat.widgets.keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.danilkinkin.buckwheat.R


class KeyboardBehavior<V: View>: CoordinatorLayout.Behavior<V> {

    companion object {
        val TAG = KeyboardBehavior::class.simpleName
    }

    private var dependencyIdReference: Int? = null

    /**
     * Конструктор для создания экземпляра FancyBehavior через разметку.
     *
     * @param context The {@link Context}.
     * @param attrs The {@link AttributeSet}.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomCollapseBehavior, 0, 0)

        val idReference = a.getResourceId(R.styleable.BottomCollapseBehavior_layout_behavior_dependency, 0)

        Log.d(TAG, "constructor idReference = ${idReference}")

        if (idReference != 0) {
            dependencyIdReference = idReference
        }
    }

    constructor() : super()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency.id == dependencyIdReference
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        val dependency = parent.findViewById<View>(dependencyIdReference!!)

        child.updateLayoutParams {
            height = parent.width
        }

        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        /* val tY = dependency.getTranslationY()
        val depHeight = dependency.getHeight()
        val bottom = dependency.bottom
        Log.d(TAG, "onDependentViewChanged tY = $tY height = $depHeight bottom = $bottom")
        child.top = bottom
        /* child.updateLayoutParams {
            height = depHeight - bottom
        } */

        return true */

        return super.onDependentViewChanged(parent, child, dependency)
    }
}