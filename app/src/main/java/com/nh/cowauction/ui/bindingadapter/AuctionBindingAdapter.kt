package com.nh.cowauction.ui.bindingadapter

import android.animation.ObjectAnimator
import android.text.TextUtils
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.nh.cowauction.R
import com.nh.cowauction.contants.AuctionSnackType
import com.nh.cowauction.contants.AuctionState
import com.nh.cowauction.contants.EntryTitleType
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.widget.visual.CustomSurfaceRenderer

object AuctionBindingAdapter {

    @JvmStatic
    @BindingAdapter("auctionStateType")
    fun setAuctionStateMessage(
        textView: AppCompatTextView,
        type: AuctionState
    ) {
        when (type) {
            AuctionState.COUNT_DOWN -> {
                textView.text = HtmlCompat.fromHtml(
                    String.format(textView.resources.getString(type.msg), type.etc[0]),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            AuctionState.OTHER_SUCCESS_BID_WON,
            AuctionState.OTHER_SUCCESS_BID_DEFAULT,
            AuctionState.OTHER_SUCCESS_BID -> {
                textView.text = HtmlCompat.fromHtml(
                    String.format(textView.resources.getString(type.msg), type.etc[0], type.etc[1]),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            AuctionState.SUCCESS_BID_WON,
            AuctionState.SUCCESS_BID_DEFAULT,
            AuctionState.SUCCESS_BID -> {
                textView.text = HtmlCompat.fromHtml(
                    String.format(textView.resources.getString(type.msg), type.etc[0]),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            else -> {
                textView.text = HtmlCompat.fromHtml(
                    textView.resources.getString(type.msg),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }
    }

    @JvmStatic
    @BindingAdapter("rendererThumbImageView")
    fun setSurfaceRendererThumbImageView(
        surfaceView: CustomSurfaceRenderer,
        imgView: View
    ) {
        surfaceView.setThumbImageView(imgView)
    }

    @JvmStatic
    @BindingAdapter(value = ["auctionEntryType", "auctionEntryContents"], requireAll = true)
    fun setAuctionEntryContentsText(
        view: AppCompatTextView,
        type: EntryTitleType?,
        value: String?
    ) {
        if (type == null || value == null) return

        if (type == EntryTitleType.NOTE) {
            view.ellipsize = TextUtils.TruncateAt.MARQUEE
            view.marqueeRepeatLimit = -1
            view.isSelected = true
        } else {
            view.ellipsize = TextUtils.TruncateAt.START
            view.isSelected = false
        }
        view.text = HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    @JvmStatic
    @BindingAdapter(value = ["auctionEntryType", "auctionEntryContents", "isMasking"], requireAll = true)
    fun setAuctionEntryContentsMaskingText(
        view: AppCompatTextView,
        type: EntryTitleType?,
        value: String?,
        masking: Boolean
    ) {
        if (type == null || value == null || value.isNullOrEmpty()) return

        view.ellipsize = TextUtils.TruncateAt.START
        view.isSelected = false

        if (masking && !value.contains("*")) {

            var maskingValue = ""

            if (value.length == 1) {
                maskingValue = value
            } else if (value.length > 1) {
                var middle = value.substring(1, 2)
                maskingValue = value.replaceFirst(middle, "*")
            }
            view.text = HtmlCompat.fromHtml(maskingValue, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            view.text = HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["auctionSnackType"])
    fun setAuctionSnackType(
        tv: AppCompatTextView,
        type: AuctionSnackType
    ) {

        if (tv == null) {
            return;
        }

        when (type) {
            AuctionSnackType.BIDDING_SUCCESS -> {
                DLogger.d("AuctionSnackType Succ")
                tv.alpha = 1.0F
                tv.text = HtmlCompat.fromHtml(tv.resources.getString(type.msg), HtmlCompat.FROM_HTML_MODE_LEGACY)
                tv.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(tv, View.ALPHA, 0.0F).apply {
                    interpolator = AccelerateInterpolator()
                    duration = 500
                    startDelay = 500 /* 1000 */
                    doOnEnd {
                        tv.visibility = View.GONE
                        tv.text = ""
                    }
                    start()
                }
            }
            AuctionSnackType.GONE -> {
                DLogger.d("AuctionSnackType Gone")
                tv?.text = ""
                tv?.visibility = View.GONE
            }
            AuctionSnackType.READY,
            AuctionSnackType.COMPLETE -> {
                DLogger.d("AuctionSnackType Ready")
                tv.visibility = View.VISIBLE
                tv.alpha = 1.0F
                tv.text = HtmlCompat.fromHtml(tv.resources.getString(type.msg), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["isFullScreen"])
    fun toggleFullScreen(
        iv: AppCompatImageView,
        state: Boolean
    ) {
        if (state) {
            iv.setImageResource(R.drawable.ic_fullscreen_exit)
        } else {
            iv.setImageResource(R.drawable.ic_fullscreen)
        }
    }
}