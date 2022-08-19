package com.danilkinkin.buckwheat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.danilkinkin.buckwheat.widgets.bottomsheet.BottomSheetFragment
import com.google.android.material.card.MaterialCardView
import java.util.*


class SettingsBottomSheet : BottomSheetFragment() {
    companion object {
        val TAG = SettingsBottomSheet::class.simpleName
    }

    private lateinit var model: AppViewModel
    private lateinit var spendsModel: SpentViewModel

    private val openSiteBtn: MaterialCardView by lazy {
        requireView().findViewById(R.id.site)
    }

    private val reportBugBtn: MaterialCardView by lazy {
        requireView().findViewById(R.id.report_bug)
    }

    private val themeSwitcher: RadioGroup by lazy {
        requireView().findViewById(R.id.theme_switcher)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: AppViewModel by activityViewModels()
        val spendsModel: SpentViewModel by activityViewModels()

        this.model = model
        this.spendsModel = spendsModel

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.findViewById<LinearLayout>(R.id.content).setPadding(0, 0, 0, insets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        build()
    }

    private fun build() {
        themeSwitcher.setOnCheckedChangeListener { group, checkedId ->
            val mode = when (checkedId) {
                R.id.theme_switcher_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.theme_switcher_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            AppCompatDelegate.setDefaultNightMode(mode)
            MainActivity.getInstance().appSettingsPrefs.edit().putInt("nightMode", mode).apply()
        }

        themeSwitcher.check(when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.theme_switcher_light
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.theme_switcher_dark
            else -> R.id.theme_switcher_system
        })
        
        openSiteBtn.setOnClickListener {
            val url = "https://danilkinkin.com"
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(url)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val clipboard = getSystemService(
                    requireContext(),
                    ClipboardManager::class.java
                ) as ClipboardManager

                clipboard.setPrimaryClip(ClipData.newPlainText("url", url))

                Toast
                    .makeText(
                        context,
                        requireContext().getString(R.string.copy_in_clipboard),
                        Toast.LENGTH_LONG
                    )
                    .show()
            }
        }

        reportBugBtn.setOnClickListener {
            val url = "https://github.com/danilkinkin/buckweat/issues"
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(url)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val clipboard = getSystemService(
                    requireContext(),
                    ClipboardManager::class.java
                ) as ClipboardManager

                clipboard.setPrimaryClip(ClipData.newPlainText("url", url))

                Toast
                    .makeText(
                        context,
                        requireContext().getString(R.string.copy_in_clipboard),
                        Toast.LENGTH_LONG
                    )
                    .show()
            }
        }
    }
}