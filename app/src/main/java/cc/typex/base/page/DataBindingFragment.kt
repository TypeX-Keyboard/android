package cc.typex.base.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import cc.typex.app.widget.view.ErrorView
import cc.typex.app.widget.view.LoadingView

abstract class DataBindingFragment<T : ViewDataBinding> : Fragment() {

    protected lateinit var binding: T

    private var loadingView: LoadingView? = null
    private var errorView: ErrorView? = null

    abstract fun getDataBindingLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, getDataBindingLayoutId(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            ViewCompat.setOnApplyWindowInsetsListener(binding.root, null)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val consume = onApplySystemBarInsets(insets.top, insets.bottom)
            if (consume) WindowInsetsCompat.CONSUMED else windowInsets
        }
    }

    open fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        binding.root.updatePadding(
            top = statusBarHeight,
            bottom = navigationBarHeight
        )
        return true
    }

    protected fun showLoading() {
        val loadingViewFinal =
            loadingView ?: LoadingView(requireContext()).also { loadingView = it }
        (loadingViewFinal.parent as? ViewGroup)?.removeView(loadingViewFinal)
        val realRoot = findRoot(binding.root) ?: binding.root as? ViewGroup
        realRoot?.addView(
            loadingViewFinal, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    protected fun hideLoading() {
        val loadingViewFinal = loadingView ?: return
        (loadingViewFinal.parent as? ViewGroup)?.removeView(loadingViewFinal)
    }

    protected fun showError() {
        val errorViewFinal = errorView ?: ErrorView(requireContext()).also { errorView = it }
        (errorViewFinal.parent as? ViewGroup)?.removeView(errorViewFinal)
        val realRoot = findRoot(binding.root) ?: binding.root as? ViewGroup
        realRoot?.addView(
            errorViewFinal, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun hideError() {
        val errorViewFinal = errorView ?: return
        (errorViewFinal.parent as? ViewGroup)?.removeView(errorViewFinal)
    }


    private fun findRoot(view: View): ViewGroup? {
        var current: View = view
        while (current.parent is ViewGroup) {
            val parent = current.parent as ViewGroup
            if (parent.id == android.R.id.content) {
                return parent
            }
            current = parent
        }
        return null
    }
}