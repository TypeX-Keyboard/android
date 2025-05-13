package cc.typex.app.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import cc.typex.app.util.EnvManager
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
class AppWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : WebView(context, attrs, defStyleAttr, defStyleRes) {

    lateinit var webViewClient: AppWebViewClient

    lateinit var webChromeClient: AppChromeClient

    lateinit var jsBridge: AppJsBridge

    lateinit var envManager: EnvManager

    private var isInitialized = false

    fun initSetting() {
        if (isInitialized) return

        settings.javaScriptEnabled = true

        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.domStorageEnabled = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        setWebContentsDebuggingEnabled(envManager.isDebugEnv)

        jsBridge.bindToWebView(this)

        setWebViewClient(webViewClient)
//        setWebViewClient(CachingWebViewClient())
        setWebChromeClient(webChromeClient)

        isInitialized = true
    }

    fun invokeJsBridgeMethod(method: String, params: Map<String, Any>) {
        jsBridge.callJsBridgeMethod(method, params)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Timber.v("onVisibilityChanged: $changedView, $visibility")
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        Timber.v("onVisibilityAggregated: $isVisible")
        if (isVisible) {
            jsBridge.checkUnsignedTransaction()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Timber.v("onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.v("onDetachedFromWindow")
    }
}