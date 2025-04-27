package cc.typex.base.page

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cc.typex.app.util.DeviceInfo
import cc.typex.app.util.ImeManager
import cc.typex.app.widget.view.ErrorView
import cc.typex.app.widget.view.LoadingView
import javax.inject.Inject

abstract class DataBindingActivity<T : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var binding: T

    abstract fun getDataBindingLayoutId(): Int

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var imeManager: ImeManager

    private var loadingView: LoadingView? = null
    private var errorView: ErrorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getDataBindingLayoutId())
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val systemBarConsume =
                onApplySystemBarInsets(systemBarInsets.top, systemBarInsets.bottom)
            deviceInfo.updateSystemBarHeight(systemBarInsets.top, systemBarInsets.bottom)

            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val imeConsume = onApplyImeInsets(systemBarInsets.bottom, imeInsets.bottom)

            if (systemBarConsume && imeConsume) WindowInsetsCompat.CONSUMED else windowInsets
        }
    }

    open fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        binding.root.updatePadding(
            top = statusBarHeight,
            bottom = navigationBarHeight
        )
        return true
    }

    open fun onApplyImeInsets(navigationBarHeight: Int, imeHeight: Int): Boolean {
        binding.root.updatePadding(
            bottom = if (imeHeight > 0) imeHeight else navigationBarHeight + imeHeight
        )
        return true
    }

    protected fun showLoading() {
        val loadingViewFinal = loadingView ?: LoadingView(this).also { loadingView = it }
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

    protected fun showError(retry: (() -> Unit)? = null) {
        val errorViewFinal = errorView ?: ErrorView(this).also {
            it.setButtonClickListener { retry?.invoke() }
            errorView = it
        }
        (errorViewFinal.parent as? ViewGroup)?.removeView(errorViewFinal)
        val realRoot = findRoot(binding.root) ?: binding.root as? ViewGroup
        realRoot?.addView(
            errorViewFinal, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    protected fun hideError() {
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

    protected open fun checkImeOnResume() = true

    override fun onResume() {
        super.onResume()
        if (checkImeOnResume() && !imeManager.isThisImeSelected) {
            imeManager.gotoEnableKeyboard(this)
            finish()
        }
    }
}