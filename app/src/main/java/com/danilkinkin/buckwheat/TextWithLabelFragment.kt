package com.danilkinkin.buckwheat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView

class TextWithLabelFragment : Fragment() {
    private var label: String = ""
    private var value: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_text_with_label, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLabel(label)
        setValue(value)
    }

    fun setLabel(text: String) {
        if (view === null) {
            label = text
            return
        }

        requireView().findViewById<MaterialTextView>(R.id.label).text = text
    }

    fun setValue(text: String) {
        if (view === null) {
            value = text
            return
        }

        requireView().findViewById<MaterialTextView>(R.id.value).text = text
    }
}