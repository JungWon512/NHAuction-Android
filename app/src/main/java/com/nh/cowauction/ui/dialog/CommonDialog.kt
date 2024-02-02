package com.nh.cowauction.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.nh.cowauction.R
import com.nh.cowauction.utility.DLogger

/**
 * Description : 공통 팝업
 *
 * Created by hmju on 2021-06-23
 */
class CommonDialog(private val context: Context) {

    interface Listener {
        fun onClick(which: Int)
    }

    private val rootView: View by lazy {
        View.inflate(context, R.layout.dialog_common, null)
    }
    private val llButtons: LinearLayoutCompat by lazy { rootView.findViewById(R.id.llButton) }
    private val negativeButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvNegative) }
    private val positiveButton: AppCompatTextView by lazy { rootView.findViewById(R.id.tvPositive) }
    private var tvContents: AppCompatTextView? = null
    private var isCancel: Boolean = false
    private var listener: Listener? = null
    private var buttonCnt = 0

    private lateinit var dialog: Dialog

    companion object {
        const val POSITIVE: Int = 1
        const val NEGATIVE: Int = 2
    }

    fun setContents(@StringRes id: Int): CommonDialog {
        return setContents(context.getString(id))
    }

    @Suppress("DEPRECATION")
    fun setContents(text: String): CommonDialog {
        if (tvContents == null) {
            tvContents = rootView.findViewById(R.id.tvContents)
        }

        if (text.isNotEmpty()) {
            tvContents?.let { textView ->
                textView.visibility = View.VISIBLE
                textView.text = HtmlCompat.fromHtml(
                        text.replace("\n", "<br>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }
        return this
    }

    fun setNegativeButton(@StringRes id: Int): CommonDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setNegativeButton(context.getString(id))
        }
    }

    fun setNegativeButton(text: String): CommonDialog {
        llButtons.visibility = View.VISIBLE
        negativeButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(NEGATIVE)
            }
        }
        buttonCnt++
        return this
    }

    fun setPositiveButton(@StringRes id: Int): CommonDialog {
        // ID 유효성 체크.
        return if (id == View.NO_ID) {
            this
        } else {
            setPositiveButton(context.getString(id))
        }
    }

    fun setPositiveButton(text: String): CommonDialog {
        llButtons.visibility = View.VISIBLE
        positiveButton.apply {
            this.text = text
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
                listener?.onClick(POSITIVE)
            }
        }
        buttonCnt++
        return this
    }

    fun setCancelable(isCancel: Boolean): CommonDialog {
        this.isCancel = isCancel
        return this
    }

    fun setListener(listener: Listener): CommonDialog {
        this.listener = listener
        return this
    }

    fun show() {
        // 원버튼인경우
        if (buttonCnt == 1) {
            if (positiveButton.visibility == View.VISIBLE) {
                positiveButton.setBackgroundColor(
                        ContextCompat.getColor(
                                context,
                                R.color.color_007eff
                        )
                )
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (negativeButton.visibility == View.VISIBLE) {
                negativeButton.setBackgroundColor(
                        ContextCompat.getColor(
                                context,
                                R.color.color_007eff
                        )
                )
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        } else {
            // 투버튼인 경우
//            vDividerLine.visibility = View.VISIBLE
//            vTopLine.visibility = View.VISIBLE
        }

        val builder = AlertDialog.Builder(context, R.style.CommonDialog).setView(rootView)
        builder.setCancelable(isCancel)
        runCatching {
            dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.bg_common_dialog)
            dialog.show()
        }
    }

    fun show(dismissListener: DialogInterface.OnDismissListener) {
        // 원버튼인경우
        if (buttonCnt == 1) {
            if (positiveButton.visibility == View.VISIBLE) {
                positiveButton.setBackgroundColor(
                        ContextCompat.getColor(
                                context,
                                R.color.color_007eff
                        )
                )
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (negativeButton.visibility == View.VISIBLE) {
                negativeButton.setBackgroundColor(
                        ContextCompat.getColor(
                                context,
                                R.color.color_007eff
                        )
                )
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }

        val builder = AlertDialog.Builder(context, R.style.CommonDialog).setView(rootView)
        builder.setCancelable(isCancel)
        dialog = builder.create().apply {
            try {
                window?.setBackgroundDrawableResource(R.drawable.bg_common_dialog)
                setOnDismissListener(dismissListener)
                show()
            } catch (ex: WindowManager.BadTokenException) {
                DLogger.e(ex.toString())
            }
        }
    }

    fun dismiss() {
        Handler(Looper.getMainLooper()).post {
            try {
                dialog.dismiss()
            } catch (ex: WindowManager.BadTokenException) {
                DLogger.e("Dismiss BadToken Error ${ex.message}")
            }
        }
    }

}