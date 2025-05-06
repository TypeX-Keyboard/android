package cc.typex.app.page.wallet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.api.ApiService
import cc.typex.app.api.entity.TransactionDetail
import cc.typex.app.databinding.WalletTransactionActivityBinding
import cc.typex.app.util.ClipboardManager
import cc.typex.app.util.shortAddress
import cc.typex.app.util.toLocalTimestamp
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WalletTransactionActivity : DataBindingActivity<WalletTransactionActivityBinding>() {

    private val viewModel by viewModels<WalletTransactionViewModel>()

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun getDataBindingLayoutId() = R.layout.wallet_transaction_activity

    override fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        binding.root.updatePadding(top = statusBarHeight)
        binding.clScrollContent.updatePadding(bottom = navigationBarHeight)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.etvToolbar.setIconLeftClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val address = intent.getStringExtra("wallet")
        val hash = intent.getStringExtra("tx_hash")
        val action = intent.getIntExtra("action", 0)
        if (address.isNullOrEmpty() || hash.isNullOrEmpty() || action == 0) {
            finish()
            return
        }
        binding.etvToolbar.setTitle(
            getString(
                when (action) {
                    1 -> R.string.wallet_history_operation_receive
                    2 -> R.string.wallet_history_operation_send
                    3 -> R.string.wallet_history_operation_buy
                    4 -> R.string.wallet_history_operation_Sell
                    else -> R.string.wallet_history_status_fail
                }
            )
        )

        binding.viewModel = viewModel
        load(hash, address)
    }

    private fun load(hash: String, address: String) {
        viewModel.loadTransactionDetail(hash, address)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doOnTerminate {
                hideLoading()
                hideError()
            }
            .doOnError { showError { load(hash, address) } }
            .autoDispose(scope())
            .subscribe({
                binding.vCopyHash.setOnClickListener {
                    clipboardManager.setClipboardText(
                        "transaction hash",
                        viewModel.transactionDetail.value?.txhash ?: ""
                    )
                }
                binding.vCopyFrom.setOnClickListener {
                    clipboardManager.setClipboardText(
                        "wallet address",
                        viewModel.transferFromAddress.value ?: ""
                    )
                }
                binding.vCopyTo.setOnClickListener {
                    clipboardManager.setClipboardText(
                        "wallet address",
                        viewModel.transferToAddress.value ?: ""
                    )
                }
            }, {
                Timber.e(it)
            })
    }

    companion object {
        @JvmStatic
        fun start(context: Context, wallet: String, txHash: String, action: Int) {
            val starter = Intent(context, WalletTransactionActivity::class.java)
                .putExtra("wallet", wallet)
                .putExtra("tx_hash", txHash)
                .putExtra("action", action)
            context.startActivity(starter)
        }
    }
}

@HiltViewModel
class WalletTransactionViewModel @Inject constructor(
    private val application: Application,
    private val apiService: ApiService,
) : AndroidViewModel(application) {

    private var address: String? = null
    private val _transactionDetail = MutableLiveData<TransactionDetail>()
    val transactionDetail: LiveData<TransactionDetail>
        get() = _transactionDetail

    val operationTokenLogo = transactionDetail.map {
        when (it.action) {
            3 -> it.outTokenLogoUrl
            4 -> it.inTokenLogoUrl
            else -> it.tokenLogoUrl
        }
    }
    val operationTokenSymbol = transactionDetail.map {
        when (it.action) {
            3 -> it.symbolOut
            4 -> it.symbolIn
            else -> it.symbol
        }
    }

    val timestamp = transactionDetail.map { it.txTime?.toLocalTimestamp("yyyy.MM.dd HH:mm") }

    val txStatus = transactionDetail.map {
        application.getString(
            when (it.txStatus) {
                "success" -> R.string.wallet_history_status_complete
                "pending" -> R.string.wallet_history_status_pending
                else -> R.string.wallet_history_status_fail
            }
        )
    }
    val transactionFailed = transactionDetail.map {
        (it.txStatus ?: "fail") == "fail"
    }
    val txHashShort = transactionDetail.map { it.txhash?.shortAddress() }

    val transferFromAddress = transactionDetail.map {
        it.tokenTransferDetails?.firstOrNull()?.from
    }

    val transferToAddress = transactionDetail.map {
        when (it.action) {
            1 -> address
            2 -> it.tokenTransferDetails?.firstOrNull()?.to
            else -> null
        }
    }

    fun loadTransactionDetail(txHash: String, wallet: String): Completable {
        address = wallet
        return apiService.getWalletTransaction(txHash, wallet)
            .doOnSuccess {
                _transactionDetail.postValue(it)
            }
            .ignoreElement()
    }

}

