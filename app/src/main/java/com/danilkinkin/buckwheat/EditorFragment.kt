package com.danilkinkin.buckwheat

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.prettyCandyCanes
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.google.android.material.button.MaterialButton

class EditorFragment : Fragment() {
    private lateinit var model: SpentViewModel
    private lateinit var appModel: AppViewModel

    private var budgetFragment: TextWithLabelFragment? = null
    private var spentFragment: TextWithLabelFragment? = null
    private var restBudgetFragment: TextWithLabelFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: SpentViewModel by activityViewModels()
        val appModel: AppViewModel by activityViewModels()

        this.model = model
        this.appModel = appModel

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val topBarHeight = if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }

        val helperView = view.findViewById<View>(R.id.top_bar_offset_helper)
        val layout = helperView.layoutParams

        layout.height = topBarHeight

        helperView.layoutParams = layout

        build()
        observe()
    }

    fun animate (view: View?, property: String, from: Float?, to: Float, duration: Long = 0): ObjectAnimator {
        val animator = (if (from === null) {
            ObjectAnimator
                .ofFloat(view, property, to)
        } else {
            ObjectAnimator
                .ofFloat(view, property, from, to)
        })

        animator.apply {
            this.duration = duration
            start()
        }

        return animator!!
    }

    private fun setDailyBudget(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.dailyBudget.value!! - model.spentFromDailyBudget.value!!))
            it.setLabel(context!!.getString(R.string.budget_for_today))
        }
    }

    private fun setRestDailyBudget(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.dailyBudget.value!! - model.spentFromDailyBudget.value!! - model.currentSpent))
            it.setLabel(context!!.getString(R.string.rest_budget_for_today))
        }
    }

    private fun setSpent(fragment: TextWithLabelFragment) {
        fragment.also {
            it.setValue(prettyCandyCanes(model.currentSpent, model.useDot))
            it.setLabel(context!!.getString(R.string.spent))
        }
    }

    private fun build() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()

        budgetFragment = TextWithLabelFragment().also {
            setDailyBudget(it)

            it.onCreated { _ ->
                it.getLabelView().textSize = 10.toSP().toFloat()
                it.getValueView().textSize = 40.toSP().toFloat()
            }
        }

        ft.add(R.id.calculator, budgetFragment!!)

        ft.commit()

        requireView().findViewById<MaterialButton>(R.id.settings_btn).setOnClickListener {
            val settingsBottomSheet = SettingsBottomSheet()
            settingsBottomSheet.show(parentFragmentManager, SettingsBottomSheet.TAG)
        }

        requireView().findViewById<MaterialButton>(R.id.dev_tool_btn).setOnClickListener {
            val newDayBottomSheet = NewDayBottomSheet()
            newDayBottomSheet.show(parentFragmentManager, NewDayBottomSheet.TAG)
        }
    }

    private fun observe() {
        model.dailyBudget.observeForever { _ ->
            budgetFragment?.let { setDailyBudget(it) }
        }

        model.spentFromDailyBudget.observeForever { _ ->
            budgetFragment?.let { setDailyBudget(it) }
        }

        model.stage.observeForever { stage ->
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()

            when (stage) {
                SpentViewModel.Stage.IDLE, null -> {
                    if (restBudgetFragment !== null) ft.remove(restBudgetFragment!!)
                    if (spentFragment !== null) ft.remove(spentFragment!!)

                    ft.commit()

                    ft.runOnCommit {
                        spentFragment = null
                        restBudgetFragment = null

                        budgetFragment?.also {
                            setDailyBudget(it)
                            animate(it.getLabelView(), "textSize", 8.toSP().toFloat(), 10.toSP().toFloat())
                            animate(it.getValueView(), "textSize",  12.toSP().toFloat(), 40.toSP().toFloat())
                        }
                    }
                }
                SpentViewModel.Stage.CREATING_SPENT -> {
                    spentFragment = TextWithLabelFragment()
                    spentFragment!!.onCreated {
                        spentFragment!!.setValue(prettyCandyCanes(model.currentSpent))
                        spentFragment!!.setLabel(context!!.getString(R.string.spent))
                        spentFragment!!.getLabelView().textSize = 10.toSP().toFloat()
                        spentFragment!!.getValueView().textSize = 46.toSP().toFloat()
                    }

                    restBudgetFragment = TextWithLabelFragment()
                    restBudgetFragment!!.onCreated {
                        setRestDailyBudget(restBudgetFragment!!)
                        restBudgetFragment!!.getLabelView().textSize = 8.toSP().toFloat()
                        restBudgetFragment!!.getValueView().textSize = 20.toSP().toFloat()
                    }

                    ft.add(R.id.calculator, spentFragment!!)
                    ft.add(R.id.calculator, restBudgetFragment!!)

                    ft.commit()

                    ft.runOnCommit {
                        budgetFragment?.also {
                            animate(it.getLabelView(), "textSize", 10.toSP().toFloat(), 6.toSP().toFloat())
                            animate(it.getValueView(), "textSize",  40.toSP().toFloat(), 12.toSP().toFloat())
                        }
                    }
                }
                SpentViewModel.Stage.EDIT_SPENT -> {
                    spentFragment?.also {
                        setSpent(it)
                    }
                    restBudgetFragment?.also {
                        setRestDailyBudget(it)
                    }
                }
                SpentViewModel.Stage.COMMITTING_SPENT -> {
                    ft.remove(budgetFragment!!)
                    ft.remove(spentFragment!!)

                    ft.commit()

                    ft.runOnCommit {
                        budgetFragment = restBudgetFragment
                        spentFragment = null
                        restBudgetFragment = null

                        budgetFragment?.also {
                            setDailyBudget(it)
                            animate(it.getLabelView(), "textSize", 8.toSP().toFloat(), 10.toSP().toFloat())
                            animate(it.getValueView(), "textSize",  20.toSP().toFloat(), 40.toSP().toFloat())
                        }

                        model.resetSpent()
                    }
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