package com.danilkinkin.buckwheat.widgets.editor

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.*
import com.danilkinkin.buckwheat.utils.getStatusBarHeight
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.toDP
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlin.math.max
import kotlin.math.min

class EditorFragment : Fragment() {
    companion object {
        val TAG: String = EditorFragment::class.java.simpleName
    }

    private lateinit var model: SpentViewModel
    private lateinit var appModel: AppViewModel

    private var settingsBottomSheet: SettingsBottomSheet? = null
    private var walletBottomSheet: WalletBottomSheet? = null
    private var newDayBottomSheet: NewDayBottomSheet? = null

    private val budgetView: ConstraintLayout by lazy {
        requireView().findViewById(R.id.budget)
    }
    private val spentView: ConstraintLayout by lazy {
        requireView().findViewById(R.id.spent)
    }
    private val restBudgetView: ConstraintLayout by lazy {
        requireView().findViewById(R.id.rest_budget)
    }

    private val budgetValue: MaterialTextView by lazy {
        requireView().findViewById(R.id.budget_value)
    }
    private val spentValue: MaterialTextView by lazy {
        requireView().findViewById(R.id.spent_value)
    }
    private val restBudgetValue: MaterialTextView by lazy {
        requireView().findViewById(R.id.rest_budget_value)
    }

    private val budgetLabel: MaterialTextView by lazy {
        requireView().findViewById(R.id.budget_label)
    }
    private val spentLabel: MaterialTextView by lazy {
        requireView().findViewById(R.id.spent_label)
    }
    private val restBudgetLabel: MaterialTextView by lazy {
        requireView().findViewById(R.id.rest_budget_label)
    }

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

    private fun calculateValues(
        budget: Boolean = true,
        restBudget: Boolean = true,
        spent: Boolean = true
    ) {
        val spentFromDailyBudget = model.spentFromDailyBudget.value!!
        val dailyBudget = model.dailyBudget.value!!

        if (budget) budgetValue.text = prettyCandyCanes(dailyBudget - spentFromDailyBudget)
        if (restBudget) restBudgetValue.text =
            prettyCandyCanes(dailyBudget - spentFromDailyBudget - model.currentSpent)
        if (spent) spentValue.text = prettyCandyCanes(model.currentSpent, model.useDot)
    }

    private fun build() {
        calculateValues()

        restBudgetView.alpha = 0F
        spentView.alpha = 0F
        budgetView.alpha = 0F

        animTo(AnimState.FIRST_IDLE)

        requireView().findViewById<MaterialButton>(R.id.settings_btn).setOnClickListener {
            if (settingsBottomSheet?.isVisible == true) return@setOnClickListener

            settingsBottomSheet = SettingsBottomSheet()
            settingsBottomSheet!!.show(parentFragmentManager, SettingsBottomSheet.TAG)
        }

        requireView().findViewById<MaterialButton>(R.id.wallet_btn).setOnClickListener {
            if (walletBottomSheet?.isVisible == true) return@setOnClickListener

            walletBottomSheet = WalletBottomSheet()
            walletBottomSheet!!.show(parentFragmentManager, WalletBottomSheet.TAG)
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
                budgetLabel.textSize = 10.toSP().toFloat()
                budgetValue.textSize = 40.toSP().toFloat()
                budgetView.translationY = 30.toDP() * (1F - progress)
                budgetView.alpha = progress
            }
            AnimState.EDITING -> {
                var offset = 0F

                restBudgetValue.textSize = 20.toSP().toFloat()
                restBudgetLabel.textSize = 8.toSP().toFloat()
                offset += restBudgetView.height
                restBudgetView.translationY = (offset + spentView.height) * (1F - progress)
                restBudgetView.alpha = 1F

                spentValue.textSize = 60.toSP().toFloat()
                spentLabel.textSize = 18.toSP().toFloat()
                spentView.translationY = (spentView.height + offset) * (1F - progress) - offset
                spentView.alpha = 1F

                offset += spentView.height

                budgetValue.textSize = 40.toSP().toFloat() - 28.toSP().toFloat() * progress
                budgetLabel.textSize = 10.toSP().toFloat() - 4.toSP().toFloat() * progress
                budgetView.translationY = -offset * progress
                budgetView.alpha = 1F
            }
            AnimState.COMMIT -> {
                var offset = 0F

                val progressA = min(progress * 2F, 1F)
                val progressB = max((progress - 0.5F) * 2F, 0F)

                restBudgetValue.textSize = 20.toSP().toFloat() + 20.toSP().toFloat() * progress
                restBudgetLabel.textSize = 8.toSP().toFloat() + 2.toSP().toFloat() * progress
                offset += restBudgetView.height
                restBudgetView.alpha = 1F

                spentValue.textSize = 60.toSP().toFloat()
                spentLabel.textSize = 18.toSP().toFloat()
                spentView.translationY = -offset - 50.toDP() * progressB
                spentView.alpha = 1F - progressB
                offset += spentView.height

                budgetValue.textSize = 12.toSP().toFloat()
                budgetLabel.textSize = 6.toSP().toFloat()
                budgetView.translationY = -offset - 50.toDP() * progressA
                budgetView.alpha = 1F - progressA
            }
            AnimState.RESET -> {
                var offset = 0F

                restBudgetValue.textSize = 20.toSP().toFloat()
                restBudgetLabel.textSize = 8.toSP().toFloat()
                offset += restBudgetView.height
                restBudgetView.translationY = (offset + spentView.height) * progress

                spentValue.textSize = 60.toSP().toFloat()
                spentLabel.textSize = 18.toSP().toFloat()
                spentView.translationY = (spentView.height + offset) * progress - offset
                offset += spentView.height

                budgetValue.textSize = 12.toSP().toFloat() + 28.toSP().toFloat() * progress
                budgetLabel.textSize = 6.toSP().toFloat() + 4.toSP().toFloat() * progress
                budgetView.translationY = -offset * (1F - progress)
            }
            AnimState.IDLE -> {
                calculateValues(restBudget = false)

                budgetValue.textSize = 40.toSP().toFloat()
                budgetLabel.textSize = 10.toSP().toFloat()
                budgetView.translationY = 0F
                budgetView.alpha = 1F

                restBudgetView.alpha = 0F
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
            duration = 220
            interpolator = AccelerateDecelerateInterpolator()

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
        model.dailyBudget.observe(viewLifecycleOwner) {
            calculateValues()
        }

        model.spentFromDailyBudget.observe(viewLifecycleOwner) {
            calculateValues(budget = currState !== AnimState.EDITING, restBudget = false)
        }

        model.stage.observe(viewLifecycleOwner) { stage ->
            when (stage) {
                SpentViewModel.Stage.IDLE, null -> {
                    if (currState === AnimState.EDITING) animTo(AnimState.RESET)
                }
                SpentViewModel.Stage.CREATING_SPENT -> {
                    calculateValues(budget = false)

                    animTo(AnimState.EDITING)
                }
                SpentViewModel.Stage.EDIT_SPENT -> {
                    calculateValues(budget = false)
                }
                SpentViewModel.Stage.COMMITTING_SPENT -> {
                    animTo(AnimState.COMMIT)

                    model.resetSpent()
                }
            }
        }

        appModel.isDebug.observe(viewLifecycleOwner) {
            requireView().findViewById<MaterialButton>(R.id.dev_tool_btn).visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}