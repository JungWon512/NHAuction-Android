package com.nh.cowauction.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.nh.cowauction.contants.LoadingDialogState
import com.nh.cowauction.ui.dialog.LoadingDialog
import com.nh.cowauction.utility.DLogger

/**
 * Description : BaseFragment Class
 *
 * Created by juhongmin on 5/18/21
 */
abstract class BaseFragment<Binding : ViewDataBinding, VM : BaseViewModel> : Fragment(),
    LifecycleOwner {

    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int
    abstract val viewModel: VM
    abstract val bindingVariable: Int
    private var isActivityViewModel = false
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<Binding>(
            inflater,
            layoutId,
            container,
            false
        ).apply {
            lifecycleOwner = this@BaseFragment
            setVariable(bindingVariable, viewModel)
            _binding = this

            runCatching {
                ViewModelProvider(requireActivity())[viewModel::class.java]
            }.onFailure {
                // viewModel -> Activity ViewModel 이 아니다.
                isActivityViewModel = false
            }.onSuccess {
                // viewModel -> Activity ViewModel 인경우.
                isActivityViewModel = true
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isActivityViewModel) {
            with(viewModel) {
                startLoadingDialog.observe(viewLifecycleOwner, { state ->
                    DLogger.d("Loading $state")
                    performLoadingDialog(state)
                })
            }
        }
    }

    /**
     * 로딩 프로그래스 바 노출 유무
     */
    @MainThread
    protected fun performLoadingDialog(state: LoadingDialogState) {
        // Show Progress Dialog
        if (LoadingDialogState.SHOW == state) {
            if (requireActivity().isFinishing) {
                return
            }

            loadingDialog?.let {
                if (!it.isShowing) {
                    it.show()
                }
            } ?: run {
                loadingDialog = LoadingDialog(requireContext()).run {
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Binding Memory Leak 방어 코드.
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // Activity ViewModel 아닌경우에만 ClearDisposable
        if (!isActivityViewModel) {
            viewModel.run {
                clearDisposable()
            }
        }

    }
}