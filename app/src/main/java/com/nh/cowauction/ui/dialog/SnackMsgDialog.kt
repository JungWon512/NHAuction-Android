package com.nh.cowauction.ui.dialog

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import com.nh.cowauction.R
import com.nh.cowauction.databinding.DialogSnackMsgBinding
import com.nh.cowauction.utility.DLogger

/**
 * Description :
 *
 * Created by hmju on 2021-06-15
 */
class SnackMsgDialog(context: Context, private val msg: String, private val isShowKeep : Boolean= false) :
    Dialog(context, R.style.SnackMsgDialog) {

    companion object {
        var IS_SHOW = false // Dialog 중복 실행 방지.
    }

    private var binding: DialogSnackMsgBinding? = null

    override fun onStart() {
        super.onStart()
        window?.run {
            setBackgroundDrawableResource(R.drawable.bg_snack_msg)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        DialogSnackMsgBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this
            message = msg
            setContentView(this.root)

            if(!isShowKeep) {
                ObjectAnimator.ofFloat(this.root, View.ALPHA,this.root.alpha,0.0F).apply {
                    interpolator = AccelerateInterpolator()
                    duration = 500
                    startDelay = 500 /* 1000 */
                    doOnEnd {
                        dismiss()
                    }
                    start()
                }
            }
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    override fun show() {
        runCatching {
            if (!IS_SHOW) {
                super.show()
                IS_SHOW = true
            }
        }
    }

    override fun dismiss() {
        runCatching {
            super.dismiss()
            IS_SHOW = false
        }.onFailure {
            DLogger.d("dismiss $it")
        }
    }
}