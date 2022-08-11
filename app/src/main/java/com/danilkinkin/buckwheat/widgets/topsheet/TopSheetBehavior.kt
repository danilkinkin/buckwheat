package com.danilkinkin.buckwheat.widgets.topsheet

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.view.AbsSavedState
import androidx.customview.widget.ViewDragHelper
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class TopSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<V>(context, attrs) {

    companion object {
        private val TAG = TopSheetBehavior::class.java.simpleName

        enum class State {
            STATE_DRAGGING, /** The bottom sheet is dragging.  */
            STATE_SETTLING, /** The bottom sheet is settling.  */
            STATE_EXPANDED, /** The bottom sheet is expanded.  */
            STATE_HIDDEN, /** The bottom sheet is hidden.  */
        }

        private const val SIGNIFICANT_VEL_THRESHOLD = 500
        private const val HIDE_THRESHOLD = 0.5f
        private const val HIDE_FRICTION = 0.1f
        private const val CORNER_ANIMATION_DURATION = 500
    }

    private var maximumVelocity = 0f

    private var settleRunnable: SettleRunnable? = null

    var state = State.STATE_HIDDEN
    var viewDragHelper: ViewDragHelper? = null
    private var ignoreEvents = false
    private var lastNestedScrollDy = 0
    private var nestedScrolled = false
    private var childHeight = 0
    private var parentWidth = 0
    var parentHeight = 0
    var viewRef: WeakReference<View>? = null
    var nestedScrollingChildRef: WeakReference<View?>? = null
    private var velocityTracker: VelocityTracker? = null
    var activePointerId = 0
    private var initialY = 0
    var touchingScrollingChild = false

    init {
        val configuration = ViewConfiguration.get(context)
        maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable {
        return SavedState(super.onSaveInstanceState(parent, child), this)
    }

    override fun onRestoreInstanceState(
        parent: CoordinatorLayout, child: V, state: Parcelable
    ) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.superState!!)

        // Intermediate states are restored as collapsed state
        if (ss.state == State.STATE_DRAGGING || ss.state == State.STATE_SETTLING) {
            this.state = State.STATE_EXPANDED
        } else {
            this.state = ss.state
        }
    }

    override fun onAttachedToLayoutParams(layoutParams: CoordinatorLayout.LayoutParams) {
        super.onAttachedToLayoutParams(layoutParams)
        // These may already be null, but just be safe, explicitly assign them. This lets us know the
        // first time we layout with this behavior by checking (viewRef == null).
        viewRef = null
        viewDragHelper = null
    }

    override fun onDetachedFromLayoutParams() {
        super.onDetachedFromLayoutParams()
        // Release references so we don't run unnecessary codepaths while not attached to a view.
        viewRef = null
        viewDragHelper = null
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child as View)) {
            child.fitsSystemWindows = true
        }
        if (viewRef == null) {
            viewRef = WeakReference(child)

            if (ViewCompat.getImportantForAccessibility(child as View)
                == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            ) {
                ViewCompat.setImportantForAccessibility(
                    child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
                )
            }
        }
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, dragCallback)
        }

        // First let the parent lay it out
        parent.onLayoutChild(child as View, layoutDirection)
        // Offset the bottom sheet
        parentWidth = parent.width
        parentHeight = parent.height
        childHeight = child.height

        when (state) {
            State.STATE_HIDDEN -> {
                Log.d(TAG, "offsetTopAndBottom = $childHeight")
                ViewCompat.offsetTopAndBottom(child, -childHeight)
            }
            State.STATE_EXPANDED, State.STATE_DRAGGING, State.STATE_SETTLING -> {
                Log.d(TAG, "offsetTopAndBottom = $0")
                ViewCompat.offsetTopAndBottom(child, 0)
            }
        }

        nestedScrollingChildRef = WeakReference(findScrollingChild(child))
        return true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent action = ${event.actionMasked}")
        if (!child.isShown) {
            ignoreEvents = true
            return false
        }
        val action = event.actionMasked
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchingScrollingChild = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                // Reset the ignore flag
                if (ignoreEvents) {
                    ignoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX = event.x.toInt()
                initialY = event.y.toInt()
                // Only intercept nested scrolling events here if the view not being moved by the
                // ViewDragHelper.
                if (state != State.STATE_SETTLING) {
                    val scroll =
                        if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
                        activePointerId = event.getPointerId(event.actionIndex)
                        touchingScrollingChild = true
                    }
                }
                ignoreEvents = ((activePointerId == MotionEvent.INVALID_POINTER_ID)
                        && !parent.isPointInChildBounds(child, initialX, initialY))
            }
            else -> {}
        }
        if (!ignoreEvents
            && viewDragHelper != null && viewDragHelper!!.shouldInterceptTouchEvent(event)
        ) {
            return true
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
        return (action == MotionEvent.ACTION_MOVE && scroll != null && !ignoreEvents
                && state != State.STATE_DRAGGING && !parent.isPointInChildBounds(
            scroll,
            event.x.toInt(),
            event.y.toInt()
        )
                && viewDragHelper != null && abs(initialY - event.y) > viewDragHelper!!.touchSlop)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent action = ${event.actionMasked}")
        if (!child.isShown) {
            return false
        }
        val action = event.actionMasked
        if (state == State.STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (viewDragHelper != null) {
            viewDragHelper!!.processTouchEvent(event)
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (viewDragHelper != null && action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
            if (abs(initialY - event.y) > viewDragHelper!!.touchSlop) {
                viewDragHelper!!.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !ignoreEvents
    }

    fun drag(dy: Int) {
        this.viewRef?.get()?.let {
            Log.d(TAG, "drag dy = ${dy} end = ${it.bottom - dy}")
            if (it.bottom - dy < 0) {
                Log.d(TAG, "offsetTopAndBottom = ${it.bottom}")
                ViewCompat.offsetTopAndBottom(it, -it.bottom)
            } else {
                Log.d(TAG, "offsetTopAndBottom = $dy")
                ViewCompat.offsetTopAndBottom(it, min(-dy, (it.height - it.bottom)))
            }

            setSmartStateInternal(State.STATE_DRAGGING)

            lastNestedScrollDy = dy
            nestedScrolled = true
        }
    }

    fun finishDrag(target: View? =  null) {
        Log.d(TAG, "finishDrag")
        this.viewRef?.get()?.let { child ->
            if (child.top == 0) {
                setSmartStateInternal(State.STATE_EXPANDED)
                return
            }
            if (
                (target !== null && (nestedScrollingChildRef == null ||
                target !== nestedScrollingChildRef!!.get())) ||
                !nestedScrolled
            ) {
                return
            }
            val bottom: Int
            val targetSmartState: State
            if (lastNestedScrollDy >= 0 && shouldHide(child, yVelocity)) {
                bottom = 0
                targetSmartState = State.STATE_HIDDEN
            } else {
                bottom = childHeight
                targetSmartState = State.STATE_EXPANDED
            }
            Log.d(TAG, "startSettlingAnimation 1")
            startSettlingAnimation(
                child,
                targetSmartState,
                bottom - childHeight,
                false,
            )
            nestedScrolled = false
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        lastNestedScrollDy = 0
        nestedScrolled = false
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            Log.d(TAG, "onNestedPreScroll skip non touch")
            // Ignore fling here. The ViewDragHelper handles it.
            return
        }
        val scrollingChild =
            if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
        if (target !== scrollingChild) {
            Log.d(TAG, "onNestedPreScroll skip wrong target")
            return
        }
        val currentBottom = child.bottom
        val newBottom = currentBottom - dy
        Log.d(TAG, "onNestedPreScroll dy = $dy currentBottom = $currentBottom")
        if (dy > 0) { // Upward - Collapsing the top sheet!
            Log.d(TAG, "Upward newBottom = $newBottom childHeight = $childHeight")
            if (!target.canScrollVertically(1)) {
                consumed[1] = dy
                Log.d(TAG, "offsetTopAndBottom = $dy")
                ViewCompat.offsetTopAndBottom(child, -dy)
                setSmartStateInternal(State.STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            Log.d(TAG, "Downward newBottom = $newBottom `childHeight` = $childHeight")
            if (newBottom > childHeight) {
                consumed[1] = currentBottom - childHeight
                Log.d(TAG, "offsetTopAndBottom = ${consumed[1]}")
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setSmartStateInternal(State.STATE_EXPANDED)
            } else {
                consumed[1] = dy
                Log.d(TAG, "offsetTopAndBottom = $dy")
                ViewCompat.offsetTopAndBottom(child, -dy)
                setSmartStateInternal(State.STATE_DRAGGING)
            }
        }

        lastNestedScrollDy = dy
        nestedScrolled = true
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        type: Int
    ) {
        Log.d(TAG, "onStopNestedScroll")

        finishDrag(target)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return if (nestedScrollingChildRef != null) {
            (target === nestedScrollingChildRef!!.get()
                    && (state != State.STATE_EXPANDED
                    || super.onNestedPreFling(
                coordinatorLayout,
                child,
                target,
                velocityX,
                velocityY
            )))
        } else {
            false
        }
    }

    fun setSmartStateInternal(state: State) {
        if (this.state == state) {
            return
        }
        this.state = state
        if (viewRef == null) {
            return
        }

        viewRef!!.get() ?: return
    }

    private fun reset() {
        activePointerId = ViewDragHelper.INVALID_POINTER
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    fun shouldHide(child: View, yvel: Float): Boolean {
        if (child.bottom > childHeight) {
            // It should not hide, but collapse.
            return false
        }
        val newBottom = child.top + yvel * HIDE_FRICTION
        return abs(newBottom - childHeight) / childHeight.toFloat() > HIDE_THRESHOLD
    }

    @VisibleForTesting
    fun findScrollingChild(view: View?): View? {
        if (ViewCompat.isNestedScrollingEnabled(view!!)) {
            return view
        }
        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    private val yVelocity: Float
        get() {
            if (velocityTracker == null) {
                return 0F
            }
            velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity)
            return velocityTracker!!.getYVelocity(activePointerId)
        }

    fun startSettlingAnimation(
        child: View,
        state: State,
        top: Int,
        settleFromViewDragHelper: Boolean
    ) {
        Log.d(TAG, "startSettlingAnimation... state = $state top = $top settleFromViewDragHelper = $settleFromViewDragHelper")
        val startedSettling = (viewDragHelper != null && if (settleFromViewDragHelper) {
            viewDragHelper!!.settleCapturedViewAt(
                child.left,
                top
            )
        } else {
            viewDragHelper!!.smoothSlideViewTo(child, child.left, top)
        })

        if (startedSettling) {
            setSmartStateInternal(State.STATE_SETTLING)
            if (settleRunnable == null) {
                // If the singleton SettleRunnable instance has not been instantiated, create it.
                settleRunnable = SettleRunnable(child, state)
            }
            // If the SettleRunnable has not been posted, post it with the correct state.
            if (!settleRunnable!!.isPosted) {
                settleRunnable!!.targetSmartState = state
                ViewCompat.postOnAnimation(child, settleRunnable!!)
                settleRunnable!!.isPosted = true
            } else {
                // Otherwise, if it has been posted, just update the target state.
                settleRunnable!!.targetSmartState = state
            }
        } else {
            setSmartStateInternal(state)
        }
    }

    private val dragCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            Log.d(TAG, "tryCaptureView... touchingScrollingChild = $touchingScrollingChild")
            if (state == State.STATE_DRAGGING) {
                return false
            }
            if (touchingScrollingChild) {
                return false
            }
            if (state == State.STATE_EXPANDED && activePointerId == pointerId) {
                val scroll = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false
                }
            }

            return viewRef != null && viewRef!!.get() === child
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setSmartStateInternal(State.STATE_DRAGGING)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(
                top,
                childHeight,
                parentHeight,
            )
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return parentHeight
        }
    }

    private inner class SettleRunnable(
        private val view: View,
        var targetSmartState: State
    ) : Runnable {
        var isPosted = false

        override fun run() {
            if (viewDragHelper != null && viewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(view, this)
            } else {
                setSmartStateInternal(targetSmartState)
            }
            isPosted = false
        }
    }

    /** State persisted across instances  */
    protected class SavedState(superState: Parcelable?, behavior: TopSheetBehavior<*>) :
        AbsSavedState(superState!!) {
        val state: State = behavior.state

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state.ordinal)
        }
    }
}