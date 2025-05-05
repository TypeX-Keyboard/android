package cc.typex.app.page.auth

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cc.typex.app.util.K3IMEAuthManager
import cc.typex.app.util.UserAuthenticationManager
import cc.typex.base.page.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class K3IMEAuthActivity : FragmentContainerActivity() {

    @Inject
    lateinit var inputMethodManager: InputMethodManager

    @Inject
    lateinit var userAuthenticationManager: UserAuthenticationManager

    @Inject
    lateinit var k3IMEAuthManager: K3IMEAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val attr = window.attributes
        attr.dimAmount = 0f
        attr.format = PixelFormat.RGBA_8888
        attr.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        window.attributes = attr

        val callbackId = intent.getStringExtra("callback_id")
        if (callbackId.isNullOrEmpty()) {
            finishAndRemoveTask()
            return
        }

        userAuthenticationManager.authAndroidXBiometric(this, {
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
            k3IMEAuthManager.markAuthStatus(callbackId, true)
            finishAndRemoveTask()
            inputMethodManager.showSoftInput(binding.root, InputMethodManager.SHOW_FORCED)
        }, {
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
            k3IMEAuthManager.markAuthStatus(callbackId, false)
            finishAndRemoveTask()
        })
    }

    companion object {
        @JvmStatic
        fun start(context: Context, callbackId: String) {
            val starter = Intent(context, K3IMEAuthActivity::class.java)
            starter.putExtra("callback_id", callbackId)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}