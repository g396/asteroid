package sns.asteroid.view.adapter.timeline.sub

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Notification
import sns.asteroid.databinding.RowNotificationIconBinding
import sns.asteroid.view.adapter.timeline.EventsListener
import sns.asteroid.view.adapter.timeline.NotificationAdapter

class NotificationImageAdapter (
    context: Context,
    private val notification: Notification,
    private val listener: EventsListener,
    private val notificationListener: NotificationAdapter.NotificationEventListener,
): ListAdapter<Account, NotificationImageAdapter.ViewHolder>(Diff()){
    private val inflater = LayoutInflater.from(context)

    init {
        submitList(notification.otherAccount.ifEmpty { listOf(notification.account) })
    }

    override fun getItemCount(): Int {
        return when (notification.type) {
            "emoji_reaction"    -> super.getItemCount() + 1
            "favourite"         -> super.getItemCount() + 1
            "reblog"            -> super.getItemCount() + 1
            "mention"           -> super.getItemCount() + 1
            else                -> super.getItemCount()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowNotificationIconBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= currentList.size) {
            setActionIcon(holder)
        } else {
            setUser(holder, position)
        }
    }

    private fun setActionIcon(holder: ViewHolder) {
        val binding = holder.binding

        binding.imageType = notification.type

        binding.icon.setOnClickListener {
            if (notification.status != null) notificationListener.onNotificationSelect(notification)
            else listener.onAccountClick(notification.account)
        }

        notification.emoji_reaction?.let {
            if(it.url.isNotEmpty()) {
                binding.image = it.url
            } else {
                binding.unicodeEmoji = it.name
            }
        }
    }

    private fun setUser(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.image = item.avatar
        binding.imageType = "user"

        binding.icon.setOnClickListener { listener.onAccountClick(item) }
    }

    class ViewHolder(val binding: RowNotificationIconBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}