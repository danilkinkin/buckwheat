package com.danilkinkin.buckwheat

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.textview.MaterialTextView
import kotlin.math.floor


class MainActivity : AppCompatActivity() {
    enum class Stage { IDLE, CREATING_DRAW, EDIT_DRAW, COMMITTING_DRAW }

    var budgetValue: Double = 10000.0
    var useDot: Boolean = false
    var drawValue: Double = 0.0

    var stage: Stage = Stage.IDLE

    private val keyboardContainer: FragmentContainerView by lazy {
        findViewById(R.id.keyboard_container)
    }

    private val calculatorContainer: LinearLayout by lazy {
        findViewById(R.id.calculator)
    }

    private val budgetView = TextWithLabelFragment().also {
        it.setLabel("Budget")
        it.setValue("$budgetValue ₽")
    }
    private val drawView = TextWithLabelFragment().also {
        it.setLabel("Draw")
        it.setValue("0 ₽")
    }
    private val restBudgetView = TextWithLabelFragment().also {
        it.setLabel("Rest budget")
        it.setValue("0 ₽")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)

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

        build()
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

    private fun createDraw() {
        stage = Stage.CREATING_DRAW

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        ft.add(R.id.calculator, drawView)
        ft.add(R.id.calculator, restBudgetView)

        ft.commit()

    }

    private fun editDraw() {
        stage = Stage.EDIT_DRAW

        drawView.setValue("$drawValue ₽")
        restBudgetView.setValue("${budgetValue - drawValue} ₽")
    }

    private fun commitDraw() {
        stage = Stage.COMMITTING_DRAW
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        ft.remove(restBudgetView)
        ft.remove(drawView)

        budgetView.setValue("$budgetValue ₽")

        ft.commit()

        stage = Stage.IDLE
    }

    private fun build() {
        keyboardContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val params = keyboardContainer.layoutParams

            params.height = keyboardContainer.width

            keyboardContainer.layoutParams = params
        }

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        val keyboard = KeyboardFragment()

        keyboard.setCallback { type, value ->
            when (type) {
                "number" -> {
                    if (stage === Stage.IDLE) createDraw()

                    drawValue = drawValue * 10 + value!!

                    editDraw()
                }
                "action_eval" -> {
                    budgetValue -= drawValue
                    drawValue = 0.0

                    commitDraw()
                }
                "action_backspace" -> {
                    /* if (useDot && beforeDot != 0) {
                        beforeDot = floor(beforeDot.toFloat() / 10F).toInt()
                        textField.text = "$afterDot.$beforeDot ₽"
                    } else {
                        useDot = false
                        afterDot = floor(afterDot.toFloat() / 10F).toInt()
                        textField.text = "$afterDot ₽"
                    } */
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

        ft.add(R.id.calculator, budgetView)
        ft.replace(R.id.keyboard_container, keyboard)

        ft.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}