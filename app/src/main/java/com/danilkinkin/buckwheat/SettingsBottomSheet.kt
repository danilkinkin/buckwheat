package com.danilkinkin.buckwheat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.danilkinkin.buckwheat.widgets.bottomsheet.BottomSheetFragment
import com.google.android.material.card.MaterialCardView


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