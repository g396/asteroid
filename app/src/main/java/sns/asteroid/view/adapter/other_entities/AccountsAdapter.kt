package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.Account
import sns.asteroid.databinding.RowAccountBinding
import sns.asteroid.view.adapter.ContentDiffUtil

class AccountsAdapter (
    private val context: Context,
    private val listener: AccountsAdapterListener,
): ListAdapter<Account, AccountsAdapter.ViewHolder>(ContentDiffUtil<Account>()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowAccountBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val account = getItem(position)

        // For Binding Adapter
        binding.setAccount(account)

        binding.root.setOnClickListener { listener.onAccountClick(account) }
        binding.note.setOnClickListener { listener.onAccountClick(account) }
    }

    class ViewHolder(val binding: RowAccountBinding) : RecyclerView.ViewHolder(binding.root)

    interface AccountsAdapterListener {
        fun onAccountClick(account: Account)
        fun onAccountClick(acct: String)
    }
}