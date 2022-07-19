package com.danilkinkin.buckwheat

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.*
import com.danilkinkin.buckwheat.adapters.DrawsAdapter
import com.danilkinkin.buckwheat.adapters.EditorAdapter
import com.danilkinkin.buckwheat.adapters.KeyboardAdapter
import com.danilkinkin.buckwheat.adapters.TopAdapter
import com.danilkinkin.buckwheat.decorators.DrawsDividerItemDecoration
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    private lateinit var model: DrawsViewModel

    val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recycle_view)
    }

    val fabHome: FloatingActionButton by lazy {
        findViewById(R.id.fab_home_btn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val windowInsetsController =  WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.isAppearanceLightNavigationBars = true

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

        window.statusBarColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral95), 230)

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
        model.requireReCalcBudget.observeForever {
            if (it) {
                val newDayBottomSheet = NewDayBottomSheet()
                newDayBottomSheet.show(supportFragmentManager, NewDayBottomSheet.TAG)
            }
        }

        model.requireSetBudget.observeForever {
            if (it) {
                val settingsBottomSheet = SettingsBottomSheet()
                settingsBottomSheet.show(supportFragmentManager, SettingsBottomSheet.TAG)
            }
        }
    }

    private fun build() {
        var recyclerViewScrollY: Long = 0

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
        val editorAdapter = EditorAdapter(supportFragmentManager, recyclerView)
        val keyboardAdapter = KeyboardAdapter(supportFragmentManager) { lockScroll ->
            layoutManager.setScrollEnabled(!lockScroll)
        }
        val contactAdapter = ConcatAdapter(topAdapter, drawsAdapter, editorAdapter, keyboardAdapter)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = contactAdapter

        recyclerView.setOnScrollChangeListener { _, _, _, _, dY: Int ->
            keyboardAdapter.scrollUpdate(recyclerView)

            recyclerViewScrollY += dY

            if (recyclerViewScrollY > recyclerView.width) {
                fabHome.show()
            } else {
                fabHome.hide()
            }
        }

        val swipeToDeleteCallback = SwipeToDeleteCallback(applicationContext, drawsAdapter) {
            model.removeDraw(it)
        }

        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchhelper.attachToRecyclerView(recyclerView)

        model.getDraws().observeForever { draws ->
            drawsAdapter.submitList(draws)
        }

        fabHome.setOnClickListener {
            recyclerView.smoothScrollToPosition(contactAdapter.itemCount - 1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}