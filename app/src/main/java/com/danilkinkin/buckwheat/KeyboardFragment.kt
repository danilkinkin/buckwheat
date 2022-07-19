package com.danilkinkin.buckwheat

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.danilkinkin.buckwheat.utils.toDP
import com.danilkinkin.buckwheat.utils.toSP
import com.danilkinkin.buckwheat.viewmodels.AppViewModel
import com.danilkinkin.buckwheat.viewmodels.DrawsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class KeyboardFragment : Fragment() {
    private lateinit var model: DrawsViewModel
    private lateinit var appModel: AppViewModel

    private val root: MotionLayout by lazy {
        requireView().findViewById(R.id.root)
    }

    private val n0Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_0)
    }

    private val n1Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_1)
    }

    private val n2Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_2)
    }

    private val n3Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_3)
    }

    private val n4Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_4)
    }

    private val n5Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_5)
    }

    private val n6Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_6)
    }

    private val n7Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_7)
    }

    private val n8Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_8)
    }

    private val n9Btn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_9)
    }

    private val backspaceBtn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_backspace)
    }

    private val dotBtn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_dot)
    }

    private val evalBtn: MaterialButton by lazy {
        requireView().findViewById(R.id.btn_eval)
    }

    var listBtns: ArrayList<MaterialButton>? = null

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
        val appModel: AppViewModel by activityViewModels()

        this.model = model
        this.appModel = appModel

        build()
    }

    fun anim(progress: Float) {
        root.progress = 1 - progress
    }

    fun build() {
        listBtns = arrayListOf(n0Btn, n1Btn, n2Btn, n3Btn, n4Btn, n5Btn, n6Btn, n7Btn, n8Btn, n9Btn, dotBtn)

        n0Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 0)
        }

        n1Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 1)
        }

        n2Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 2)
        }

        n3Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 3)
        }

        n4Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 4)
        }

        n5Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 5)
        }

        n6Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 6)
        }

        n7Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 7)
        }

        n8Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 8)
        }

        n9Btn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.PUT_NUMBER, 9)
        }

        dotBtn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.SET_DOT)
        }

        backspaceBtn.setOnClickListener {
            this.model.executeAction(DrawsViewModel.Action.REMOVE_LAST)
        }

        evalBtn.setOnClickListener {
            if ("${model.valueLeftDot}.${model.valueRightDot}" == "00000000.") {
                model.resetDraw()

                appModel.setIsDebug(!appModel.isDebug.value!!)

                Snackbar
                    .make(requireView(), "Debug ${if (appModel.isDebug.value!!) { "ON" } else { "OFF" }}", Snackbar.LENGTH_LONG)
                    .show()

                return@setOnClickListener
            }

            model.commitDraw()
        }

        root.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                val shiftProgress: Float = if (progress < 0.5) {
                    0F
                } else {
                    (progress - 0.5F) * 2F
                }

                backspaceBtn.iconSize = (36.toDP() - 12.toDP() * shiftProgress).toInt()

                listBtns?.forEach {
                    it.textSize = 26.toSP() - 12.toSP() * shiftProgress
                }
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                val shiftProgress =  if (currentId == R.id.end) {
                    1F
                } else {
                    0F
                }

                backspaceBtn.iconSize = (36.toDP() - 12.toDP() * shiftProgress).toInt()

                listBtns?.forEach {
                    it.textSize = 26.toSP() - 12.toSP() * shiftProgress
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {

            }

        })
    }
}