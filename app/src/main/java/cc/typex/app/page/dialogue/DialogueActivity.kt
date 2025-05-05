package cc.typex.app.page.dialogue

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import autodispose2.androidx.lifecycle.scope
import autodispose2.autoDispose
import cc.typex.app.AppRemoteService
import cc.typex.app.R
import cc.typex.app.aidl.IAppAidlInterface
import cc.typex.app.databinding.DialogueActivityBinding
import cc.typex.app.databinding.DialogueListItemButton2Binding
import cc.typex.app.databinding.DialogueListItemButtonBinding
import cc.typex.app.databinding.DialogueListItemTextBinding
import cc.typex.app.widget.DialoguePopupWindow
import cc.typex.base.page.DataBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DialogueActivity : DataBindingActivity<DialogueActivityBinding>(),
    DialogueListItemOperationListener {

    private val viewModel by viewModels<DialogueViewModel>()

    private var remoteService: IAppAidlInterface? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            remoteService = IAppAidlInterface.Stub.asInterface(service)

            if (intent.getBooleanExtra("open_wallet", false)) {
                remoteService?.showCustomView("wallet")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remoteService = null
        }
    }

    override fun getDataBindingLayoutId() = R.layout.dialogue_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindRemoteService()
        binding.etvToolbar.setIconLeftClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.etInput.requestFocus()

        val adapter = DialogueListAdapter(this, this)
        binding.rvList.adapter = adapter
        viewModel.list.observe(this) {
            adapter.submitList(it)
        }

        viewModel.showWelcomeMessage()
            .andThen(viewModel.showOperationMessage())
            .andThen(Completable.timer(400L, TimeUnit.MILLISECONDS))
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe({
                if (!intent.getBooleanExtra("open_wallet", false)) {
                    DialoguePopupWindow(this)
                        .showAbove(binding.etInput)
                }
            }, {
                Timber.e(it)
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindRemoteService()
    }

    private fun bindRemoteService() {
        Timber.v("bindRemoteService")
        val intent = Intent(this, AppRemoteService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindRemoteService() {
        Timber.v("unbindRemoteService")
        unbindService(serviceConnection)
    }

    override fun onClickItemOperation(operation: DialogueListItemOperation) {
        when (operation) {
            DialogueListItemOperation.PERMISSION -> {

            }

            DialogueListItemOperation.TRANSFER -> {
                viewModel.showWalletMessage()
                    .autoDispose(scope())
                    .subscribe({

                    }, {
                        Timber.e(it)
                    })
            }

            DialogueListItemOperation.SWAP -> {
                viewModel.showTokenMessage()
                    .autoDispose(scope())
                    .subscribe({

                    }, {
                        Timber.e(it)
                    })
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context, newTask: Boolean = false, openWallet: Boolean = false) {
            val starter = Intent(context, DialogueActivity::class.java)
            if (newTask) starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            starter.putExtra("open_wallet", openWallet)
            context.startActivity(starter)
        }
    }
}

@HiltViewModel
class DialogueViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _list = MutableLiveData<List<DialogueListItem>>()
    val list: LiveData<List<DialogueListItem>>
        get() = _list

    fun showWelcomeMessage(): Completable {
        val item =
            DialogueListItemText(getApplication<Application>().getString(R.string.dialogues_message_welcome))
        return insertMessage(item)
    }

    fun showPermissionMessage(): Completable {
        val item = DialogueListItemButton1(
            getApplication<Application>().getString(R.string.dialogues_message_permission),
            DialogueListItemOperation.PERMISSION
        )
        return insertMessage(item)
    }

    fun showOperationMessage(): Completable {
        val item = DialogueListItemButton2(
            getApplication<Application>().getString(R.string.dialogues_message_transfer),
            DialogueListItemOperation.TRANSFER,
            getApplication<Application>().getString(R.string.dialogues_message_swap),
            DialogueListItemOperation.SWAP
        )
        return insertMessage(item)
    }

    fun showWalletMessage(): Completable {
        val item =
            DialogueListItemText(getApplication<Application>().getString(R.string.dialogues_message_wallet))
        return insertMessage(item)
    }

    fun showTokenMessage(): Completable {
        val item =
            DialogueListItemText(getApplication<Application>().getString(R.string.dialogues_message_meme))
        return insertMessage(item)
    }


    private fun insertMessage(item: DialogueListItem): Completable {
        return Completable.timer(400L, TimeUnit.MILLISECONDS)
            .doOnComplete {
                val list = _list.value?.toMutableList() ?: mutableListOf()
                list.add(item)
                _list.postValue(list)
            }
    }
}

interface DialogueListItemOperationListener {
    fun onClickItemOperation(operation: DialogueListItemOperation)
}

enum class DialogueListItemOperation {
    PERMISSION,
    TRANSFER,
    SWAP,
    ;
}

interface DialogueListItem {
    fun getViewType(): Int
}

data class DialogueListItemText(val text: String) : DialogueListItem {
    override fun getViewType() = R.layout.dialogue_list_item_text
}

data class DialogueListItemButton1(val text: String, val operation: DialogueListItemOperation) :
    DialogueListItem {
    override fun getViewType() = R.layout.dialogue_list_item_button
}

data class DialogueListItemButton2(
    val text1: String,
    val operation1: DialogueListItemOperation,
    val text2: String,
    val operation2: DialogueListItemOperation
) : DialogueListItem {
    override fun getViewType() = R.layout.dialogue_list_item_button_2
}

class DialogueListItemTextHolder(val binding: DialogueListItemTextBinding) :
    ViewHolder(binding.root)

class DialogueListItemButtonHolder(val binding: DialogueListItemButtonBinding) :
    ViewHolder(binding.root)

class DialogueListItemButton2Holder(val binding: DialogueListItemButton2Binding) :
    ViewHolder(binding.root)

class DialogueListAdapter(
    context: Context,
    private val listener: DialogueListItemOperationListener
) : ListAdapter<DialogueListItem, ViewHolder>(
    object : DiffUtil.ItemCallback<DialogueListItem>() {
        override fun areItemsTheSame(
            oldItem: DialogueListItem,
            newItem: DialogueListItem
        ): Boolean {
            return when {
                oldItem is DialogueListItemText && newItem is DialogueListItemText -> oldItem == newItem
                oldItem is DialogueListItemButton1 && newItem is DialogueListItemButton1 -> oldItem == newItem
                oldItem is DialogueListItemButton2 && newItem is DialogueListItemButton2 -> oldItem == newItem
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: DialogueListItem,
            newItem: DialogueListItem
        ): Boolean {
            return when {
                oldItem is DialogueListItemText && newItem is DialogueListItemText -> oldItem == newItem
                oldItem is DialogueListItemButton1 && newItem is DialogueListItemButton1 -> oldItem == newItem
                oldItem is DialogueListItemButton2 && newItem is DialogueListItemButton2 -> oldItem == newItem
                else -> false
            }
        }
    }
) {

    private val inflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getViewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            R.layout.dialogue_list_item_text -> DialogueListItemTextHolder(
                DialogueListItemTextBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            R.layout.dialogue_list_item_button -> DialogueListItemButtonHolder(
                DialogueListItemButtonBinding.inflate(
                    inflater,
                    parent,
                    false
                ).apply {
                    vArrow.setOnClickListener {
                        listener.onClickItemOperation(item?.operation ?: return@setOnClickListener)
                    }
                }
            )

            R.layout.dialogue_list_item_button_2 -> DialogueListItemButton2Holder(
                DialogueListItemButton2Binding.inflate(
                    inflater,
                    parent,
                    false
                ).apply {
                    vArrow1.setOnClickListener {
                        listener.onClickItemOperation(item?.operation1 ?: return@setOnClickListener)
                    }
                    vArrow2.setOnClickListener {
                        listener.onClickItemOperation(item?.operation2 ?: return@setOnClickListener)
                    }
                }
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is DialogueListItemTextHolder -> {
                holder.binding.item = getItem(position) as DialogueListItemText
            }

            is DialogueListItemButtonHolder -> {
                holder.binding.item = getItem(position) as DialogueListItemButton1
            }

            is DialogueListItemButton2Holder -> {
                holder.binding.item = getItem(position) as DialogueListItemButton2
            }
        }
    }
}