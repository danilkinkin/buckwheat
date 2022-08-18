package com.danilkinkin.buckwheat.widgets.editor

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.NewDayBottomSheet
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.SettingsBottomSheet
import com.danilkinkin.buckwheat.TextWithLabelFragment
import com.danilkinkin.buckwheat.utils.getStatusBarHeight
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.toDP
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.google.android.material.button.MaterialButton
import kotlin.math.max
import kotlin.math.min

class EditorFragment : Fragment() {
    private lateinit var model: SpentViewModel
    private lateinit var appModel: AppViewModel

    private lateinit var budgetFragment: TextWithLabelFragment
    private lateinit var spentFragment: TextWithLabelFragment
    private lateinit var restBudgetFragment: TextWithLabelFragment
    private var settingsBottomSheet: SettingsBottomSheet? = null
    private var newDayBottomSheet: NewDayBottomSheet? = null

    enum class AnimState { FIRST_IDLE, EDITING, COMMIT, IDLE, RESET }

    private var currAnimator: ValueAnimator? = null
    private var currState: AnimState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: SpentViewModel by activityViewModels()
        val appModel: AppViewModel by activityViewModels()

        this.model = model
        this.appModel = appModel

        val helperView = view.findViewById<View>(R.id.top_bar_offset_helper)
        val layout = helperView.layoutParams

        layout.height = getStatusBarHeight(view)

        helperView.layoutParams = layout

