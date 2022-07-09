package com.danilkinkin.buckwheat

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.*
import com.danilkinkin.buckwheat.adapters.DrawsAdapter
import com.danilkinkin.buckwheat.adapters.EditorAdapter
import com.danilkinkin.buckwheat.adapters.KeyboardAdapter
import com.danilkinkin.buckwheat.adapters.TopAdapter
import com.danilkinkin.buckwheat.decorators.DrawsDividerItemDecoration
import com.danilkinkin.buckwheat.utils.toDP
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var model: DrawsViewModel

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

    private fun observe() {
    }

    private fun build() {
        val recyclerView: RecyclerView = findViewById(R.id.recycle_view)

        val layoutManager = object : LinearLayoutManager(this) {
            private var isScrollEnabled = true

            fun setScrollEnabled(flag: Boolean) {
                this.isScrollEnabled = flag
            }

            override fun canScrollVertically(): Boolean {
                // Log.d("Main", "isScrollEnabled = $isScrollEnabled")
                return isScrollEnabled && super.canScrollVertically();
            }
        }

        val drawsDividerItemDecoration = DrawsDividerItemDecoration(recyclerView.context)
        recyclerView.addItemDecoration(drawsDividerItemDecoration)

        layoutManager.stackFromEnd = true

        val topAdapter = TopAdapter()
        val drawsAdapter = DrawsAdapter()
        val editorAdapter = EditorAdapter(supportFragmentManager)
        val keyboardAdapter = KeyboardAdapter(supportFragmentManager) { lockScroll ->
            layoutManager.setScrollEnabled(!lockScroll)
        }
        val contactAdapter = ConcatAdapter(topAdapter, drawsAdapter, editorAdapter, keyboardAdapter)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = contactAdapter

        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            keyboardAdapter.scrollUpdate(recyclerView)
        }

        val swipeToDeleteCallback = SwipeToDeleteCallback(applicationContext, drawsAdapter) {
            model.removeDraw(it)
        }

        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchhelper.attachToRecyclerView(recyclerView)

        model.getDraws().observeForever { draws ->
            drawsAdapter.submitList(draws)
        }

        /* keyboardContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
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

        ft.replace(R.id.keyboard_container, keyboard)

        ft.commit() */
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}