package cc.typex.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import cc.typex.app.page.keyboard.KeyboardGuideActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inputMethodManager: InputMethodManager,
) {

    var isThisImeSelected = false
        private set

    private val callbacks = mutableListOf<ImeChangeCallback>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshImeStatus()
            callbacks.forEach {
                it.onImeChanged()
            }
        }
    }

    fun init() {
        refreshImeStatus()
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED))
    }

    private fun refreshImeStatus() {
        val currentImeId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val thisImi = getInputMethodInfoOf(context.packageName, inputMethodManager)
        isThisImeSelected = thisImi?.id == currentImeId
    }

    private fun getInputMethodInfoOf(
        packageName: String,
        imm: InputMethodManager
    ): InputMethodInfo? {
        for (imi in imm.inputMethodList) {
            if (packageName == imi.packageName) {
                return imi
            }
        }
        return null
    }

    fun gotoEnableKeyboard(context: Context) {
        KeyboardGuideActivity.start(context)
    }

    fun addImeChangeCallbacks(callback: ImeChangeCallback) {
        callbacks.add(callback)
    }

    fun removeImeChangeCallbacks(callback: ImeChangeCallback) {
        callbacks.remove(callback)
    }

    interface ImeChangeCallback {
        fun onImeChanged()
    }
}