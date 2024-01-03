package sns.asteroid.view.adapter.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Notification
import sns.asteroid.databinding.RowNotificationBinding
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.view.adapter.ContentDiffUtil
import sns.asteroid.view.adapter.poll.PollAdapter
import sns.asteroid.view.adapter.timeline.sub.MediaAdapter
import sns.asteroid.view.adapter.timeline.sub.NotificationImageAdapter

class NotificationAdapter(
    val context: Context,
    private val listener: EventsListener,
    private val notificationListener: NotificationEventListener,
) : ListAdapter<Notification, NotificationAdapter.ViewHolder>(ContentDiffUtil<Notification>()), TimelineFilter {
    override val columnContext = "notifications"
    private val settings = SettingsValues.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowNotificationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val notification = getItem(position)

        binding.apply {
            status.root.isVisible = notification.status != null
            reaction.notificationText.isVisible = notification.status == null
        }

        setReactionView(holder, position)
        if (notification.status != null) setStatusView(holder, position)
    }

    private fun setReactionView(holder: ViewHolder, position: Int) {
        val binding = holder.binding
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
            filteringVisibility =
                notification.status?.let { filteringVisibility(it.filtered, it.useFilter) } ?: View.VISIBLE
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
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setStatusView(holder: ViewHolder, position: Int) {
        val binding = holder.binding.status
        val notification = getItem(position)
        val status = notification.status ?: return

        // For BindingAdapter
        binding.posts = status
        binding.columnContext = columnContext
        binding.filteringVisibility = filteringVisibility(status.filtered, status.useFilter)
        binding.filteringMessageVisibility = filteringMessageVisibility(status.filtered, status.useFilter)
        binding.showCard = settings.isShowCard
        binding.showVia = false
        binding.showRelation = false

        holder.binding.apply {
            reactedUser = notification.account
            notificationType = notification.type
        }

        // 投稿の詳細を開く
        binding.root.setOnClickListener {
            listener.onStatusSelect(status)
        }
        // 当たり判定広げる用
        binding.apply {
            summary.setOnClickListener { root.callOnClick() }
            includePoll.root.setOnClickListener { root.callOnClick() }
            includePoll.poll.setOnClickListener { root.callOnClick() }
        }

        // show filtered posts
        binding.filterText.setOnClickListener {
            status.useFilter = false
            val currentPosition = currentList.indexOfFirst { it.status?.id == status.id }
            notifyItemChanged(currentPosition)
        }

        // show or hide content warning
        binding.cw.root.setOnClickListener {
            status.isShowContent = !status.isShowContent
            val currentPosition = currentList.indexOfFirst { it.status?.id == status.id }
            notifyItemChanged(currentPosition)
        }

        binding.icon.setOnClickListener {
            listener.onAccountClick(status.reblog?.account?: status.account)
        }
        binding.reply.root.setOnClickListener {
            listener.onStatusSelect(status)
        }
        binding.includePoll.pollButton.setOnClickListener {
            val recyclerView = binding.includePoll.poll
            val loading = binding.includePoll.pollLoading
            val checked = (recyclerView.adapter as PollAdapter).getCheckedList()
            listener.onVoteButtonClick(status.poll!!.id, checked, loading)
        }

        binding.card.rowTootCard.setOnClickListener {
            val uri = status.card?.url?.toUri() ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }

        // enable hyperlink in textview
        binding.summary.movementMethod = TextLinkMovementMethod(object : TextLinkMovementMethod.LinkCallback {
            override fun onHashtagClick(hashtag: String) {
                listener.onHashtagClick(hashtag)
            }
            override fun onWebFingerClick(acct: String) {
                listener.onAccountClick(acct)
            }
            override fun onAccountURLClick(url: String) {
                val acct = status.mentions.find { it.url == url }?.acct ?: return
                listener.onAccountClick(acct)
            }
        })

        // RecyclerView contents
        binding.mediaAttachments.apply {
            (adapter as MediaAdapter).submitList(status.media_attachments, status.sensitive)
            (layoutManager as GridLayoutManager).spanSizeLookup = MediaAdapter.MediaSpanSizeLookUp(status.media_attachments.size)
        }
        binding.includePoll.poll.apply {
            val poll = status.poll ?: return@apply
            (adapter as? PollAdapter)?.submitPoll(poll)
        }
    }

    inner class ViewHolder(val binding: RowNotificationBinding): RecyclerView.ViewHolder(binding.root) {
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
                it.adapter = MediaAdapter(context, listener)
                it.layoutManager = GridLayoutManager(context, 2)
            }
            binding.status.includePoll.poll.also {
                it.adapter = PollAdapter(context)
                it.layoutManager = GridLayoutManager(context, 1)
            }
        }
    }

    interface NotificationEventListener {
        fun onFollowRequestAccept(account: Account)
        fun onFollowRequestReject(account: Account)
        fun onNotificationSelect(notification: Notification)
    }
}