package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.databinding.WalletRevealMnemonicFragmentBinding
import cc.typex.app.databinding.WalletRevealMnemonicListItemBinding
import cc.typex.app.databinding.WalletRevealPrivateKeyFragmentBinding
import cc.typex.app.databinding.WalletRevealWarningFragmentBinding
import cc.typex.app.db.Wallet
import cc.typex.app.page.wallet.WalletRevealActivity.Companion.PARAMS_ADDRESS
import cc.typex.app.util.UserAuthenticationManager
import cc.typex.app.util.WalletManager
import cc.typex.base.page.DataBindingFragment
import cc.typex.base.page.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WalletRevealActivity : FragmentContainerActivity() {

    @Inject
    lateinit var userAuthenticationManager: UserAuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(PARAMS_ADDRESS) ?: throw IllegalArgumentException()

        if (savedInstanceState == null) {
            showWarning()
        }
    }

    private fun showWarning() {
        addFragment(WalletRevealWarningFragment())
    }

    fun reveal() {
        userAuthenticationManager.authAndroidXBiometric(this, {
            intent.getStringExtra(PARAMS_ADDRESS) ?: throw IllegalArgumentException()
            val revealMnemonic = intent.getBooleanExtra(PARAMS_REVEAL_MNEMONIC, false)
            if (revealMnemonic) {
                replaceFragment(WalletRevealMnemonicFragment())
            } else {
                replaceFragment(WalletRevealPrivateKeyFragment())
            }
        }, {

        })
    }

    companion object {

        const val PARAMS_ADDRESS = "address"
        const val PARAMS_REVEAL_MNEMONIC = "reveal_mnemonic"

        @JvmStatic
        fun start(context: Context, address: String, revealMnemonic: Boolean) {
            val starter = Intent(context, WalletRevealActivity::class.java)
                .putExtra(PARAMS_ADDRESS, address)
                .putExtra(PARAMS_REVEAL_MNEMONIC, revealMnemonic)
            context.startActivity(starter)
        }
    }
}

@AndroidEntryPoint
class WalletRevealWarningFragment : DataBindingFragment<WalletRevealWarningFragmentBinding>() {

    override fun getDataBindingLayoutId() = R.layout.wallet_reveal_warning_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val revealMnemonic =
            activity?.intent?.getBooleanExtra(WalletRevealActivity.PARAMS_REVEAL_MNEMONIC, false)
                ?: false
        binding.revealMnemonic = revealMnemonic
        if (revealMnemonic) {
            binding.tvWarning.text = Html.fromHtml(
                getString(R.string.wallet_reveal_mnemonic_warning),
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            binding.tvWarning.text = Html.fromHtml(
                getString(R.string.wallet_reveal_private_key_warning),
                Html.FROM_HTML_MODE_LEGACY
            )
        }
        binding.tvButton.setOnClickListener {
            (activity as? WalletRevealActivity)?.reveal()
        }
    }
}

@AndroidEntryPoint
class WalletRevealPrivateKeyFragment :
    DataBindingFragment<WalletRevealPrivateKeyFragmentBinding>() {

    private val viewModel by viewModels<WalletRevealViewModel>()

    override fun getDataBindingLayoutId() = R.layout.wallet_reveal_private_key_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etvToolbar.setIconLeftClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.viewModel = viewModel
        binding.tvShowHide.setOnClickListener {
            binding.show = !(binding.show ?: false)
        }

        val address = activity?.intent?.getStringExtra(PARAMS_ADDRESS)
            ?: throw IllegalArgumentException()
        viewModel.loadPrivateKey(address)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe({

            }, {
                Timber.e(it)
            })
    }
}

@AndroidEntryPoint
class WalletRevealMnemonicFragment : DataBindingFragment<WalletRevealMnemonicFragmentBinding>() {

    private val viewModel by viewModels<WalletRevealViewModel>()

    override fun getDataBindingLayoutId() = R.layout.wallet_reveal_mnemonic_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etvToolbar.setIconLeftClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.viewModel = viewModel
        binding.tvShowHide.setOnClickListener {
            binding.show = !(binding.show ?: false)
        }
        val adapter = WalletRevealMnemonicAdapter(requireContext())
        binding.rvMnemonic.adapter = adapter
        viewModel.mnemonic.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        val address = activity?.intent?.getStringExtra(PARAMS_ADDRESS)
            ?: throw IllegalArgumentException()
        viewModel.loadMnemonic(address)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe({

            }, {
                Timber.e(it)
            })
    }
}

@HiltViewModel
class WalletRevealViewModel @Inject constructor(
    private val walletManager: WalletManager,
) : ViewModel() {

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet>
        get() = _wallet

    private val _privateKey = MutableLiveData<String>()
    val privateKey: LiveData<String>
        get() = _privateKey

    private val _mnemonic = MutableLiveData<List<String>>()
    val mnemonic: LiveData<List<String>>
        get() = _mnemonic

    fun loadPrivateKey(address: String): Completable {
        return walletManager.getWalletByAddress(address)
            .doOnSuccess {
                _wallet.postValue(it)
                _privateKey.postValue(walletManager.exportPrivateKeyString(it))
            }
            .ignoreElement()
    }

    fun loadMnemonic(address: String): Completable {
        return walletManager.getWalletByAddress(address)
            .doOnSuccess {
                _wallet.postValue(it)
                _mnemonic.postValue(walletManager.exportMnemonicString(it).split(" "))
            }
            .ignoreElement()
    }
}

class WalletRevealMnemonicViewHolder(
    private val binding: WalletRevealMnemonicListItemBinding
) : ViewHolder(binding.root) {
    fun bind(word: String, index: Int) {
        binding.word = word
        binding.index = index
    }
}

class WalletRevealMnemonicAdapter(context: Context) :
    ListAdapter<String, WalletRevealMnemonicViewHolder>(object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }) {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WalletRevealMnemonicViewHolder {
        val binding = WalletRevealMnemonicListItemBinding.inflate(layoutInflater, parent, false)
        return WalletRevealMnemonicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalletRevealMnemonicViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
}