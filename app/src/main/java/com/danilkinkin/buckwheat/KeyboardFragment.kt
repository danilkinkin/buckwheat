package com.danilkinkin.buckwheat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class KeyboardFragment : Fragment() {
    private var callback: ((type: String, value: Int?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_keyboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        build()
    }

    fun setCallback(callback: (type: String, value: Int?) -> Unit) {
        this.callback = callback
    }

    fun build() {
        val root = requireView().findViewById<ConstraintLayout>(R.id.root)

        root.findViewById<MaterialButton>(R.id.btn_0).setOnClickListener {
            callback?.let { it("number", 0) }
        }

        root.findViewById<MaterialButton>(R.id.btn_1).setOnClickListener {
            callback?.let { it("number", 1) }
        }

        root.findViewById<MaterialButton>(R.id.btn_2).setOnClickListener {
            callback?.let { it("number", 2) }
        }

        root.findViewById<MaterialButton>(R.id.btn_3).setOnClickListener {
            callback?.let { it("number", 3) }
        }

        root.findViewById<MaterialButton>(R.id.btn_4).setOnClickListener {
            callback?.let { it("number", 4) }
        }

        root.findViewById<MaterialButton>(R.id.btn_5).setOnClickListener {
            callback?.let { it("number", 5) }
        }

        root.findViewById<MaterialButton>(R.id.btn_6).setOnClickListener {
            callback?.let { it("number", 6) }
        }

        root.findViewById<MaterialButton>(R.id.btn_7).setOnClickListener {
            callback?.let { it("number", 7) }
        }

        root.findViewById<MaterialButton>(R.id.btn_8).setOnClickListener {
            callback?.let { it("number", 8) }
        }

        root.findViewById<MaterialButton>(R.id.btn_9).setOnClickListener {
            callback?.let { it("number", 9) }
        }

        root.findViewById<MaterialButton>(R.id.btn_dot).setOnClickListener {
            callback?.let { it("action_dot", null) }
        }

        root.findViewById<MaterialButton>(R.id.btn_backspace).setOnClickListener {
            callback?.let { it("action_backspace", null) }
        }

        root.findViewById<MaterialButton>(R.id.btn_eval).setOnClickListener {
            callback?.let { it("action_eval", null) }
        }
    }
}