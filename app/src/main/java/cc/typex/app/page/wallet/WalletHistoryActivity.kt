package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.R
import cc.typex.app.api.ApiService
import cc.typex.app.api.entity.ListLoadingStatus
import cc.typex.app.api.entity.Transaction
import cc.typex.app.databinding.WalletHistoryActivityBinding
import cc.typex.app.databinding.WalletHistoryListItemBinding
import cc.typex.app.databinding.WalletHistoryListItemDateBinding
import cc.typex.app.util.WalletManager
import cc.typex.app.util.shortAddress
import cc.typex.app.util.toLocalTimestamp
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WalletHistoryActivity : DataBindingActivity<WalletHistoryActivityBinding>(),
    OnWalletHistoryListItemClickListener {

    private val viewModel by viewModels<WalletHistoryViewModel>()

    override fun getDataBindingLayoutId() = R.layout.wallet_history_activity

    override fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        binding.root.updatePadding(top = statusBarHeight)
        binding.srlList.updatePadding(bottom = navigationBarHeight)
        return true
    }

    override fun onApplyImeInsets(navigationBarHeight: Int, imeHeight: Int): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val address = intent.getStringExtra("address")
        viewModel.setAddress(address)

        binding.viewModel = viewModel
        binding.etvToolbar.setIconLeftClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.srlList.setEnableRefresh(true)
        binding.srlList.setEnableLoadMore(true)
        binding.srlList.setOnRefreshListener {
            loadList(refresh = true)
        }
        binding.srlList.setOnLoadMoreListener {
            loadList(refresh = false)
        }
        viewModel.loadingStatus.observe(this) {
            when (it) {
                ListLoadingStatus.LOADING -> {

                }

                ListLoadingStatus.ERROR -> {
                    binding.srlList.finishRefresh(false)
                    binding.srlList.finishLoadMore(false)
                }

                else -> {
                    binding.srlList.finishRefresh()
                    binding.srlList.finishLoadMore()
                }
            }
            binding.srlList.setEnableLoadMore(it != ListLoadingStatus.NO_MORE)
        }

        val adapter = WalletHistoryListAdapter(this, this)
        binding.rvHistoryList.adapter = adapter
        viewModel.historyList.observe(this) {
            adapter.submitList(it)
        }
        loadList(refresh = true)
    }

    private fun loadList(refresh: Boolean) {
        viewModel.loadPage(refresh)
            .doOnSubscribe {
                if (viewModel.historyList.value.isNullOrEmpty()) {
                    showLoading()
                }
            }
            .doOnTerminate {
                hideLoading()
                hideError()
            }
            .doOnError {
                if (viewModel.historyList.value.isNullOrEmpty()) {
                    showError {
                        loadList(true)
                    }
                }
            }
            .autoDispose(scope())
            .subscribe({}, {
                Timber.e(it)
            })
    }

    override fun onItemClick(item: WalletHistoryListItem) {
        val hash = item.data.txHash
        val address = viewModel.address
        if (hash.isNullOrEmpty()) return
        if (address.isNullOrEmpty()) return
        WalletTransactionActivity.start(this, address, hash, item.data.action ?: 0)
    }

    companion object {
        @JvmStatic
        fun start(context: Context, address: String? = null, newTask: Boolean = false) {
            val starter = Intent(context, WalletHistoryActivity::class.java)
            starter.putExtra("address", address)
            if (newTask) starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}

interface OnWalletHistoryListItemClickListener {
    fun onItemClick(item: WalletHistoryListItem)
}

@HiltViewModel
class WalletHistoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val walletManager: WalletManager,
) : ViewModel() {

    private val _address = MutableLiveData<String?>()
    val address: String?
        get() = _address.value
    val addressShort = _address.map { it?.shortAddress() }

    private val _historyList = MutableLiveData<List<IWalletHistoryListItem>>()
    val historyList: LiveData<List<IWalletHistoryListItem>>
        get() = _historyList
    private var cursor: String? = null
    private val _loadingStatus = MutableLiveData(ListLoadingStatus.NO_MORE)
    val loadingStatus: LiveData<ListLoadingStatus>
        get() = _loadingStatus

    fun setAddress(address: String?) {
        _address.value = address
    }

    fun loadPage(refresh: Boolean): Completable {
        return Single
            .defer {
                val address = _address.value
                if (address.isNullOrEmpty()) {
                    walletManager.getDefaultWallet().map { it.address }
                        .doOnSuccess { _address.postValue(it) }
                } else {
                    Single.just(address)
                }
            }
            .flatMap {
                Single.just(it).zipWith(
                    apiService.getWalletHistory(
                        it,
                        chains = "501",
                        cursor = if (refresh) null else this.cursor
                    )
                ) { addr, resp ->
                    this.cursor = resp.cursor

                    val currentList = _historyList.value
                    val newList = mutableListOf<IWalletHistoryListItem>()
                    var date: String? = null
                    resp.transactionList?.forEach { tx ->
                        val item = WalletHistoryListItem(addr, tx)
                        if (date != item.date) {
                            newList.add(WalletHistoryListItemDate(item.date))
                            date = item.date
                        }
                        newList.add(WalletHistoryListItem(addr, tx))
                    }

                    if (refresh || currentList.isNullOrEmpty()) {
                        this._historyList.postValue(newList)
                    } else {
                        val currentLast = currentList.lastOrNull() as? WalletHistoryListItem
                        val newFirst = newList.firstOrNull() as? WalletHistoryListItemDate
                        if (newFirst != null && currentLast?.date == newFirst.date) {
                            newList.removeAt(0)
                        }
                        this._historyList.postValue(currentList + newList)
                    }
                    resp
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _loadingStatus.value = ListLoadingStatus.LOADING
            }
            .doOnSuccess {
                _loadingStatus.value =
                    if (it.cursor.isNullOrEmpty()) ListLoadingStatus.NO_MORE else ListLoadingStatus.SUCCESS
            }
            .doOnError {
                _loadingStatus.value = ListLoadingStatus.ERROR
            }
            .ignoreElement()
    }
}

interface IWalletHistoryListItem {
    fun getItemViewType(): Int
}

class WalletHistoryListItemDate(val date: String) : IWalletHistoryListItem {
    override fun getItemViewType() = R.layout.wallet_history_list_item_date
}

class WalletHistoryListItem(val address: String, val data: Transaction) : IWalletHistoryListItem {
    override fun getItemViewType() = R.layout.wallet_history_list_item

    val date: String = data.txTime?.toLocalTimestamp("yyyy.MM.dd") ?: ""
    val timestampShort: String = data.txTime?.toLocalTimestamp("HH:mm") ?: ""
    val operationTokenUrl = when (data.action) {
        1 -> data.tokenLogoUrl
        2 -> data.tokenLogoUrl
        3 -> data.outTokenLogoUrl
        4 -> data.inTokenLogoUrl
        else -> ""
    }
    val operationResId: Int = when (data.action) {
        1 -> R.string.wallet_history_operation_receive
        2 -> R.string.wallet_history_operation_send
        3 -> R.string.wallet_history_operation_buy
        4 -> R.string.wallet_history_operation_Sell
        else -> 0
    }
    val operationSymbol: String = when (data.action) {
        1 -> data.symbol
        2 -> data.symbol
        3 -> data.symbolOut
        4 -> data.symbolIn
        else -> null
    } ?: ""
    val detailLine1: String = when (data.action) {
        1 -> "+${data.amount} ${data.symbol}"
        2 -> "-${data.amount} ${data.symbol}"
        3 -> "-${data.inAmount} ${data.symbolIn}"
        4 -> "+${data.outAmount} ${data.symbolOut}"
        else -> null
    } ?: ""
    val detailLine2: String = when (data.action) {
        3 -> "+${data.outAmount} ${data.symbolOut}"
        4 -> "-${data.inAmount} ${data.symbolIn}"
        else -> null
    } ?: ""
    val isSuccess: Boolean = data.txStatus == "success"
    val isError: Boolean = data.txStatus == "fail"
    val statusColorId: Int = when (data.txStatus) {
        "fail" -> R.color.red
        "pending" -> R.color.white
        else -> 0
    }
}

class WalletHistoryListItemViewHolder(private val binding: WalletHistoryListItemBinding) :
    ViewHolder(binding.root) {
    fun bind(item: WalletHistoryListItem) {
        binding.item = item
        binding.executePendingBindings()
    }
}

class WalletHistoryListItemDateViewHolder(private val binding: WalletHistoryListItemDateBinding) :
    ViewHolder(binding.root) {
    fun bind(item: WalletHistoryListItemDate) {
        binding.date = item.date
        binding.executePendingBindings()
    }
}

class WalletHistoryListAdapter(
    context: Context,
    private val listener: OnWalletHistoryListItemClickListener
) :
    ListAdapter<IWalletHistoryListItem, ViewHolder>(object :
        DiffUtil.ItemCallback<IWalletHistoryListItem>() {
        override fun areItemsTheSame(
            oldItem: IWalletHistoryListItem,
            newItem: IWalletHistoryListItem
        ): Boolean {
            return when {
                oldItem is WalletHistoryListItemDate && newItem is WalletHistoryListItemDate -> oldItem.date == newItem.date
                oldItem is WalletHistoryListItem && newItem is WalletHistoryListItem -> oldItem.data.txHash == newItem.data.txHash
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: IWalletHistoryListItem,
            newItem: IWalletHistoryListItem
        ): Boolean {
            return when {
                oldItem is WalletHistoryListItemDate && newItem is WalletHistoryListItemDate -> oldItem.date == newItem.date
                oldItem is WalletHistoryListItem && newItem is WalletHistoryListItem -> oldItem.data == newItem.data
                else -> false
            }
        }
    }) {
    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getItemViewType()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (viewType == R.layout.wallet_history_list_item_date) {
            return WalletHistoryListItemDateViewHolder(
                WalletHistoryListItemDateBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }
        val binding = WalletHistoryListItemBinding.inflate(layoutInflater, parent, false)
        binding.root.setOnClickListener {
            val item = binding.item ?: return@setOnClickListener
            listener.onItemClick(item)
        }
        return WalletHistoryListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is WalletHistoryListItemDateViewHolder) {
            holder.bind(getItem(position) as WalletHistoryListItemDate)
        } else if (holder is WalletHistoryListItemViewHolder) {
            holder.bind(getItem(position) as WalletHistoryListItem)
        }
    }
}