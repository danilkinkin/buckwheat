package com.danilkinkin.buckwheat

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import java.util.*
import kotlin.math.floor


class MainActivity : AppCompatActivity() {
    private lateinit var model: DrawsViewModel

    private val keyboardContainer: FragmentContainerView by lazy {
        findViewById(R.id.keyboard_container)
    }

    private var budgetFragment: TextWithLabelFragment? = null
    private var drawFragment: TextWithLabelFragment? = null
    private var restBudgetFragment: TextWithLabelFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)

        windowInsetsController?.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            val mlp = view.layoutParams as MarginLayoutParams
            mlp.topMargin = 0
            mlp.leftMargin = insets.left
            mlp.bottomMargin = 0
            mlp.rightMargin = insets.right
            view.layoutParams = mlp

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

        val model: DrawsViewModel by viewModels()

        this.model = model
        build()
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun observe() {
        model.stage.observeForever { stage ->
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

            when (stage) {
                DrawsViewModel.Stage.IDLE, null -> {

                }
                DrawsViewModel.Stage.CREATING_DRAW -> {
                    drawFragment = TextWithLabelFragment()
                    drawFragment!!.onCreated {
                        drawFragment!!.setValue("${model.drawValue} ₽")
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
                        it.setValue("${model.drawValue} ₽")
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

    private fun build() {
        keyboardContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                keyboardContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val params = keyboardContainer.layoutParams

                params.height = keyboardContainer.width

                keyboardContainer.layoutParams = params
            }
        })

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        val keyboard = KeyboardFragment()

        keyboard.setCallback { type, value ->
            when (type) {
                "number" -> {
                    if (model.stage.value == DrawsViewModel.Stage.IDLE) model.createDraw()

                    model.editDraw(model.drawValue * 10F + value!!)
                }
                "action_eval" -> {
                    model.commitDraw()
                }
                "action_backspace" -> {
                    model.editDraw(floor(model.drawValue / 10F))
                }
                "action_dot" -> {
                    /* if (useDot) {
                        return@setCallback
                    }

                    useDot = true
                    beforeDot = 0
                    textField.text = "$afterDot.$beforeDot ₽" */
                }
            }
            Log.d("Main", "$type: $value")
        }

        budgetFragment = TextWithLabelFragment().also {
            it.setValue("${model.budgetValue} ₽")
            it.setLabel("Budget")

            it.onCreated { view ->
                it.getLabelView().textSize = 10.toSP().toFloat()
                it.getValueView().textSize = 40.toSP().toFloat()
            }
        }

        ft.add(R.id.calculator, budgetFragment!!)

        ft.replace(R.id.keyboard_container, keyboard)

        ft.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}