package com.nh.cowauction.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import com.nh.cowauction.R
import com.nh.cowauction.contants.LoadingDialogState
import com.nh.cowauction.ui.dialog.LoadingDialog
import com.nh.cowauction.ui.splash.SplashActivity

/**
 * Description : BaseActivity Class
 *
 * Created by hmju on 2021-05-18
 */
abstract class BaseActivity<Binding : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity(),
        LifecycleObserver {
    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int
    abstract val viewModel: VM
    abstract val bindingVariable: Int
    private var loadingDialog: LoadingDialog? = null
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<Binding>(this, layoutId).apply {
            lifecycleOwner = this@BaseActivity
            setVariable(bindingVariable, viewModel)
            _binding = this
        }

        with(viewModel) {
            startLoadingDialog.observe(this@BaseActivity, { state ->
                performLoadingDialog(state)
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearDisposable()

        // Binding Memory Leak 방어 코드.
        _binding = null
    }

    override fun finish() {
        super.finish()
        if (this !is SplashActivity) {
            overridePendingTransition(R.anim.out_left_to_right, R.anim.in_left_to_right)
        }
    }

    protected fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.run {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    /**
     * Status Bar 영역까지 넓히는 함수.
     */
    @Suppress("DEPRECATION")
    protected fun setFitsWindows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.run {
                hide(WindowInsets.Type.navigationBars())
                // Statusbar 영역까지 넓힘.
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
        } else {
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    /**
     * 로딩 화면 비노출
     */
    protected fun dismissLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        loadingDialog = null
    }

    /**
     * 로딩 프로그래스 바 노출 유무
     */
    @MainThread
    protected fun performLoadingDialog(state: LoadingDialogState) {
        // Show Progress Dialog
        if (LoadingDialogState.SHOW == state) {
            if (isFinishing) {
                return
            }

            loadingDialog?.let {
                if (!it.isShowing) {
                    it.show()
                }
            } ?: run {
                loadingDialog = LoadingDialog(this).run {
                    show()
                    this
                }
            }
        } else {
            // Dismiss Progress Dialog
            loadingDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
            loadingDialog = null
        }
    }

    fun showToast(@StringRes strId: Int) {
        showToast(getString(strId))
    }

    fun showToast(msg: String) {
        runOnUiThread {
            if (toast != null) {
                toast?.cancel()
                toast = null
            }
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }
}