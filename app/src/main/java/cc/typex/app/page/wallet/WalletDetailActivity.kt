package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.databinding.WalletDetailActivityBinding
import cc.typex.app.db.Wallet
import cc.typex.app.util.ClipboardManager
import cc.typex.app.util.WalletManager
import cc.typex.app.widget.CommonDialog
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WalletDetailActivity : DataBindingActivity<WalletDetailActivityBinding>() {

    private val viewModel: WalletDetailViewModel by viewModels()

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun getDataBindingLayoutId() = R.layout.wallet_detail_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val address = intent.getStringExtra("address")
        if (address.isNullOrEmpty()) {
            Timber.e("Address is empty")
            finish()
            return
        }
        binding.etvToolbar.setIconLeftClickListener {
            finish()
        }
        binding.viewModel = viewModel
        binding.vWalletAddressCopy.setOnClickListener {
            clipboardManager.copyAddress(viewModel.wallet.value?.address.orEmpty())
        }
        binding.vWallerRevealMnemonic.setOnClickListener {
            val wallet = viewModel.wallet.value ?: return@setOnClickListener
            WalletRevealActivity.start(this, wallet.address, true)
        }
        binding.vWallerRevealPrivateKey.setOnClickListener {
            val wallet = viewModel.wallet.value ?: return@setOnClickListener
            WalletRevealActivity.start(this, wallet.address, false)
        }
        binding.vWallerDelete.setOnClickListener {
            CommonDialog.Builder(this)
                .setTitleTextRes(R.string.notice)
                .setTextTextRes(R.string.wallet_detail_delete_confirm)
                .setNegativeButton(R.string.later) {}
                .setPositiveButton(R.string.sure) {
                    delete()
                }
                .build()
                .show()
        }
        viewModel.loadWallet(address)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe({

            }, {
                Timber.e(it)
                finish()
            })
    }

    private fun delete() {
        val address = viewModel.wallet.value?.address ?: return
        viewModel.deleteWallet(address)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe({
                finish()
            }, {
                Timber.e(it)
            })
    }

    companion object {
        fun start(context: Context, address: String) {
            val starter = Intent(context, WalletDetailActivity::class.java)
            starter.putExtra("address", address)
            context.startActivity(starter)
        }
    }
}

@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    private val walletManager: WalletManager,
) : ViewModel() {

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet>
        get() = _wallet

    fun loadWallet(address: String): Completable {
        return walletManager.getWalletByAddress(address)
            .doOnSuccess {
                _wallet.postValue(it)
            }
            .ignoreElement()
    }

    fun deleteWallet(address: String): Completable {
        return walletManager.deleteWalletByAddress(address)
    }
}