package cc.typex.app.web

import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebMessage
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import cc.typex.app.api.ApiService
import cc.typex.app.api.entity.JsBridgeKey
import cc.typex.app.db.EntityDao
import cc.typex.app.db.TokenHistory
import cc.typex.app.db.TokenHistoryDao
import cc.typex.app.util.AppKeyInterceptor
import cc.typex.app.util.ClipboardManager
import cc.typex.app.util.DeviceInfo
import cc.typex.app.util.K3IMEAuthManager
import cc.typex.app.util.UriRouter
import cc.typex.app.util.WalletManager
import com.crypto.future.keyboard3.K3IMEService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AppJsBridge @Inject constructor(
    private val walletManager: WalletManager,
    private val tokenHistoryDao: TokenHistoryDao,
    private val clipboardManager: ClipboardManager,
    private val uriRouter: UriRouter,
    private val deviceInfo: DeviceInfo,
    private val keyInterceptor: AppKeyInterceptor,
    private val k3IMEAuthManager: K3IMEAuthManager,
) : View.OnAttachStateChangeListener {

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var entityDao: EntityDao

    @Inject
    lateinit var apiService: ApiService

    private val realJsBridge = AndroidBridge()
    private var webView: AppWebView? = null
    private var disposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()
    private val jsBridgeMessageSubject = PublishSubject.create<String>()

    private fun getUserInfo(callbackId: String) {
        compositeDisposable.add(
            Single
                .fromCallable {
                    mapOf(
                        "hello" to "world"
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId, it)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun getBaseInfo(callbackId: String) {
        compositeDisposable.add(
            Single
                .fromCallable {
                    mapOf(
                        "status_bar_height" to deviceInfo.statusBarHeight,
                        "navigation_bar_height" to deviceInfo.navigationBarHeight,
                        "device_id" to deviceInfo.deviceId,
                        "app_version" to deviceInfo.appVersion,
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId, it)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun getDefaultWallet(callbackId: String) {
        compositeDisposable.add(
            walletManager.getDefaultWallet()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId, mapOf("default_wallet" to it.address))
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun setDefaultWallet(callbackId: String, params: Map<String, Any>) {
        compositeDisposable.add(
            Single
                .fromCallable { params["default_wallet"] as String }
                .flatMapCompletable {
                    walletManager.setDefaultWallet(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun getWalletList(callbackId: String) {
        compositeDisposable.add(
            walletManager.getWalletList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(
                        callbackId,
                        mapOf("wallet_list" to it.map { it.address })
                    )
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun commitTextNative(callbackId: String, params: Map<String, Any>) {
        compositeDisposable.add(
            Single
                .fromCallable { params["text"] as String }
                .doOnSuccess {
                    K3IMEService.getInstance().currentInputConnection.commitText(it, 1)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun openNative(callbackId: String, params: Map<String, Any>) {
        val uri = params["uri"] as String
        val webViewFinal = webView
        if (webViewFinal == null) {
            invokeJsBridgeCallback(callbackId, error = "Failed to open $uri")
            return
        }
        val result = uriRouter.route(webViewFinal.context, uri)
        if (result) {
            invokeJsBridgeCallback(callbackId)
        } else {
            invokeJsBridgeCallback(callbackId, error = "Failed to open $uri")
        }
    }

    private fun startWebInput(callbackId: String) {
        compositeDisposable.add(
            Completable
                .fromAction { keyInterceptor.startIntercept(this) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun stopWebInput(callbackId: String) {
        compositeDisposable.add(
            Completable
                .fromAction { keyInterceptor.stopIntercept() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    fun commitTextWeb(keyCode: Int) {
        val data = mapOf("key_code" to keyCode)
        callJsBridgeMethod("commitTextWeb", data)
    }

    private fun getClipboardText(callbackId: String) {
        val text = clipboardManager.getClipboardText() ?: ""
        invokeJsBridgeCallback(
            callbackId, mapOf(
                "clipboard_text" to text
            )
        )
    }

    private fun setClipboardText(callbackId: String, params: Map<String, Any>) {
        val text = params["clipboard_text"] as String
        clipboardManager.setClipboardText("text", text)
        invokeJsBridgeCallback(callbackId)
    }

    private fun signTransaction(callbackId: String, params: Map<String, Any>) {
        compositeDisposable.add(
            Single
                .fromCallable { params["unsigned_tx"] as String }
                .flatMap {
                    walletManager.signTransaction(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId, mapOf("signed_tx" to it))
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun getTokenHistoryList(callbackId: String) {
        compositeDisposable.add(
            tokenHistoryDao.getAll()
                .map { list -> list.map { it.address } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId, mapOf("token_history_list" to it))
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun recordTokenView(callbackId: String, params: Map<String, Any>) {
        compositeDisposable.add(
            Single
                .fromCallable {
                    val tokenAddress = params["token_address"] as String
                    TokenHistory(tokenAddress, System.currentTimeMillis())
                }
                .flatMapCompletable {
                    tokenHistoryDao.insert(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invokeJsBridgeCallback(callbackId)
                }, {
                    invokeJsBridgeCallback(callbackId, error = it.message)
                })
        )
    }

    private fun setWebViewHeight(callbackId: String, params: Map<String, Any>) {
        try {
            val newHeight = params["height"] as Double
            val isCover = params["isCover"] as? Boolean? ?: false
            val webViewFinal = webView!!
            val newHeightPixel = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                newHeight.toFloat(),
                webViewFinal.context.resources.displayMetrics
            ).toInt()
            K3IMEService.setCustomViewHeight(newHeightPixel, isCover)
            invokeJsBridgeCallback(callbackId)
        } catch (e: Exception) {
            invokeJsBridgeCallback(callbackId, error = e.message)
        }
    }

    private fun closeWebView(callbackId: String) {
        try {
            val context = webView!!.context!!
            if (context is AppCompatActivity) {
                context.onBackPressedDispatcher.onBackPressed()
                invokeJsBridgeCallback(callbackId)
                return
            }
            // Direct dependency on K3IMEService, which is not good
            if (context is K3IMEService) {
                K3IMEService.hideCustomView()
                invokeJsBridgeCallback(callbackId)
                return
            }
        } catch (e: Exception) {
            invokeJsBridgeCallback(callbackId, error = e.message)
        }
    }

    @MainThread
    private fun invokeJsBridgeCallback(
        method: String,
        params: Map<String, Any>? = null,
        error: String? = null
    ) {
        val message = JsResponseMessage.obtain(method, params, error)
        val json = gson.toJson(message)
        Timber.tag("AppJsBridge").v("window.postMessage($json)")
        webView?.postWebMessage(WebMessage(json), "*".toUri())
    }

    @MainThread
    fun callJsBridgeMethod(method: String, params: Map<String, Any>) {
        val message = JsRequestMessage.obtain(method, params)
        val json = gson.toJson(message)
        Timber.tag("AppJsBridge").v("window.handleNativeMessage($json)")
        webView?.evaluateJavascript(
            "window.handleNativeMessage('$json')",
            null
        )
    }

    fun bindToWebView(webView: AppWebView) {
        this.webView = webView
        webView.addJavascriptInterface(realJsBridge, "androidBridge")
        webView.addOnAttachStateChangeListener(this)
        if (webView.isAttachedToWindow) {
            this.onViewAttachedToWindow(webView)
        }
    }

    private fun dispatchMessage(message: JsRequestMessage) {
        Timber.tag("AppJsBridge").v("dispatchMessage: $message")
        when (message.method) {
            "getUserInfo" -> {
                getUserInfo(message.callbackId)
            }

            "getBaseInfo" -> {
                getBaseInfo(message.callbackId)
            }

            "getDefaultWallet" -> {
                getDefaultWallet(message.callbackId)
            }

            "setDefaultWallet" -> {
                setDefaultWallet(message.callbackId, message.params)
            }

            "getWalletList" -> {
                getWalletList(message.callbackId)
            }

            "commitTextNative" -> {
                commitTextNative(message.callbackId, message.params)
            }

            "openNative" -> {
                openNative(message.callbackId, message.params)
            }

            "startWebInput" -> {
                startWebInput(message.callbackId)
            }

            "stopWebInput" -> {
                stopWebInput(message.callbackId)
            }

            "getClipboardText" -> {
                getClipboardText(message.callbackId)
            }

            "setClipboardText" -> {
                setClipboardText(message.callbackId, message.params)
            }

            "signTransaction" -> {
                if (!k3IMEAuthManager.requestAuth(message)) {
                    checkUnsignedTransaction()
                } else {
                    K3IMEService.setHoldCustomViewOnFinish(true)
                }
            }

            "getTokenHistoryList" -> {
                getTokenHistoryList(message.callbackId)
            }

            "recordTokenView" -> {
                recordTokenView(message.callbackId, message.params)
            }

            "setWebViewHeight" -> {
                setWebViewHeight(message.callbackId, message.params)
            }

            "closeWebview" -> {
                closeWebView(message.callbackId)
            }
        }
    }

    fun checkUnsignedTransaction() {
        K3IMEService.setHoldCustomViewOnFinish(false)
        val jsMessage = k3IMEAuthManager.exactReadyToSignTransaction() ?: return
        signTransaction(jsMessage.callbackId, jsMessage.params)
    }

    private fun subscribeJsBridgeMessage() {
        disposable?.dispose()
        disposable = jsBridgeMessageSubject
            .observeOn(Schedulers.io())
            .map {
                val message = gson.fromJson(it, JsRequestMessage::class.java)
                if (message.type != "request") {
                    return@map JsRequestMessage.obtainError()
                }
                if (!message.isEncrypted()) {
                    return@map message
                }
                try {
                    decryptMessage(message)
                } catch (e: Exception) {
                    try {
                        entityDao.setJsBridgeKeyExpire(0)
                        decryptMessage(message)
                    } catch (e: Exception) {
                        Timber.tag("AppJsBridge").e(e)
                        JsRequestMessage.obtainError(message.callbackId)
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it.isError() && it.callbackId.isNotEmpty()) {
                    invokeJsBridgeCallback(it.callbackId, null, "error")
                    return@doOnNext
                }
                dispatchMessage(it)
            }
            .subscribe()
    }

    private fun decryptMessage(message: JsRequestMessage): JsRequestMessage {
        val jsBridgeKey = loadSecretKeys()

        val timestamp = (message.params["timestamp"] as Double).toLong()
        val nonce = message.params["nonce"] as String
        val signHex = message.params["sign"] as String

        val privateKeyBase64 = jsBridgeKey.key
        val privateKeyRaw = Base64.decode(privateKeyBase64, Base64.DEFAULT)
        val privateKey = KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(privateKeyRaw))
        val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

        val encryptedDataBase64Chunks = (message.params["encryptedData"] as String).split(":::")
        var dataRaw = byteArrayOf()
        for (chunk in encryptedDataBase64Chunks) {
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val chunkRaw = rsaCipher.doFinal(Base64.decode(chunk, Base64.DEFAULT))
            dataRaw += chunkRaw
        }
        val dataJson = dataRaw.decodeToString()

        val hmacKeyHex = jsBridgeKey.signKey

        @OptIn(ExperimentalStdlibApi::class)
        val hmacKeyRaw = hmacKeyHex.hexToByteArray()
        val signContent = "$dataJson$timestamp$nonce$hmacKeyHex"

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(hmacKeyRaw, "HmacSHA256"))
        @OptIn(ExperimentalStdlibApi::class)
        val mySignHex = mac.doFinal(signContent.toByteArray()).toHexString()
        if (mySignHex != signHex) {
            return JsRequestMessage.obtainError(message.callbackId)
        }

        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return message.copy(params = gson.fromJson(dataJson, mapType))
    }

    private fun loadSecretKeys(): JsBridgeKey {
        val expireTime = entityDao.getJsBridgeKeyExpire() ?: 0L
        if (expireTime < System.currentTimeMillis() / 1000L) {
            val keyResp = apiService.getJsBridgeKey(mapOf("from" to "native")).blockingGet()

            // 后台返回的 key 为 Base64(pem_private_key)
            // 即：给 PEM 格式的私钥又做了一次 Base64，所以这里多了一次 Base64.decode
            val keyPEM = Base64.decode(keyResp.key, Base64.DEFAULT)
            val keyBase64 = keyPEM
                .decodeToString()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim()

            entityDao.setJsBridgeKey(keyBase64)

            // 后台返回的 signKey 为 hex string
            entityDao.setJsBridgeSignKey(keyResp.signKey)
            // 后台返回的 expire 为 unix timestamp
            entityDao.setJsBridgeKeyExpire(keyResp.expire)
        }
        return JsBridgeKey(
            key = entityDao.getJsBridgeKey()!!,
            signKey = entityDao.getJsBridgeSignKey()!!,
            expire = entityDao.getJsBridgeKeyExpire()!!
        )
    }

    override fun onViewAttachedToWindow(v: View) {
        Timber.v("onViewAttachedToWindow")
        subscribeJsBridgeMessage()
    }

    override fun onViewDetachedFromWindow(v: View) {
        Timber.v("onViewDetachedFromWindow")
        disposable?.dispose()
        keyInterceptor.stopIntercept()
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun postMessage(message: String) {
            Timber.tag("AppJsBridge").v("androidBridge.postMessage: $message")
            jsBridgeMessageSubject.onNext(message)
        }
    }

    @Keep
    data class JsRequestMessage(
        val type: String,
        val callbackId: String,
        val method: String,
        val params: Map<String, Any>,
    ) {

        fun isEncrypted(): Boolean {
            return !(params["sign"] as? String).isNullOrEmpty()
        }

        fun isError(): Boolean {
            return type == "error"
        }

        companion object {
            fun obtain(method: String, params: Map<String, Any>): JsRequestMessage {
                return JsRequestMessage(
                    type = "request",
                    callbackId = "",
                    method = method,
                    params = params
                )
            }

            fun obtainError(callbackId: String = ""): JsRequestMessage {
                return JsRequestMessage(
                    type = "error",
                    callbackId = callbackId,
                    method = "",
                    params = emptyMap()
                )
            }
        }
    }

    @Keep
    data class JsResponseMessage(
        val type: String,
        val callbackId: String,
        val data: Map<String, Any>?,
        val error: String?,
    ) {
        companion object {
            fun obtain(
                callbackId: String,
                data: Map<String, Any>?,
                error: String?
            ): JsResponseMessage {
                return JsResponseMessage(
                    type = "response",
                    callbackId = callbackId,
                    data = data,
                    error = error
                )
            }
        }
    }
}