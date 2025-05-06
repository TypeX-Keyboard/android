package cc.typex.app.page.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.liveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cc.typex.app.R
import cc.typex.app.databinding.WalletListActivityBinding
import cc.typex.app.databinding.WalletListItemViewBinding
import cc.typex.app.db.Wallet
import cc.typex.app.util.WalletManager
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WalletListActivity : DataBindingActivity<WalletListActivityBinding>(),
    WalletListItemClickListener {

    private val viewModel by viewModels<WalletListViewModel>()

    lateinit var adapter: WalletListAdapter

    override fun getDataBindingLayoutId() = R.layout.wallet_list_activity

    override fun onApplySystemBarInsets(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        binding.root.updatePadding(top = statusBarHeight)
        binding.llButtons.updateLayoutParams<MarginLayoutParams> {
            bottomMargin = navigationBarHeight
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.llButtons.addOnLayoutChangeListener { _, _, top, _, _, _, _, _, _ ->
            binding.rvList.updatePadding(bottom = binding.root.bottom - top)
        }

        binding.etvToolbar.setIconLeftClickListener {
            finish()
        }
        binding.btnImport.setOnClickListener {
            WalletAddActivity.import(this)
        }
        binding.btnCreate.setOnClickListener {
            WalletAddActivity.create(this)
        }

        adapter = WalletListAdapter(this, this)
        binding.rvList.adapter = adapter
        val pager = Pager(PagingConfig(10)) { viewModel.createPagingSource() }
        pager.liveData.observeForever {
            adapter.submitData(this@WalletListActivity.lifecycle, it)
        }
    }

    override fun onListItemClick(wallet: Wallet) {
        WalletDetailActivity.start(this, wallet.address)
    }

    companion object {
        fun start(context: Context, newTask: Boolean = false) {
            val starter = Intent(context, WalletListActivity::class.java)
            if (newTask) starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val walletManager: WalletManager
) : ViewModel() {

    fun createPagingSource() = walletManager.createPagingSource()

}


class WalletListAdapter(
    context: Context,
    private val listener: WalletListItemClickListener
) : PagingDataAdapter<Wallet, WalletViewHolder>(object : DiffUtil.ItemCallback<Wallet>() {
    override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
        return oldItem == newItem
    }
}) {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val itemBinding = WalletListItemViewBinding.inflate(layoutInflater, parent, false)
        itemBinding.root.setOnClickListener {
            val item = itemBinding.item ?: return@setOnClickListener
            listener.onListItemClick(item)
        }
        return WalletViewHolder(itemBinding)
    }
}

class WalletViewHolder(
    private val binding: WalletListItemViewBinding
) : ViewHolder(binding.root) {
    fun bind(wallet: Wallet?) {
        binding.item = wallet
        binding.executePendingBindings()
    }
}

interface WalletListItemClickListener {
    fun onListItemClick(wallet: Wallet)
}