package com.nh.cowauction.ui.bindingadapter

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseFragmentPagerAdapter
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.FragmentType
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.ui.auction.WatchAuctionActivity
import com.nh.cowauction.viewmodels.WatchAuctionViewModel
import com.nh.cowauction.widget.indicator.BaseIndicatorView
import com.nh.cowauction.widget.pagertablayout.LinePagerTabLayout
import com.nh.cowauction.widget.pagertablayout.PagerTabItem
import io.agora.base.internal.SurfaceViewRenderer
import io.agora.base.internal.video.RendererCommon

/**
 * Description : 공통으로 쓰이는 Binding Adapter
 *
 * Created by hmju on 2021-05-20
 */
object CommonBindingAdapter {
    // [s] Common View =============================================================================
    /**
     * set TextView
     * default Type
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(
        textView: TextView,
        text: String?
    ) {
        if (!text.isNullOrEmpty()) {
            val oldText = textView.text.toString()
            if (oldText == text) {
                return
            }

            textView.text = text
        }
    }

    /**
     * set TextView
     * Resource Id Type
     */
    @JvmStatic
    @BindingAdapter("resourceText")
    fun setResourceText(
        textView: AppCompatTextView,
        resourceId: Int? //Default Argument 0
    ) {
        if (resourceId != 0 && resourceId != -1) {
            textView.setText(resourceId!!)
        }
    }

