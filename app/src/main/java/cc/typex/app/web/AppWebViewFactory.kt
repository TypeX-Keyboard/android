package cc.typex.app.web

import android.content.Context
import cc.typex.app.util.EnvManager
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AppWebViewFactory @Inject constructor(
    private val webViewClient: Provider<AppWebViewClient>,
    private val webChromeClient: Provider<AppChromeClient>,
    private val jsBridge: Provider<AppJsBridge>,
    private val envManager: Provider<EnvManager>,
) {

    fun create(context: Context): AppWebView {
        val webView = AppWebView(context)
        injectDependencies(webView)
        return webView
    }

    fun injectDependencies(webView: AppWebView) {
        webView.webViewClient = webViewClient.get()
        webView.webChromeClient = webChromeClient.get()
        webView.jsBridge = jsBridge.get()
        webView.envManager = envManager.get()
        webView.initSetting()
    }
}