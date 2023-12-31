package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.Account
import sns.asteroid.databinding.RowAccountAddToListBinding

/**
 * Accountエンティティをリスト形式で表示するアダプタ
 * トグルボタン付
 * (リストへの追加・削除へ使用する)
 */
class AccountsCheckableAdapter (
    private val context: Context,
    private val listener: ItemSelectListener,
): ListAdapter<Pair<Account, Boolean>, AccountsCheckableAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowAccountAddToListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position).first
        val isAdded = getItem(position).second

        binding.account = item

        binding.root.also {
            it.setOnClickListener { listener.onAccountClick(item) }
        }
        binding.username.also {
            it.setOnClickListener { listener.onAccountClick(item) }
        }

        binding.button.isChecked = isAdded
        binding.button.setOnClickListener {
            if(isAdded) listener.onRemoveButtonClick(item, binding.progressBar, binding.button)
            else listener.onAddButtonClick(item, binding.progressBar, binding.button)
        }
    }

    inner class ViewHolder(val binding: RowAccountAddToListBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<Pair<Account, Boolean>>() {
        override fun areItemsTheSame(oldItem: Pair<Account, Boolean>, newItem: Pair<Account, Boolean>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<Account, Boolean>, newItem: Pair<Account, Boolean>): Boolean {
            return oldItem == newItem
        }
    }

    interface ItemSelectListener {
        fun onAccountClick(account: Account)
        fun onAddButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton)
        fun onRemoveButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton)
    }
}