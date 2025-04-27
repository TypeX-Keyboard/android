package cc.typex.app.page.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import cc.typex.app.R
import cc.typex.app.databinding.WebFragmentBinding
import cc.typex.app.web.AppWebViewFactory
import cc.typex.base.page.DataBindingFragment
import cc.typex.base.page.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WebActivity : FragmentContainerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val url =
                intent.getStringExtra("url") ?: throw IllegalArgumentException("No url provided")
            addFragment(WebFragment.newInstance(url))
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context, url: String) {
            val starter = Intent(context, WebActivity::class.java)
                .putExtra("url", url)
            context.startActivity(starter)
        }
    }
}

@AndroidEntryPoint
class WebFragment : DataBindingFragment<WebFragmentBinding>() {

    @Inject
    lateinit var webViewFactory: AppWebViewFactory

    override fun getDataBindingLayoutId() = R.layout.web_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etvToolbar.setIconLeftClickListener {
            if (binding.ewvWebView.canGoBack()) {
                binding.ewvWebView.goBack()
            } else {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        webViewFactory.injectDependencies(binding.ewvWebView)

        val url = arguments?.getString("url") ?: throw IllegalArgumentException("No url provided")
        binding.ewvWebView.loadUrl(url)
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String): WebFragment {
            val fragment = WebFragment()
            fragment.arguments = Bundle().apply {
                putString("url", url)
            }
            return fragment
        }
    }
}