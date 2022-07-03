package com.danilkinkin.buckwheat

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.danilkinkin.buckwheat.utils.toSP
import com.google.android.material.textview.MaterialTextView

class TextWithLabelFragment : Fragment() {
    enum class Size { BIG, CAPTION, SMALL_CAPTION }

    lateinit var root: View
    private var size: Size = Size.BIG
    private var label: String = ""
    private var value: String = ""
    private var labelTextSize = 0F
    private var valueTextSize = 0F
    private var callback: ((view: View) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        root = inflater.inflate(R.layout.fragment_text_with_label, container, false)

        Log.d("TextWith", "onCreateView")

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLabel(label)
        setValue(value)
        // setSize(size)

        Log.d("TextWith", "onViewCreated this.callback: ${this.callback}")

        this.callback?.let {
            Log.d("TextWith", "Execute callback...")
            it(root)
        }
    }

    fun onCreated(callback: (view: View) -> Unit) {
        Log.d("TextWith", "setCallback")
        this.callback = callback
    }

    fun setLabel(text: String) {
        if (view === null) {
            label = text
            return
        }

        root.findViewById<MaterialTextView>(R.id.label).text = text
    }

    fun setValue(text: String) {
        if (view === null) {
            value = text
            return
        }

        root.findViewById<MaterialTextView>(R.id.value).text = text
    }

    fun getLabelView(): MaterialTextView {
        return root.findViewById(R.id.label)
    }

    fun getValueView(): MaterialTextView {
        return root.findViewById(R.id.value)
    }

    fun setSize(newSize: Size) {
        if (view === null) {
            size = newSize
            return
        }

        var newLabelTextSize = 0F
        var newValueTextSize = 0F

        when (newSize) {
            Size.SMALL_CAPTION -> {
                newValueTextSize = 14.toSP().toFloat()
                newLabelTextSize = 6.toSP().toFloat()
            }
            Size.CAPTION -> {
                newValueTextSize = 20.toSP().toFloat()
                newLabelTextSize = 8.toSP().toFloat()
            }
            Size.BIG -> {
                newValueTextSize = 50.toSP().toFloat()
                newLabelTextSize = 10.toSP().toFloat()
            }
        }

        val diffValueTextSize = newValueTextSize - valueTextSize
        val diffLabelTextSize = newLabelTextSize - labelTextSize

        valueTextSize = newValueTextSize
        labelTextSize = newLabelTextSize

        val valueView = root.findViewById<MaterialTextView>(R.id.value)
        val labelView = root.findViewById<MaterialTextView>(R.id.label)

        val animatorValue = ValueAnimator.ofFloat(1F, 0F)

        animatorValue
            .setDuration(300)
            .addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Float

                valueView.textSize = newValueTextSize - animatedValue * diffValueTextSize
                labelView.textSize = newLabelTextSize - animatedValue * diffLabelTextSize
            }

        animatorValue.start()
    }
}