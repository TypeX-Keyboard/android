package cc.typex.app.util

import android.content.Context
import cc.typex.app.page.auth.K3IMEAuthActivity
import cc.typex.app.web.AppJsBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class K3IMEAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var triggerTimeStamp = 0L
    private var unsignedTransaction: AppJsBridge.JsRequestMessage? = null
    private var readyToSignTransaction: AppJsBridge.JsRequestMessage? = null


    fun requestAuth(jsRequestMessage: AppJsBridge.JsRequestMessage): Boolean {
        triggerTimeStamp = System.currentTimeMillis()
        unsignedTransaction = jsRequestMessage

        if (NEED_AUTH) {
            K3IMEAuthActivity.start(context, jsRequestMessage.callbackId)
            return true
        } else {
            markAuthStatus(jsRequestMessage.callbackId, true)
            return false
        }
    }

    fun markAuthStatus(callbackId: String, success: Boolean) {
        val messageFinal = unsignedTransaction ?: return
        if (messageFinal.callbackId != callbackId) {
            unsignedTransaction = null
            return
        }
        if (System.currentTimeMillis() - triggerTimeStamp > 10000L) {
            unsignedTransaction = null
            return
        }
        if (!success) {
            unsignedTransaction = null
            return
        }
        readyToSignTransaction = messageFinal
        unsignedTransaction = null
        triggerTimeStamp = System.currentTimeMillis()
    }

    fun exactReadyToSignTransaction(): AppJsBridge.JsRequestMessage? {
        val messageFinal = if (System.currentTimeMillis() - triggerTimeStamp > 5000L) {
            null
        } else {
            readyToSignTransaction
        }
        readyToSignTransaction = null
        unsignedTransaction = null
        triggerTimeStamp = 0
        return messageFinal
    }

    companion object {
        private const val NEED_AUTH = false
    }
}