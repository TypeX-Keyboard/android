package cc.typex.app

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import autodispose2.ScopeProvider
import autodispose2.autoDispose
import cc.typex.app.page.SplashActivity
import cc.typex.app.util.AppKeyInterceptor
import cc.typex.app.util.ClipboardManager
import cc.typex.app.util.EnvManager
import cc.typex.app.util.ImeManager
import cc.typex.app.util.StaticsManager
import cc.typex.app.util.SystemSettingManager
import cc.typex.app.util.WalletManager
import cc.typex.app.util.WebAssetsManager
import cc.typex.app.web.AppWebView
import cc.typex.app.web.AppWebViewFactory
import cc.typex.app.widget.keyboard.KeyboardCandidateButton
import cc.typex.app.widget.view.KeyboardChargingView
import com.crypto.future.keyboard3.InputViewListener
import com.crypto.future.keyboard3.K3IMEService
import com.crypto.future.keyboard3.widget.KeyboardViewsProvider
import com.crypto.k3imesdk.LatinSDK
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class TypeXApp : Application() {

    @Inject
    lateinit var imeManager: ImeManager

    @Inject
    lateinit var envManager: EnvManager

    @Inject
    lateinit var webViewFactory: Lazy<AppWebViewFactory>

    @Inject
    lateinit var clipboardManager: Lazy<ClipboardManager>

    @Inject
    lateinit var keyInterceptor: Provider<AppKeyInterceptor>

    @Inject
    lateinit var systemSettingManager: SystemSettingManager

    @Inject
    lateinit var webAssetsManager: WebAssetsManager

    @Inject
    lateinit var staticsManager: Lazy<StaticsManager>

    private val customViewDispatcher = CustomViewDispatcher()

    @Inject
    lateinit var keyboardChargingViewProvider: Lazy<KeyboardChargingViewDispatcher>

    @Inject
    lateinit var walletManager: Lazy<WalletManager>

    override fun onCreate() {
        super.onCreate()

        if (envManager.isDebugEnv) {
            Timber.plant(Timber.DebugTree())
        }
        RxJavaPlugins.setErrorHandler {
            Timber.e(it)
            if (envManager.isDebugEnv) {
                throw it
            }
        }
        imeManager.init()
        systemSettingManager.loadSystemSetting()
        webAssetsManager.loadWebAssets()

        val processName = getProcessName()
        Timber.v("process name is $processName")
        if (processName.contains(":")) {
            WebView.setDataDirectorySuffix(processName.split(":")[1])
        }

        if (processName.contains(":ime")) {
            LatinSDK.init(this)
            clipboardManager.get().setSolanaAddressListener(object :
                ClipboardManager.SolanaAddressListener {
                override fun onAddressDetected(address: String) {
                    val webView = newWebView(K3IMEService.getInstance())
                    webView.loadUrl(envManager.getWebSearchUrl(address))

                    customViewDispatcher.showCustomView(webView, 0)
                }
            })
            K3IMEService.setInputViewListener(object : InputViewListener {
                override fun onStartInputView() {
                    clipboardManager.get().startMonitoring()
                    clipboardManager.get().searchSolanaWalletAddress()

                    customViewDispatcher.onStartInputView()

                    walletManager.get()
                        .getWalletCount()
                        .observeOn(AndroidSchedulers.mainThread())
                        .autoDispose(ScopeProvider.UNBOUND)
                        .subscribe({
                            Timber.tag("observeWalletCount").v("Wallet count $it")
                            keyboardChargingViewProvider.get().setEnable(it > 0)
                        }, {
                            Timber.tag("observeWalletCount").e(it, "Unexpected error")
                        })
                }

                override fun onFinishInputView() {
                    customViewDispatcher.onFinishInputView()

                    clipboardManager.get().stopMonitoring()
                }
            })
            K3IMEService.setKeyboardViewsProvider(object :
                KeyboardViewsProvider {
                override fun getLeftToolButton(context: Context): View {
                    val view = KeyboardCandidateButton(context)
                    view.setWalletClickListener {
                        val webView = newWebView(context)
                        webView.loadUrl(envManager.getWebWalletUrl())

                        customViewDispatcher.showCustomView(webView)
                    }
                    view.setRecentClickListener {
                        val webView = newWebView(context)
                        webView.loadUrl(envManager.getWebHistoryUrl())

                        customViewDispatcher.showCustomView(webView)
                    }
                    return view
                }

                override fun getRightToolButton(context: Context) = null

                override fun getExtraView(context: Context): View {
                    return keyboardChargingViewProvider.get().newKeyboardChargingView(context)
                }
            })

            K3IMEService.setKeyInterceptor(keyInterceptor.get())
            K3IMEService.setOnCommitTextListener {
                staticsManager.get().recordWordInput(it)
                keyboardChargingViewProvider.get().recordWordInput(it)
            }
        }
    }

    fun showWalletOnKeyboardStart() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            customViewDispatcher.mainHandler.post { showWalletOnKeyboardStart() }
            return
        }

        val webView = newWebView(K3IMEService.getInstance())
        webView.loadUrl(envManager.getWebWalletUrl())
        customViewDispatcher.showCustomView(webView, Int.MAX_VALUE)
    }

    private fun newWebView(context: Context): AppWebView {
        val webView = webViewFactory.get().create(context)
        val dp216 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            216f,
            context.resources.displayMetrics
        ).toInt()
        webView.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp216)
        webView.setBackgroundColor(Color.BLACK)
        return webView
    }

    class KeyboardChargingViewDispatcher @Inject constructor(private val staticsManager: StaticsManager) {

        private var enable = false

        private var count = 0

        private var keyboardChargingView: KeyboardChargingView? = null

        private val mainHandler = Handler(Looper.getMainLooper())

        private val onClickListener = View.OnClickListener {
            keyboardChargingView?.context?.let {
                SplashActivity.start(it)
            }
        }

        fun recordWordInput(word: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mainHandler.post { recordWordInput(word) }
            } else {
                Timber.tag("KeyboardChargingViewDispatcher").v("recording $word")
                if (word.isBlank()) return
                count += word.length
                if (count >= MAX) {
                    count %= MAX
                    keyboardChargingView?.showAnimation()
                    staticsManager.reportNow()
                }
                Timber.tag("KeyboardChargingViewDispatcher").v("set progress $count")
                keyboardChargingView?.setProgress(count)
            }
        }

        fun newKeyboardChargingView(context: Context): KeyboardChargingView {
            val view = KeyboardChargingView(context)
            view.setMax(MAX)
            view.setEnable(enable)
            view.setOnClickListener(onClickListener)
            keyboardChargingView = view

            return keyboardChargingView!!
        }

        fun setEnable(boolean: Boolean) {
            enable = boolean
            keyboardChargingView?.setEnable(enable)
            keyboardChargingView?.setOnClickListener(onClickListener)
        }

        companion object {
            private const val MAX = 100
        }
    }

    class CustomViewDispatcher {

        val mainHandler = Handler(Looper.getMainLooper())
        private var available = false
        private var stashedView: Pair<View, Int>? = null

        fun onStartInputView() {
            available = true

            stashedView?.let {
                showCustomView(it.first)
                stashedView = null
            }
        }

        fun onFinishInputView() {
            available = false
        }

        fun showCustomView(view: View, priority: Int = Int.MIN_VALUE) {
            if (available) {
                mainHandler.post {
                    K3IMEService.showCustomView(view)
                }
            } else {
                val stashedPriority = stashedView?.second ?: Int.MIN_VALUE
                if (priority > stashedPriority) {
                    stashedView?.let {
                        Timber.tag("CustomViewDispatcher")
                            .w("priority ${it.second} is too low, dropping ${it.first}")
                    }
                    stashedView = view to priority
                } else {
                    Timber.tag("CustomViewDispatcher")
                        .w("priority $priority is too low, dropping $view")
                }
            }
        }
    }
}