package cc.typex.app.page.setting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import cc.typex.app.AppRemoteService
import cc.typex.app.R
import cc.typex.app.aidl.IAppAidlInterface
import cc.typex.app.databinding.LanguageActivityBinding
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LanguageActivity : DataBindingActivity<LanguageActivityBinding>() {

    override fun getDataBindingLayoutId() = R.layout.language_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindRemoteService()

        binding.etvToolbar.setIconLeftClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvEnglish.setOnClickListener {
            remoteService?.switchToLanguage("en_US")
            unselectAll()
            select()
        }
        binding.tvChinese.setOnClickListener {
            remoteService?.switchToLanguage("zh_CN")
            unselectAll()
            select()
        }
        binding.tvJapanese.setOnClickListener {
            remoteService?.switchToLanguage("ja_JP")
            unselectAll()
            select()
        }
    }

    private fun onServiceConnected() {
        unselectAll()
        select()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindRemoteService()
    }

    private fun select() {
        when (remoteService?.currentSubtypeLocale) {
            "en_US" -> {
                binding.tvEnglish.isSelected = true
            }

            "ja_JP" -> {
                binding.tvJapanese.isSelected = true
            }

            "zh_CN" -> {
                binding.tvChinese.isSelected = true
            }
        }
    }

    private fun unselectAll() {
        binding.tvEnglish.isSelected = false
        binding.tvChinese.isSelected = false
        binding.tvJapanese.isSelected = false
    }

    private var remoteService: IAppAidlInterface? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.v("onServiceConnected")
            remoteService = IAppAidlInterface.Stub.asInterface(service)
            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.v("onServiceDisconnected")
            remoteService = null
        }
    }

    private fun bindRemoteService() {
        Timber.v("bindRemoteService")
        val intent = Intent(this, AppRemoteService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindRemoteService() {
        Timber.v("unbindRemoteService")
        unbindService(serviceConnection)
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, LanguageActivity::class.java)
            context.startActivity(starter)
        }
    }
}