        build()
        observe()
    }

    private fun setDailyBudget(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.dailyBudget.value!! - model.spentFromDailyBudget.value!!))
            it.setLabel(requireContext().getString(R.string.budget_for_today))
        }
    }

    private fun setRestDailyBudget(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.dailyBudget.value!! - model.spentFromDailyBudget.value!! - model.currentSpent))
            it.setLabel(requireContext().getString(R.string.rest_budget_for_today))
        }
    }

    private fun setSpent(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.currentSpent, model.useDot))
            it.setLabel(requireContext().getString(R.string.spent))
        }
    }

    private fun build() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()

        budgetFragment = TextWithLabelFragment().also {
            setDailyBudget(it)

            it.onCreated { _ ->
                val params = it.root.layoutParams as ConstraintLayout.LayoutParams
                params.leftToLeft = R.id.calculator
                params.bottomToBottom = R.id.calculator
                it.root.requestLayout()

                it.root.transitionAlpha = 0F
            }
        }

        spentFragment = TextWithLabelFragment().also {
            setSpent(it)

            it.onCreated { _ ->
                val params = it.root.layoutParams as ConstraintLayout.LayoutParams
                params.leftToLeft = R.id.calculator
                params.bottomToBottom = R.id.calculator
                it.root.requestLayout()

                it.root.transitionAlpha = 0F
            }
        }

        restBudgetFragment = TextWithLabelFragment().also {
            setRestDailyBudget(it)

            it.onCreated { _ ->
                val params = it.root.layoutParams as ConstraintLayout.LayoutParams
                params.leftToLeft = R.id.calculator
                params.bottomToBottom = R.id.calculator
                it.root.requestLayout()

                it.root.transitionAlpha = 0F
            }
        }

        ft.add(R.id.calculator, budgetFragment)
        ft.add(R.id.calculator, spentFragment)
        ft.add(R.id.calculator, restBudgetFragment)

        ft.commit()

        ft.runOnCommit {
            animTo(AnimState.FIRST_IDLE)
        }

        requireView().findViewById<MaterialButton>(R.id.settings_btn).setOnClickListener {
            if (settingsBottomSheet?.isVisible == true) return@setOnClickListener

            settingsBottomSheet = SettingsBottomSheet()
            settingsBottomSheet!!.show(parentFragmentManager, SettingsBottomSheet.TAG)
        }

        requireView().findViewById<MaterialButton>(R.id.dev_tool_btn).setOnClickListener {
            if (newDayBottomSheet?.isVisible == true) return@setOnClickListener

            newDayBottomSheet = NewDayBottomSheet()
            newDayBottomSheet!!.show(parentFragmentManager, NewDayBottomSheet.TAG)
        }
    }

    private fun animFrame(state: AnimState, progress: Float = 1F) {
        when (state) {
            AnimState.FIRST_IDLE -> {
                budgetFragment.also {
                    it.getLabelView().textSize = 10.toSP().toFloat()
                    it.getValueView().textSize = 40.toSP().toFloat()

                    it.root.translationY = 30.toDP() * (1F - progress)
                    it.root.transitionAlpha = progress
                }
            }
            AnimState.EDITING -> {
                var offset = 0F

                restBudgetFragment.also {
                    it.getLabelView().textSize = 8.toSP().toFloat()
                    it.getValueView().textSize = 20.toSP().toFloat()

                    offset += it.root.height
                    it.root.translationY = (offset + spentFragment.root.height) * (1F - progress)
                    it.root.transitionAlpha = 1F
                }

                spentFragment.also {
                    it.getLabelView().textSize = 18.toSP().toFloat()
                    it.getValueView().textSize = 60.toSP().toFloat()

                    it.root.translationY = (it.root.height + offset) * (1F - progress) - offset
                    it.root.transitionAlpha = 1F

                    offset += it.root.height
                }

                budgetFragment.also {
                    it.getLabelView().textSize = 10.toSP().toFloat() - 4.toSP().toFloat() * progress
                    it.getValueView().textSize = 40.toSP().toFloat() - 28.toSP().toFloat() * progress

                    it.root.translationY = -offset * progress
                    it.root.transitionAlpha = 1F
                }
            }
            AnimState.COMMIT -> {
                var offset = 0F

                val progressA = min(progress * 2F, 1F)
                val progressB = max((progress - 0.5F) * 2F, 0F)

                restBudgetFragment.also {
                    it.getLabelView().textSize = 8.toSP().toFloat() + 2.toSP().toFloat() * progress
                    it.getValueView().textSize = 20.toSP().toFloat() + 20.toSP().toFloat() * progress

                    offset += it.root.height

                    it.root.transitionAlpha = 1F
                }

                spentFragment.also {
                    it.getLabelView().textSize = 18.toSP().toFloat()
                    it.getValueView().textSize = 60.toSP().toFloat()

                    it.root.translationY = -offset - 50.toDP() * progressB
                    it.root.transitionAlpha = 1F - progressB

                    offset += it.root.height
                }

                budgetFragment.also {
                    it.getLabelView().textSize = 6.toSP().toFloat()
                    it.getValueView().textSize = 12.toSP().toFloat()

                    it.root.translationY = -offset - 50.toDP() * progressA
                    it.root.transitionAlpha = 1F - progressA
                }
            }
            AnimState.RESET -> {
                var offset = 0F

                restBudgetFragment.also {
                    it.getLabelView().textSize = 8.toSP().toFloat()
                    it.getValueView().textSize = 20.toSP().toFloat()

                    offset += it.root.height
                    it.root.translationY = (offset + spentFragment.root.height) * progress
                }

                spentFragment.also {
                    it.getLabelView().textSize = 18.toSP().toFloat()
                    it.getValueView().textSize = 60.toSP().toFloat()

                    it.root.translationY = (it.root.height + offset) * progress - offset

                    offset += it.root.height
                }

                budgetFragment.also {
                    it.getLabelView().textSize = 6.toSP().toFloat() + 4.toSP().toFloat() * progress
                    it.getValueView().textSize = 12.toSP().toFloat() + 28.toSP().toFloat() * progress

                    it.root.translationY = -offset * (1F - progress)
                }
            }
            AnimState.IDLE -> {
                budgetFragment.also {
                    it.getLabelView().textSize = 10.toSP().toFloat()
                    it.getValueView().textSize = 40.toSP().toFloat()

                    it.root.translationY = 0F
                    it.root.transitionAlpha = 1F
                }
                restBudgetFragment.also {
                    it.root.transitionAlpha = 0F
                }
            }
        }
    }

    private fun animTo(state: AnimState) {
        if (currState === state) return

        currState = state

        if (currAnimator !== null) {
            currAnimator!!.pause()
        }

        currAnimator = ValueAnimator.ofFloat(0F, 1F)

        currAnimator!!.apply {
            duration = 300

            addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Float

                animFrame(state, animatedValue)
            }

            doOnEnd {
                if (state === AnimState.COMMIT) {
                    animFrame(AnimState.IDLE)
                }
            }

            start()
        }
    }

    private fun observe() {
        model.dailyBudget.observeForever {
            setDailyBudget(budgetFragment)
        }

        model.spentFromDailyBudget.observeForever {
            setDailyBudget(budgetFragment)
        }

        model.stage.observeForever { stage ->
            when (stage) {
                SpentViewModel.Stage.IDLE, null -> {
                    if (currState === AnimState.EDITING) animTo(AnimState.RESET)
                }
                SpentViewModel.Stage.CREATING_SPENT -> {
                    setSpent(spentFragment)
                    setRestDailyBudget(restBudgetFragment)

                    animTo(AnimState.EDITING)
                }
                SpentViewModel.Stage.EDIT_SPENT -> {
                    setSpent(spentFragment)
                    setRestDailyBudget(restBudgetFragment)
                }
                SpentViewModel.Stage.COMMITTING_SPENT -> {
                    setDailyBudget(budgetFragment)

                    animTo(AnimState.COMMIT)

                    model.resetSpent()
                }
            }
        }

        appModel.isDebug.observeForever {
            requireView().findViewById<MaterialButton>(R.id.dev_tool_btn).visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}