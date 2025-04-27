package cc.typex.app.page.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.databinding.KeyboardGuideActivityBinding
import cc.typex.app.page.main.MainActivity
import cc.typex.app.page.wallet.WalletWelcomeActivity
import cc.typex.app.util.UncachedInputMethodManagerUtils
import cc.typex.app.util.WalletManager
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class KeyboardGuideActivity : DataBindingActivity<KeyboardGuideActivityBinding>() {

    @Inject
    lateinit var inputMethodManager: InputMethodManager

    @Inject
    lateinit var walletManager: WalletManager

    override fun getDataBindingLayoutId() = R.layout.keyboard_guide_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.currentStep = STEP_1
    }

    private fun invokeLanguageAndInputSettings() {
        val intent = Intent()
        intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        this.startActivity(intent)
    }

    private fun invokeInputMethodPicker() {
        inputMethodManager.showInputMethodPicker()
    }

    override fun checkImeOnResume() = false

    override fun onResume() {
        super.onResume()
        matchCurrentStep()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            return
        }
        matchCurrentStep()
    }

    private fun matchCurrentStep() {
        val imeEnabled = UncachedInputMethodManagerUtils.isThisImeEnabled(this, inputMethodManager)
        val imeSelected = UncachedInputMethodManagerUtils.isThisImeCurrent(this, inputMethodManager)
        if (!imeEnabled) {
            binding.currentStep = STEP_1
            binding.tvButton.setOnClickListener {
                invokeLanguageAndInputSettings()
            }
        } else if (!imeSelected) {
            binding.currentStep = STEP_2
            binding.tvButton.setOnClickListener {
                invokeInputMethodPicker()
            }
        } else {
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
    }

    data class GuideStep(
        @StringRes val title: Int,
        @StringRes val description: Int,
        val lottieAssetName: String,
        @StringRes val button: Int,
    )

    companion object {
        private val STEP_1 = GuideStep(
            R.string.keyboard_guide_title,
            R.string.keyboard_guide_description,
            "anim/keyboard_guide_step_1.json",
            R.string.keyboard_guide_button_step_1,
        )
        private val STEP_2 = GuideStep(
            R.string.keyboard_guide_title,
            R.string.keyboard_guide_description,
            "anim/keyboard_guide_step_2.json",
            R.string.keyboard_guide_button_step_2,
        )

        fun start(context: Context) {
            val starter = Intent(context, KeyboardGuideActivity::class.java)
            context.startActivity(starter)
        }
    }
}