    @JvmStatic
    @BindingAdapter("intText")
    fun setIntText(
        textView: AppCompatTextView,
        value: Int?
    ) {
        textView.text = "${value ?: ""}"
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:text", event = "android:textAttrChanged")
    fun getTextString(view: TextView): String {
        return view.text.toString()
    }

    /**
     * set TextView
     * htmlText Type
     */
    @JvmStatic
    @BindingAdapter("htmlText")
    fun setHtmlText(
        textView: TextView,
        text: String?
    ) {
        if (text != null && text.trim().isNotEmpty()) {
            textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }


    /**
     * set TextView
     * String Format Type
     * 초기에 null 보이는 이슈 처리용.
     */
    @JvmStatic
    @BindingAdapter(value = ["fmtText", "data"], requireAll = true)
    fun setFmtTextData(
        textView: AppCompatTextView,
        fmtText: String,
        data: Any?
    ) {
        if (data != null) {
            textView.text = fmtText
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @Suppress("DEPRECATION")
    @BindingAdapter(value = ["fmtHtmlText", "data"], requireAll = true)
    fun setFmtHtmlTextData(
        textView: AppCompatTextView,
        fmtText: String,
        data: Any?
    ) {
        if (data != null) {
            textView.text = HtmlCompat.fromHtml(fmtText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    @BindingAdapter(value = ["hapticFeedback"])
    fun setHapticFeedBack(
        view: View,
        feedBack: Boolean
    ) {
        val vibrator: Vibrator = view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        view.isHapticFeedbackEnabled = feedBack
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (ContextCompat.checkSelfPermission(view.context, Manifest.permission.VIBRATE) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100L, 1))
                    } else {
                        vibrator.vibrate(100L)
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageDrawable(view: ImageView, drawable: Drawable) {
        view.setImageDrawable(drawable)
    }

    // [s] 중복 클릭 방지 리스너
    class OnSingleClickListener(private val onSingleCLick: (View) -> Unit) : View.OnClickListener {
        companion object {
            const val CLICK_INTERVAL = 500
        }

        private var lastClickedTime: Long = 0L

        override fun onClick(v: View?) {
            v?.let {
                if (isSafe()) {
                    onSingleCLick(it)
                }
                lastClickedTime = System.currentTimeMillis()
            }
        }

        private fun isSafe() = System.currentTimeMillis() - lastClickedTime > CLICK_INTERVAL
    }

    fun View.setOnSingleClickListener(onSingleCLick: (View) -> Unit) {
        val singleClickListener = OnSingleClickListener {
            onSingleCLick(it)
        }
        setOnClickListener(singleClickListener)
    }

    @JvmStatic
    @BindingAdapter("turtleClick")
    fun setTurtleClick(
        view: View,
        listener: View.OnClickListener
    ) {
        view.setOnClickListener(OnSingleClickListener {
            listener.onClick(it)
        })
    }
// [e] 중복 클릭 방지 리스너

    /**
     * set View Visible
     */
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(
        view: View,
        visible: Boolean
    ) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("inVisibility")
    fun setInVisibility(
        view: View,
        inVisible: Boolean?
    ) {
        view.visibility = if (inVisible == true) View.VISIBLE else View.INVISIBLE
    }

    /**
     * EditText
     * Action Next Done.
     */
    @JvmStatic
    @BindingAdapter("editNextDone")
    fun setEditTextListener(
        editText: AppCompatEditText,
        listener: View.OnClickListener
    ) {
        editText.setOnEditorActionListener { v, actionId, event ->
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                listener.onClick(v)
            }
            return@setOnEditorActionListener false
        }
    }


    @JvmStatic
    @BindingAdapter("android:enabled")
    fun setEnabled(
        view: View,
        isEnabled: Boolean
    ) {
        view.isEnabled = isEnabled
    }

    /**
     * set Layout Width or Height
     */
    @JvmStatic
    @BindingAdapter(value = ["layout_width", "layout_height"], requireAll = false)
    fun setLayoutWidthAndHeight(
        view: View,
        @Dimension width: Int?,
        @Dimension height: Int?
    ) {
        val layoutParams = view.layoutParams
        width?.run {
            layoutParams.width = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        height?.run {
            layoutParams.height = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        view.layoutParams = layoutParams
    }

    /**
     * set Typeface TextView
     */
    @JvmStatic
    @BindingAdapter("android:textStyle")
    fun setTextViewTypeFace(
        textView: AppCompatTextView,
        style: String
    ) {
        when (style) {
            "bold" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_bold
                    ), Typeface.BOLD
                )
            }
            "normal" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_medium
                    ), Typeface.NORMAL
                )
            }
            "italic" -> {
                textView.setTypeface(
                    ResourcesCompat.getFont(
                        textView.context,
                        R.font.font_noto_sans_regular
                    ), Typeface.ITALIC
                )
            }
            else -> {
            }
        }
    }

    /**
     * set View Selected
     */
    @JvmStatic
    @BindingAdapter("isSelected")
    fun setSelected(
        view: View,
        isSelect: Boolean
    ) {
        if (view is TextView) {
            view.isSelected = isSelect
        } else {
            view.isSelected = isSelect
        }

    }

    @JvmStatic
    @BindingAdapter(value = ["viewPager", "indicatorCnt"], requireAll = false)
    fun setIndicatorViewPager(
        indicatorView: BaseIndicatorView,
        viewPager: ViewPager2?,
        dataCnt: Int?
    ) {
        indicatorView.viewPager = viewPager
        indicatorView.dataCnt = dataCnt ?: 0
    }

    /**
     * set Line Tab Layout
     */
    @JvmStatic
    @BindingAdapter(value = ["viewPager", "menuList", "fixedSize"], requireAll = false)
    fun setLineTabDataList(
        view: LinePagerTabLayout,
        viewPager: ViewPager2,
        dataList: MutableList<PagerTabItem>?,
        fixedSize: Int?
    ) {
        view.viewPager = viewPager
        view.fixedSize = fixedSize ?: -1
        if (!dataList.isNullOrEmpty()) {
            view.dataList = dataList
        }
    }

    @JvmStatic
    @BindingAdapter("android:textColor")
    fun setTextColor(
        view: TextView,
        @ColorRes color: Int
    ) {
        view.setTextColor(ContextCompat.getColor(view.context, color))
    }

    @JvmStatic
    @BindingAdapter("rendererScaleType")
    fun setSurfaceViewRendererScaleType(
        view: SurfaceViewRenderer,
        scaleType: RendererCommon.ScalingType
    ) {
        view.setScalingType(scaleType)
    }

    @JvmStatic
    @BindingAdapter("imgBitmap")
    fun setImageViewBitmap(
        view: ImageView,
        bitmap: Bitmap?
    ) {
        if (bitmap == null) return
        view.setImageBitmap(bitmap)
    }

    @JvmStatic
    @BindingAdapter("alphaAniVisible")
    fun setAlphaAniVisibility(
        view: View,
        isVisible: Boolean
    ) {
        if (isVisible) {
            ObjectAnimator.ofFloat(view, View.ALPHA, view.alpha, 1.0F).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 500
                doOnStart { view.visibility = View.VISIBLE }
                start()
            }
        } else {
            ObjectAnimator.ofFloat(view, View.ALPHA, view.alpha, 0.0F).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 500
                doOnEnd { view.visibility = View.GONE }
                start()
            }
        }
    }
    // [e] Common View =============================================================================

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @BindingAdapter(value = ["itemType", "dataList", "viewModel"], requireAll = false)
    fun <T : Any> setFragmentViewPagerAdapter(
        viewPager: ViewPager2,
        type: FragmentType,
        dataList: MutableList<T>?,
        viewModel: BaseViewModel?
    ) {
        if (viewPager.adapter == null) {
            viewPager.adapter = when (type) {
                FragmentType.AUCTION_TOP -> {

                    if (viewModel is WatchAuctionViewModel) {
                        WatchAuctionActivity.AuctionTopFragmentPagerAdapter(
                            viewPager.context,
                            viewModel
                        )
                    }else{
                        AuctionActivity.AuctionTopFragmentPagerAdapter(
                            viewPager.context,
                            viewModel
                        )
                    }
                }
            }
        }

        if (dataList != null && dataList.size > 0) {
            (viewPager.adapter as BaseFragmentPagerAdapter<T>).run {
                if (this.dataList !== dataList || this.dataList.size != dataList.size) {
                    if (viewPager[0] is RecyclerView) {
                        (viewPager[0] as RecyclerView).removeAllViewsInLayout()
                    }
                    setDataList(dataList)
                }
            }
        }
    }
}