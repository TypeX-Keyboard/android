package cc.typex.app.util

import cc.typex.app.web.AppJsBridge
import com.android.inputmethod3.latin.Constants
import com.crypto.future.keyboard3.KeyInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppKeyInterceptor @Inject constructor() :
    KeyInterceptor {

    private var intercept: Boolean = false
    private var jsBridge: AppJsBridge? = null

    override fun intercept(keyCode: Int): Boolean {
        if (intercept) {
            if (keyCode in 32..127) {
                this.jsBridge?.commitTextWeb(keyCode)
            } else if (keyCode == Constants.CODE_DELETE) {
                this.jsBridge?.commitTextWeb(127)
            }
            return true
        } else {
            return false
        }
    }

    fun startIntercept(jsBridge: AppJsBridge) {
        this.jsBridge = jsBridge
        this.intercept = true
    }

    fun stopIntercept() {
        this.jsBridge = null
        this.intercept = false
    }
}