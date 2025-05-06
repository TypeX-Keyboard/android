package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.viewModels
import androidx.core.text.trimmedLength
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.api.ApiService
import cc.typex.app.databinding.WalletAddCompleteFragmentBinding
import cc.typex.app.databinding.WalletAddImportFragmentBinding
import cc.typex.app.databinding.WalletAddProcessingFragmentBinding
import cc.typex.app.db.Wallet
import cc.typex.app.page.main.MainActivity
import cc.typex.app.util.WalletManager
import cc.typex.app.widget.CommonDialog
import cc.typex.base.page.DataBindingFragment
import cc.typex.base.page.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WalletAddActivity : FragmentContainerActivity() {

    private val viewModel by viewModels<WalletAddViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val data = intent.getBooleanExtra(PARAM_KEY_IMPORT, false)
            if (data) {
                addFragment(WalletAddImportFragment())
            } else {
                create()
            }
        }
    }

    private fun create() {
        viewModel.create()
            .doOnSubscribe {
                replaceFragment(WalletAddProcessingFragment(), true)
            }
            .autoDispose(scope())
            .subscribe({
                replaceFragment(WalletAddCompleteFragment())
            }, {
                supportFragmentManager.popBackStack()
            })
    }

    fun import(data: String) {
        viewModel.import(data)
            .doOnSubscribe {
                replaceFragment(WalletAddProcessingFragment(), true)
            }
            .autoDispose(scope())
            .subscribe({
                replaceFragment(WalletAddCompleteFragment())
            }, {
                supportFragmentManager.popBackStack()
            })
    }

    companion object {
        const val PARAM_KEY_IMPORT = "is_import"

        fun create(context: Context) {
            context.startActivity(Intent(context, WalletAddActivity::class.java))
        }

        fun import(context: Context) {
            context.startActivity(Intent(context, WalletAddActivity::class.java).apply {
                putExtra(PARAM_KEY_IMPORT, true)
            })
        }
    }
}

@AndroidEntryPoint
class WalletAddProcessingFragment : DataBindingFragment<WalletAddProcessingFragmentBinding>() {

    override fun getDataBindingLayoutId() = R.layout.wallet_add_processing_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isImport =
            activity?.intent?.getBooleanExtra(WalletAddActivity.PARAM_KEY_IMPORT, false) ?: false
    }
}

@AndroidEntryPoint
class WalletAddImportFragment : DataBindingFragment<WalletAddImportFragmentBinding>() {

    private var viewDestroyedBefore = false

    override fun getDataBindingLayoutId() = R.layout.wallet_add_import_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etvToolbar.setIconLeftClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.tvButton.isEnabled = textValid(binding.etKeys.text)
        binding.etKeys.doAfterTextChanged {
            binding.tvButton.isEnabled = textValid(it)
        }
        binding.tvButton.setOnClickListener {
            val text = binding.etKeys.text.toString()
            (activity as? WalletAddActivity)?.import(text)
        }

        if (savedInstanceState != null || viewDestroyedBefore) {
            CommonDialog.Builder(requireContext())
                .setIconDrawableRes(R.drawable.ic_alert)
                .setTextTextRes(R.string.wallet_import_error)
                .setPositiveButton(R.string.ok, {})
                .build()
                .show()
            viewDestroyedBefore = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDestroyedBefore = true
    }

    private fun textValid(text: Editable?): Boolean {
        return if ((text?.split(" ")?.size ?: 0) >= 12) {
            true
        } else if ((text?.trimmedLength() ?: 0) >= 44) {
            true
        } else {
            false
        }
    }
}

@AndroidEntryPoint
class WalletAddCompleteFragment : DataBindingFragment<WalletAddCompleteFragmentBinding>() {

    private val viewModel by activityViewModels<WalletAddViewModel>()

    override fun getDataBindingLayoutId() = R.layout.wallet_add_complete_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isImport =
            activity?.intent?.getBooleanExtra(WalletAddActivity.PARAM_KEY_IMPORT, false) ?: false
        binding.viewModel = viewModel
        binding.isImport = isImport

        if (isImport) {
            Completable
                .complete()
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribe({
                    MainActivity.start(requireContext())
                }, {
                    MainActivity.start(requireContext())
                })
        } else {
            binding.tvBackup.setOnClickListener {
                MainActivity.start(requireContext())
                val address = viewModel.wallet.value?.address ?: return@setOnClickListener
                WalletRevealActivity.start(requireContext(), address, true)
            }
            binding.tvSkip.setOnClickListener {
                MainActivity.start(requireContext())
            }
        }
    }
}

@HiltViewModel
class WalletAddViewModel @Inject constructor(
    private val walletManager: WalletManager,
    private val apiService: ApiService,
) : ViewModel() {

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet>
        get() = _wallet

    fun create(): Completable {
        val startTime = System.currentTimeMillis()
        return walletManager.createWallet()
            .flatMap { wallet ->
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = DELAY_MILLIS - elapsedTime
                if (remainingTime > 0) {
                    Single.timer(remainingTime, TimeUnit.MILLISECONDS)
                        .map { wallet }
                } else {
                    Single.just(wallet)
                }
            }
            .doOnSuccess {
                _wallet.postValue(it)
            }
            .flatMapCompletable {
                apiService.createBot(mapOf("address" to it.address))
            }
    }

    fun import(data: String): Completable {
        val startTime = System.currentTimeMillis()
        return if (data.trim().contains(" ")) {
            walletManager.importWallet(data)
        } else {
            walletManager.importSolanaPrivateKey(data)
        }
            .flatMap { wallet ->
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = DELAY_MILLIS - elapsedTime
                if (remainingTime > 0) {
                    Single.timer(remainingTime, TimeUnit.MILLISECONDS)
                        .map { wallet }
                } else {
                    Single.just(wallet)
                }
            }
            .doOnSuccess {
                _wallet.postValue(it)
            }
            .flatMapCompletable {
                apiService.createBot(mapOf("address" to it.address))
            }
    }

    companion object {
        private const val DELAY_MILLIS = 3000L
    }
}
