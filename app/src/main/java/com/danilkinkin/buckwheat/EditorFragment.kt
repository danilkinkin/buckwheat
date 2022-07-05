package com.danilkinkin.buckwheat

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel

class EditorFragment() : Fragment() {
    private lateinit var model: DrawsViewModel

    private var budgetFragment: TextWithLabelFragment? = null
    private var drawFragment: TextWithLabelFragment? = null
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

        val model: DrawsViewModel by activityViewModels()

        this.model = model

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

    private fun build() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()

        budgetFragment = TextWithLabelFragment().also {
            it.setValue("${model.budgetValue} ₽")
            it.setLabel("Budget")

            it.onCreated { view ->
                it.getLabelView().textSize = 10.toSP().toFloat()
                it.getValueView().textSize = 40.toSP().toFloat()
            }
        }

        ft.add(R.id.calculator, budgetFragment!!)

        ft.commit()
    }

    private fun observe() {
        model.stage.observeForever { stage ->
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()

            when (stage) {
                DrawsViewModel.Stage.IDLE, null -> {

                }
                DrawsViewModel.Stage.CREATING_DRAW -> {
                    drawFragment = TextWithLabelFragment()
                    drawFragment!!.onCreated {
                        if (model.useDot) {
                            drawFragment!!.setValue("${model.drawValue} ₽")
                        } else {
                            drawFragment!!.setValue("${model.drawValue.toInt()} ₽")
                        }
                        drawFragment!!.setLabel("Draw")
                        drawFragment!!.getLabelView().textSize = 10.toSP().toFloat()
                        drawFragment!!.getValueView().textSize = 46.toSP().toFloat()
                    }

                    restBudgetFragment = TextWithLabelFragment()
                    restBudgetFragment!!.onCreated {
                        restBudgetFragment!!.setValue("${model.budgetValue - model.drawValue} ₽")
                        restBudgetFragment!!.setLabel("Rest budget")
                        restBudgetFragment!!.getLabelView().textSize = 8.toSP().toFloat()
                        restBudgetFragment!!.getValueView().textSize = 20.toSP().toFloat()
                    }

                    ft.add(R.id.calculator, drawFragment!!)
                    ft.add(R.id.calculator, restBudgetFragment!!)

                    ft.commit()

                    ft.runOnCommit {
                        budgetFragment?.also {
                            animate(it.getLabelView(), "textSize", 10.toSP().toFloat(), 6.toSP().toFloat())
                            animate(it.getValueView(), "textSize",  40.toSP().toFloat(), 12.toSP().toFloat())
                        }
                    }
                }
                DrawsViewModel.Stage.EDIT_DRAW -> {
                    drawFragment?.also {
                        if (model.useDot) {
                            it.setValue("${model.drawValue} ₽")
                        } else {
                            it.setValue("${model.drawValue.toInt()} ₽")
                        }
                        it.setLabel("Draw")
                    }
                    restBudgetFragment?.also {
                        it.setValue("${model.budgetValue - model.drawValue} ₽")
                        it.setLabel("Rest budget")
                    }
                }
                DrawsViewModel.Stage.COMMITTING_DRAW -> {
                    ft.remove(budgetFragment!!)
                    ft.remove(drawFragment!!)

                    ft.commit()

                    ft.runOnCommit {
                        budgetFragment = restBudgetFragment
                        drawFragment = null
                        restBudgetFragment = null

                        budgetFragment?.also {
                            it.setValue("${model.budgetValue} ₽")
                            it.setLabel("Budget")
                            animate(it.getLabelView(), "textSize", 8.toSP().toFloat(), 10.toSP().toFloat())
                            animate(it.getValueView(), "textSize",  20.toSP().toFloat(), 40.toSP().toFloat())
                        }
                    }
                }
            }
        }
    }
}