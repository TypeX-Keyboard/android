package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cc.typex.app.R
import cc.typex.app.databinding.WalletWelcomeActivityBinding
import cc.typex.app.page.setting.SettingActivity
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletWelcomeActivity : DataBindingActivity<WalletWelcomeActivityBinding>() {

    override fun getDataBindingLayoutId() = R.layout.wallet_welcome_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.vSetting.setOnClickListener {
            SettingActivity.start(this, false)
        }
        binding.tvCreate.setOnClickListener {
            WalletAddActivity.create(this)
        }
        binding.tvImport.setOnClickListener {
            WalletAddActivity.import(this)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, WalletWelcomeActivity::class.java))
        }
    }
}