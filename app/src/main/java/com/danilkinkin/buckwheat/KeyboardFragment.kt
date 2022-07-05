package com.danilkinkin.buckwheat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.button.MaterialButton

class KeyboardFragment : Fragment() {
    private lateinit var model: DrawsViewModel

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

        val model: DrawsViewModel by activityViewModels()

        this.model = model

        build()
    }

    fun build() {
        val root = requireView().findViewById<ConstraintLayout>(R.id.root)

        root.findViewById<MaterialButton>(R.id.btn_0).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 0)
        }

        root.findViewById<MaterialButton>(R.id.btn_1).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 1)
        }

        root.findViewById<MaterialButton>(R.id.btn_2).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 2)
        }

        root.findViewById<MaterialButton>(R.id.btn_3).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 3)
        }

        root.findViewById<MaterialButton>(R.id.btn_4).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 4)
        }

        root.findViewById<MaterialButton>(R.id.btn_5).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 5)
        }

        root.findViewById<MaterialButton>(R.id.btn_6).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 6)
        }

        root.findViewById<MaterialButton>(R.id.btn_7).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 7)
        }

        root.findViewById<MaterialButton>(R.id.btn_8).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 8)
        }

        root.findViewById<MaterialButton>(R.id.btn_9).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 9)
        }

        root.findViewById<MaterialButton>(R.id.btn_dot).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.SET_DOT)
        }

        root.findViewById<MaterialButton>(R.id.btn_backspace).setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.REMOVE_LAST)
        }

        root.findViewById<MaterialButton>(R.id.btn_eval).setOnClickListener {
            model.commitDraw()
        }
    }
}