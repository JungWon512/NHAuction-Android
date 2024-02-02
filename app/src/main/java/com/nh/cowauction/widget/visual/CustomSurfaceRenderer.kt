package com.nh.cowauction.widget.visual

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import com.nh.cowauction.utility.DLogger
import io.agora.base.internal.SurfaceViewRenderer

/**
 * Description :
 *
 * Created by hmju on 2021-06-30
 */
class CustomSurfaceRenderer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : SurfaceViewRenderer(context, attrs) {

    private var isRelease: Boolean = false
    private var thumbnailView: View? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var isAni : Boolean = false
    private var objectAnimator : ObjectAnimator? = null

    init {
        visibility = View.VISIBLE
    }

    fun setThumbImageView(thumbnailView: View) {
        this.thumbnailView = thumbnailView
    }

    private fun startAni(){
        if(objectAnimator == null) {
            thumbnailView?.let { view ->
                objectAnimator = ObjectAnimator.ofFloat(view,View.ALPHA,view.alpha,0.0F).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    duration = 500
                    addListener(
                            onStart = {
                                DLogger.d("Animation Start ${tag}")
                                view.visibility = View.VISIBLE
                                this@CustomSurfaceRenderer.visibility = View.VISIBLE
                            },
                            onRepeat = {
                                DLogger.d("Animation onRepeat!! $tag")
                            },
                            onCancel = {
                                DLogger.d("Animation Cancel!! $tag")
                                view.alpha = 1.0F
                                view.visibility = VISIBLE
                                this@CustomSurfaceRenderer.visibility = View.INVISIBLE
                            },
                            onEnd = {
                                DLogger.d("Animation onEnd!! $tag ")
                                view.visibility = View.GONE
                                isRelease = false
                            }
                    )
                }
            }
        }
        if(objectAnimator?.isRunning == true) {
            DLogger.d("에니메이션 동작중입니다.")
            objectAnimator?.cancel()
        }
        objectAnimator?.start()

    }

//    override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {
//        super.onFrameResolutionChanged(videoWidth, videoHeight, rotation)
////        DLogger.d("onFrameResolutionChanged $isRelease $tag ${thumbnailView?.alpha}")
//        uiHandler.post {
//
//            startAni()
////            if (isRelease) {
////            if(!isAni) {
////                thumbnailView?.let { imgView ->
////                    ObjectAnimator.ofFloat(imgView, View.ALPHA, imgView.alpha, 0.0F).apply {
////                        interpolator = AccelerateDecelerateInterpolator()
////                        duration = 500
////                        addListener(
////                                onStart = {
////                                    this@CustomSurfaceRenderer.visibility = View.VISIBLE
////                                    isAni = true
////                                },
////                                onCancel = {
////                                    DLogger.d("Animation Cancel!! ")
////                                },
////                                onEnd = {
////                                    DLogger.d("Animation onEnd!! ")
////                                    imgView.visibility = View.GONE
////                                    isRelease = false
////                                    isAni = false
////                                })
////                        start()
////                    }
////                }
////            }
//
////            }
//
////            else {
////                visibility = View.VISIBLE
////            }
//        }
//    }

//    override fun release() {
//        uiHandler.post {
//            objectAnimator?.cancel()
////            DLogger.d("release ${tag}")
////            thumbnailView?.let { view ->
////                view.visibility = View.VISIBLE
////                view.alpha = 1.0F
////            }
////            visibility = View.INVISIBLE
////            isRelease = true
//        }
//        super.release()
//    }
}