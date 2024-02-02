package com.nh.cowauction.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.nh.cowauction.R
import com.nh.cowauction.utility.DLogger

/**
 * Description : Loading Dialog Class
 *
 * Created by hmju on 2021-06-10
 */
class LoadingDialog(ctx: Context) : AppCompatDialog(ctx) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        window?.run {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        setCancelable(false)

        setOnDismissListener {
            try {
                dismiss()
            } catch (ex: WindowManager.BadTokenException) {
                DLogger.e("Error ${ex.message}")
            }
        }
    }
}