package cc.typex.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var listener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var solanaAddressListener: SolanaAddressListener? = null

    private val solanaAddressPattern = Regex("[1-9A-HJ-NP-Za-km-z]{32,44}")

    fun startMonitoring() {
        if (listener == null) {
            listener = ClipboardManager.OnPrimaryClipChangedListener {
                searchSolanaWalletAddress()
            }
            clipboard.addPrimaryClipChangedListener(listener)
        }
    }

    private var lastNotifyId: Long? = null

    fun searchSolanaWalletAddress() {
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val timestamp = clip.description.timestamp
            if (timestamp <= (lastNotifyId ?: 0)) {
                return
            }
            val copiedText = clip.getItemAt(0).text?.toString()
            if (!copiedText.isNullOrEmpty()) {
                val walletAddress = findSolanaWalletAddress(copiedText)
                if (walletAddress != null && !walletAddress.contains("So11111111111111111111111111111111111111112")) {
                    solanaAddressListener?.onAddressDetected(walletAddress)
                    lastNotifyId = timestamp
                }
            }
        }
    }

    fun stopMonitoring() {
        clipboard.removePrimaryClipChangedListener(listener)
        listener = null
    }

    fun getClipboardText(): String? {
        clipboard.primaryClip?.getItemAt(0)
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val copiedText = clip.getItemAt(0).text?.toString()
            return copiedText
        } else {
            return null
        }
    }

    fun setClipboardText(label: String, text: String) {
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun copyAddress(address: String) {
        setClipboardText("wallet_address", address)
    }

    fun setSolanaAddressListener(listener: SolanaAddressListener) {
        solanaAddressListener = listener
    }

    // 移除回调监听器
    fun removeSolanaAddressListener() {
        solanaAddressListener = null
    }

    // 接口用于回调监听器
    interface SolanaAddressListener {
        fun onAddressDetected(address: String)
    }

    private fun findSolanaWalletAddress(text: String): String? {
        val matchResult = solanaAddressPattern.find(text)
        return matchResult?.value
    }
}