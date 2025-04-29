package cc.typex.app.page.setting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import cc.typex.app.R
import cc.typex.app.databinding.SettingActivityBinding
import cc.typex.app.util.EnvManager
import cc.typex.app.util.SystemSettingManager
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : DataBindingActivity<SettingActivityBinding>() {

    @Inject
    lateinit var systemSettingManager: SystemSettingManager

    @Inject
    lateinit var envManager: EnvManager

    override fun getDataBindingLayoutId() = R.layout.setting_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.etvToolbar.setIconLeftClickListener {
            finish()
        }
        binding.llLanguage.setOnClickListener {
            LanguageActivity.start(this)
        }
        binding.llTou.setOnClickListener {
            openUrl(envManager.getTermsOfServiceUrl())
        }
        binding.llPp.setOnClickListener {
            openUrl(envManager.getPrivacyPolicyUrl())
        }
        if (systemSettingManager.hasNewVersion) {
            binding.tvNewVersion.text = ""
            binding.llAbout.setOnClickListener {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.vNewVersion.visibility = View.GONE
            binding.vNewVersionLabel.visibility = View.GONE
            binding.tvNewVersion.visibility = View.GONE
        }
    }

    private fun openUrl(url: String) {
        try {
            val intentBuilder = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(Color.BLACK)
                        .build()
                )
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setUrlBarHidingEnabled(false)
                .setInstantAppsEnabled(false)
                .setShowTitle(true)
            val icon = BitmapFactory.decodeResource(this.resources, R.drawable.ic_toolbar_back)
            if (icon != null) {
                intentBuilder.setCloseButtonIcon(icon)
            }
            val intent = intentBuilder.build()
            intent.launchUrl(this, Uri.parse(url))
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(url))
                this.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context, newTask: Boolean = false) {
            val starter = Intent(context, SettingActivity::class.java)
            if (newTask) starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}