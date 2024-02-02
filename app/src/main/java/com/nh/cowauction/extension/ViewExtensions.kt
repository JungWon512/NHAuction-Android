package com.nh.cowauction.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nh.cowauction.utility.DLogger
import java.util.*
import kotlin.math.roundToInt

/**
 * CustomView initBinding Function..
 * Invalid LayoutId or Not LifecycleOwner Extension
 * T -> Not Support Class
 * @param layoutId View Layout Id
 * @param lifecycleOwner View LifecycleOwner
 * @param isAdd 이 함수 내에서 View 를 추가 할건지? Default true,
 * @param apply 고차 함수. (Optional)
 */
inline fun <reified T : ViewDataBinding> ViewGroup.initBinding(
        @LayoutRes layoutId: Int,
        lifecycleOwner: LifecycleOwner,
        isAdd: Boolean = true,
        apply: T.() -> Unit = {}
): T {
    val viewRoot = LayoutInflater.from(context).inflate(layoutId, this, false)
    val binding: T = DataBindingUtil.bind<T>(viewRoot)
            ?: throw NullPointerException("Invalid LayoutId ...")
    binding.lifecycleOwner = lifecycleOwner

    if (isAdd) {
        addView(binding.root)
    }

    binding.apply(apply)

    return binding
}

/**
 * RecyclerView Clear Function
 */
fun RecyclerView.clear() {
    this.adapter = null
    for (i in itemDecorationCount - 1 downTo 0) {
        removeItemDecorationAt(i)
    }
    removeAllViewsInLayout()
}

/**
 * Convert Dp to Int
 * ex. 5.dp
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
    ).toInt()

/**
 * Convert Dp to Float
 * ex. 5F.dp
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
    )

/**
 * RecyclerView notifyItemChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun <T : Any> ViewParent.recyclerViewNotifyPayload(pos: Int, item: T?) {
    if (this is RecyclerView) {
        if (item == null) {
            this.adapter?.notifyItemChanged(pos)
        } else {
            this.adapter?.notifyItemChanged(pos, item)
        }
    }
}

fun ViewParent.recyclerViewNotify(pos: Int) {
    if (this is RecyclerView) {
        this.adapter?.notifyItemChanged(pos)
    }
}

fun ViewParent.recyclerViewNotifyAll() {
    if (this is RecyclerView) {
        this.adapter?.notifyDataSetChanged()
    }
}

/**
 * getActivity
 * Class Cast Exception 방어 함수.
 */
fun Context.getActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return null
}

/**
 * ViewPager2 Get Fragment
 * @param viewPager : ViewPager2
 */
fun AppCompatActivity.getFragment(viewPager: ViewPager2): Fragment? {
    val pos = viewPager.currentItem
    return getFragment(pos, viewPager)
}

/**
 * ViewPager2 Get Fragment
 * @param pos : Current Pos
 * @param viewPager : ViewPager2
 */
fun AppCompatActivity.getFragment(pos: Int, viewPager: ViewPager2): Fragment? {
    return supportFragmentManager.findFragmentByTag("f${viewPager.adapter?.getItemId(pos)}")
}

/**
 * ViewPager2 Request Layout
 */
fun ViewPager2.requestLayout(view: View?) {
    if (view == null) return
    if (view is ViewGroup) {
        view.post {
            DLogger.d("Child Height ${view.height}")
            val widthSpec =
                    View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            DLogger.d("ViewPager2 RequestLayout ${this.layoutParams.height}   ${view.measuredHeight}")
            if (this.layoutParams.height != view.measuredHeight) {
                this.layoutParams = (this.layoutParams).also { lp ->
                    lp.height = view.measuredHeight
                }
            }
        }
    }
}

/**
 * isFakeDragging Check Current Item Func
 */
fun ViewPager2.currentItem(pos: Int, smoothScroll: Boolean = true) {
    if (isFakeDragging) {
        endFakeDrag()
    }
    setCurrentItem(pos, smoothScroll)
}

@ColorInt
fun String.alphaColor(alpha: Float): Int {
    val rounded = (((alpha * 100).roundToInt() / 100.0) * 255).roundToInt()
    var hex = Integer.toHexString(rounded).toUpperCase(Locale.ROOT)
    if (hex.length == 1) {
        hex = "0$hex"
    }
    return Color.parseColor("#$hex$this")
}

inline fun <reified T : Fragment> initFragment(args: Bundle.() -> Unit = {}): T {
    val fragment = T::class.java.newInstance()
    val bundle = Bundle()
    bundle.args()
    fragment.arguments = bundle
    return fragment
}