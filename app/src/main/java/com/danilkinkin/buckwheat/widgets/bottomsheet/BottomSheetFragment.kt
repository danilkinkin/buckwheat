package com.danilkinkin.buckwheat.widgets.bottomsheet

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.utils.getStatusBarHeight
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetFragment: BottomSheetDialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        val topBarHeight = getStatusBarHeight(view)

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED && bottomSheet.top < topBarHeight) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        dialog!!.window!!.insetsController?.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }

                    view.findViewById<AppBarLayout>(R.id.app_bar).background = ContextCompat.getColor(
                        context!!,
                        com.google.android.material.R.color.material_dynamic_neutral95,
                    ).toDrawable()

                    dialog!!.window!!.statusBarColor = ContextCompat.getColor(
                        context!!,
                        com.google.android.material.R.color.material_dynamic_neutral95,
                    )

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        dialog!!.window!!.insetsController?.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }

                    view.findViewById<AppBarLayout>(R.id.app_bar).background = ContextCompat.getColor(
                        context!!,
                        android.R.color.transparent,
                    ).toDrawable()

                    dialog!!.window!!.statusBarColor = ContextCompat.getColor(
                        context!!,
                        android.R.color.transparent,
                    )
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })

    }
}