package sns.asteroid.view.adapter.timeline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Notification
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.RowHiddenBinding
import sns.asteroid.databinding.RowNotificationBinding
import sns.asteroid.databinding.RowPostsBinding
import sns.asteroid.databinding.RowPostsFilterBinding
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.view.adapter.poll.PollAdapter
import sns.asteroid.view.adapter.timeline.sub.MediaAdapter
import sns.asteroid.view.adapter.timeline.sub.NotificationImageAdapter
import sns.asteroid.view.adapter.timeline.viewholder.FilterViewHolder
import sns.asteroid.view.adapter.timeline.viewholder.HiddenViewHolder

class NotificationAdapter(
    val context: Context,
    private val listener: EventsListener,
    private val notificationListener: NotificationEventListener,
): BaseTimelineAdapter<Notification>(context, listener) {
    override val columnContext = "notifications"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when(viewType) {
            VIEW_TYPE_DEFAULT ->
                NotificationViewHolder(RowNotificationBinding.inflate(inflater, parent, false))
            VIEW_TYPE_FILTER ->
                FilterViewHolder(RowPostsFilterBinding.inflate(inflater, parent, false))
            else ->
                HiddenViewHolder(RowHiddenBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NotificationViewHolder -> bindReaction(holder.binding, position)
            is FilterViewHolder -> bindFilterText(holder.binding, position)
        }
    }

    override fun getStatus(position: Int): Status? {
        return getItem(position).status?.reblog ?: getItem(position).status
    }

    override fun getParentStatus(position: Int): Status? {
        return getItem(position).status
    }

    override fun findPositions(status: Status): List<Int> {
        return currentList.filter { status.id == it.status?.id }.map { currentList.indexOf(it) }
    }

    override fun bindStatus(binding: RowPostsBinding, position: Int) {
        super.bindStatus(binding, position)

        val status = getStatus(position) ?: return

        // 投稿の詳細を開く
        binding.root.setOnClickListener {
            listener.onStatusSelect(status)
        }
    }

    private fun bindReaction(binding: RowNotificationBinding, position: Int) {
        val notification = getItem(position)

        // View-binding
        binding.reaction.apply {
            account =
                notification.account
            accountCount =
                notification.otherAccount.size
            notificationType =
                notification.type
            content =
                notification.account.note.ifEmpty { "No content" }
        }

        // Events
        binding.reaction.apply {
            notificationTitle.setOnClickListener {
                notification.status?.let {
                    when (notification.type) {
                        "favourite"         -> notificationListener.onNotificationSelect(notification)
                        "reblog"            -> notificationListener.onNotificationSelect(notification)
                        "emoji_reaction"    -> notificationListener.onNotificationSelect(notification)
                        else                -> listener.onStatusSelect(it)
                    }
                } ?: listener.onAccountClick(notification.account)
            }
            notificationText.setOnClickListener {
                if(notification.status != null)
                    notificationListener.onNotificationSelect(notification)
                else
                    listener.onAccountClick(notification.account)
            }
            accept.setOnClickListener {
                notificationListener.onFollowRequestAccept(notification.account)
            }
            deny.setOnClickListener {
                notificationListener.onFollowRequestReject(notification.account)
            }

            userIcons.also {
                it.adapter = NotificationImageAdapter(context, notification, listener, notificationListener)
                it.layoutManager = FlexboxLayoutManager(context)
            }
        }

        binding.reaction.notificationText.movementMethod = TextLinkMovementMethod(object : TextLinkMovementMethod.LinkCallback {
            override fun onHashtagClick(hashtag: String) {
                listener.onHashtagClick(hashtag)
            }
            override fun onWebFingerClick(acct: String) {
                listener.onAccountClick(acct)
            }
            override fun onAccountURLClick(url: String) {
                val acct = notification.status?.mentions?.find { it.url == url }?.acct ?: return
                listener.onAccountClick(acct)
            }
        })

        binding.apply {
            status.root.isVisible = notification.status != null
            reaction.notificationText.isVisible = notification.status == null
        }

        if (notification.status != null) bindStatus(binding.status, position)
    }


    inner class NotificationViewHolder(val binding: RowNotificationBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.status.icon.visibility = View.GONE
            binding.status.username.visibility = View.GONE
            binding.status.editIcon.visibility = View.GONE
            binding.status.visibilityIcon.visibility = View.GONE
            binding.status.postAt.visibility = View.GONE
            binding.status.include.rowTootButton.visibility = View.GONE
            binding.status.via.visibility = View.GONE
            binding.status.reply.root.visibility = View.GONE
            binding.status.spaceEnd.visibility = View.GONE
            binding.status.reactions.visibility = View.GONE

            binding.status.mediaAttachments.also {
                it.adapter = MediaAdapter(context, listener, 4)
                it.layoutManager = GridLayoutManager(context, 4)
            }
            binding.status.includePoll.poll.also {
                it.adapter = PollAdapter(context)
                it.layoutManager = GridLayoutManager(context, 1)
            }

            binding.status.columnContext = columnContext
            binding.status.showVia = false
            binding.status.showRelation = false
        }
    }

    interface NotificationEventListener {
        fun onFollowRequestAccept(account: Account)
        fun onFollowRequestReject(account: Account)
        fun onNotificationSelect(notification: Notification)
    }
}