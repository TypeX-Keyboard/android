package cc.typex.app.page.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import cc.typex.app.R
import cc.typex.app.databinding.MainFragmentBinding
import cc.typex.app.util.EnvManager
import cc.typex.app.web.AppWebViewFactory
import cc.typex.base.page.DataBindingFragment
import cc.typex.base.page.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentContainerActivity() {

    override fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            addFragment(MainFragment())
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            context.startActivity(intent)
        }
    }
}

@AndroidEntryPoint
class MainFragment : DataBindingFragment<MainFragmentBinding>() {

    @Inject
    lateinit var envManager: EnvManager

    @Inject
    lateinit var webViewFactory: AppWebViewFactory

    override fun getDataBindingLayoutId() = R.layout.main_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webViewFactory.injectDependencies(binding.ewvWebView)
        binding.ewvWebView.loadUrl(envManager.getWebHomeUrl())
    }
}