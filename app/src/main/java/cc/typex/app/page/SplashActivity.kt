package cc.typex.app.page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.databinding.SplashActivityBinding
import cc.typex.app.page.main.MainActivity
import cc.typex.app.page.wallet.WalletWelcomeActivity
import cc.typex.app.util.SystemSettingManager
import cc.typex.app.util.WalletManager
import cc.typex.app.widget.UpgradeDialog
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : DataBindingActivity<SplashActivityBinding>() {

    @Inject
    lateinit var walletManager: WalletManager

    @Inject
    lateinit var systemSettingManager: SystemSettingManager

    override fun getDataBindingLayoutId() = R.layout.splash_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (systemSettingManager.forceUpdate) {
            UpgradeDialog(this)
                .setVersionName(systemSettingManager.latestVersion)
                .setUpdateInfo(systemSettingManager.updateInfo)
                .setOnClickSkipListener {
                    finish()
                }
                .setOnClickConfirmListener {
                    UpgradeDialog.gotoMarket(this)
                    finish()
                }
                .show()
            return
        }

        val enabled = imeManager.isThisImeSelected
        if (!enabled) {
            imeManager.gotoEnableKeyboard(this)
            finish()
            return
        }

        walletManager
            .getWalletList()
            .autoDispose(scope())
            .subscribe({
                if (it.isEmpty()) {
                    WalletWelcomeActivity.start(this)
                    finish()
                } else {
                    MainActivity.start(this)
                    finish()
                }
            }, {
                Timber.e(it)
            })
    }

    override fun checkImeOnResume() = false

    companion object {

        fun start(context: Context) {
            val starter = Intent(context, SplashActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}