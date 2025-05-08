package cc.typex.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import cc.typex.app.aidl.IAppAidlInterface
import com.android.inputmethod3.dictionarypack.LocaleUtils
import com.crypto.k3imecore.inputmethod.subtype.SubtypeManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AppRemoteService : Service() {

    @ApplicationContext
    @Inject
    lateinit var app: Context

    private val handler = Handler(Looper.getMainLooper())

    private val binder: IBinder = object : IAppAidlInterface.Stub() {
        @Throws(RemoteException::class)
        override fun switchToLanguage(language: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                handler.post { switchToLanguage(language) }
                return
            }
            Timber.tag("AppRemoteService").v("switchToLanguage $language")
            val enLocale = LocaleUtils.constructLocaleFromString(language)
            val subtype = SubtypeManager.getSubtype(enLocale)
            SubtypeManager.switch2Subtype(subtype)
        }

        @Throws(RemoteException::class)
        override fun getCurrentSubtypeLocale(): String {
            Timber.tag("AppRemoteService").v("getCurrentSubtypeLocale")
            return SubtypeManager.getCurrentSubtype().locale.toString()
        }

        @Throws(RemoteException::class)
        override fun showCustomView(param: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                handler.post { showCustomView(param) }
                return
            }
            Timber.tag("AppRemoteService").v("showCustomView $param")
            when (param) {
                "wallet" -> {
                    (app as TypeXApp).showWalletOnKeyboardStart()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag("AppRemoteService").v("onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.tag("AppRemoteService").v("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.tag("AppRemoteService").v("onBind")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("AppRemoteService").v("onDestroy")
    }
}
