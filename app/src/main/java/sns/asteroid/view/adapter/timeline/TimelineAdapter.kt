package sns.asteroid.view.adapter.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.PopupMenu
import android.widget.ToggleButton
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import sns.asteroid.R
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.RowPostsBinding
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.view.adapter.*
import sns.asteroid.view.adapter.poll.PollAdapter
import sns.asteroid.view.adapter.timeline.EventsListener.*
import sns.asteroid.view.adapter.timeline.EventsListener.Companion.FAVOURITE
import sns.asteroid.view.adapter.timeline.EventsListener.Companion.GENERAL
import sns.asteroid.view.adapter.timeline.EventsListener.Companion.MY_POSTS
import sns.asteroid.view.adapter.timeline.EventsListener.Companion.OTHER_ACCOUNT
import sns.asteroid.view.adapter.timeline.EventsListener.Companion.WHO_ACTIONED
import sns.asteroid.view.adapter.timeline.sub.MediaAdapter
import sns.asteroid.view.adapter.timeline.sub.ReactionAdapter

/**
 * タイムラインの要素
 * フッターは別で定義(TimelineFooterAdapter)
 */
open class TimelineAdapter(
    val context: Context,
    private val myAccountId: String,
    private val listener: EventsListener,
    override val columnContext: String,
) : ListAdapter<Status, TimelineAdapter.ViewHolder>(ContentDiffUtil<Status>()), TimelineFilter {
    private val settings = SettingsValues.getInstance()

    // アクションボタンを隠す設定が有効の際には
    // このIDの投稿だけボタンを表示する
    private var selectingStatusId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowPostsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val status = getItem(position).reblog?: getItem(position)
        val parentStatus = getItem(position)

        // For BindingAdapter
        binding.posts = status
        binding.columnContext = columnContext
        binding.filteringVisibility = filteringVisibility(status.filtered, status.useFilter)
        binding.filteringMessageVisibility = filteringMessageVisibility(status.filtered, status.useFilter)
        binding.boostedBy =
            if(parentStatus.reblog != null) {
                val user = parentStatus.account.convertedDisplayName.ifBlank { parentStatus.account.acct }
                String.format(context.getString(R.string.boosted_by), user)
            } else null
        binding.showCard = settings.isShowCard
        binding.showVia = settings.isShowVia
        binding.showRelation = true

        // ボタンを展開したり閉じたりするやつ
        binding.root.setOnClickListener {
            if (settings.isHideActionButtons) showOrHideActionButtons(parentStatus.id)
        }
        // 当たり判定広げる用
        binding.apply {
            summary.setOnClickListener { binding.root.callOnClick() }
            includePoll.root.setOnClickListener { binding.root.callOnClick() }
            includePoll.poll.setOnClickListener { binding.root.callOnClick() }

            // setOnClickListenerだとなんかうまくいかない
            reactions.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    binding.root.callOnClick()
                    true
                } else false
            }
        }

        // show filtered posts
        binding.filterText.setOnClickListener {
            status.useFilter = false
            // ストリーミング中だとpositionがだんだんズレていくので再度取得する
            val currentPosition = currentList.indexOfFirst { it.id == parentStatus.id }
            notifyItemChanged(currentPosition)
        }

        // show or hide content warning
        binding.cw.root.setOnClickListener {
            status.isShowContent = !status.isShowContent
            val currentPosition = currentList.indexOfFirst { it.id == parentStatus.id }
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
        binding.include.apply {
            root.setOnClickListener { return@setOnClickListener } // ボタンの隙間を押した時に非表示になるとめんどくさいので
            reply.setOnClickListener { listener.onReplyButtonClick(status) }
            favorite.setOnClickListener { listener.onFavouriteButtonClick(status, it as ToggleButton) }
            emojiAction.setOnClickListener { listener.onEmojiButtonClick(status, true, "") }
            boost.setOnClickListener { listener.onBoostButtonClick(status, it as ToggleButton) }
            bookmark.setOnClickListener { listener.onBookmarkButtonClick(status, it as ToggleButton) }
            detail.setOnClickListener(OnMenuClickListener(status))

            favorite.setOnLongClickListener { listener.onFavouriteButtonLongClick(status.uri).run { true } }
            boost.setOnLongClickListener { listener.onBoostButtonLongClick(status.uri).run { true } }
            bookmark.setOnLongClickListener { listener.onBookmarkButtonLongClick(status.uri).run { true } }
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
        }
        binding.reactions.apply {
            val emojis = status.emoji_reactions ?: emptyList()
            (adapter as ReactionAdapter).submitList(emojis, status)
        }
        binding.includePoll.poll.apply {
            val poll = status.poll ?: return@apply
            (adapter as? PollAdapter)?.submitPoll(poll)
        }

        // BindingAdapterに移行したいけどなんか重くなる
        binding.include.rowTootButton.apply {
            visibility = if ((parentStatus.id == selectingStatusId) or !settings.isHideActionButtons) View.VISIBLE else View.GONE
        }
    }

    private fun showOrHideActionButtons(id: String) {
        val index = currentList.indexOfFirst { it.id == id }
        val currentIndex = currentList.indexOfFirst { it.id == selectingStatusId }

        if (currentIndex == -1) {
            selectingStatusId = id
            notifyItemChanged(index)
        } else if (index == currentIndex) {
            selectingStatusId = null
            notifyItemChanged(currentIndex)
        } else {
            selectingStatusId = id
            notifyItemChanged(currentIndex)
            notifyItemChanged(index)
        }
    }

    fun showActionButton(id: String?) {
        selectingStatusId = id
        val index = currentList.indexOfFirst { it.id == id }
        if(index >= 0) notifyItemChanged(index)
    }

    inner class OnMenuClickListener(private val posts: Status): View.OnClickListener {
        override fun onClick(v: View?) {
            val popupMenu = PopupMenu(context, v)

            popupMenu.menu.apply {
                if(myAccountId == posts.account.id)
                    add(MY_POSTS, Item.MENU_DELETE.order, Item.MENU_DELETE.order, R.string.delete)
                if((myAccountId == posts.account.id) and !posts.pinned)
                    add(MY_POSTS, Item.MENU_PIN.order, Item.MENU_PIN.order, context.getString(R.string.pin_the_posts))
                if((myAccountId == posts.account.id) and posts.pinned)
                    add(MY_POSTS, Item.MENU_UNPIN.order, Item.MENU_UNPIN.order, context.getString(R.string.unpin_the_posts))

                if((posts.emoji_reactions != null) and !posts.favourited) {
                    add(FAVOURITE, Item.MENU_FAVOURITE.order, Item.MENU_FAVOURITE.order, context.getString(R.string.favourite))
                }
                if((posts.emoji_reactions != null) and posts.favourited)
                    add(FAVOURITE, Item.MENU_UNFAVOURITE.order, Item.MENU_UNFAVOURITE.order, context.getString(R.string.unfavourite))
                if(posts.favourites_count > 0)
                    add(WHO_ACTIONED, Item.MENU_WHO_FAVOURITED.order, Item.MENU_WHO_FAVOURITED.order,  context.getString(R.string.who_favourited))
                if(posts.reblogs_count > 0)
                    add(WHO_ACTIONED, Item.MENU_WHO_BOOSTED.order, Item.MENU_WHO_BOOSTED.order, context.getString(R.string.who_reblogged))
                if(!posts.visibility.matches(Regex("private|direct")))
                    add(OTHER_ACCOUNT, Item.MENU_BOOST_OTHER_ACCOUNT.order, Item.MENU_BOOST_OTHER_ACCOUNT.order, context.getString(R.string.boost_other_account))

                add(OTHER_ACCOUNT, Item.MENU_FAVOURITE_OTHER_ACCOUNT.order, Item.MENU_FAVOURITE_OTHER_ACCOUNT.order, context.getString(R.string.favourite_other_account))
                add(OTHER_ACCOUNT, Item.MENU_BOOKMARK_OTHER_ACCOUNT.order, Item.MENU_BOOKMARK_OTHER_ACCOUNT.order, context.getString(R.string.bookmark_other_account))
                add(GENERAL, Item.MENU_OPEN_BROWSER.order, Item.MENU_OPEN_BROWSER.order, R.string.open_in_browser)
                add(GENERAL, Item.MENU_COPY_CLIPBOARD.order, Item.MENU_COPY_CLIPBOARD.order, R.string.copy_link)
            }
            popupMenu.setOnMenuItemClickListener {
                val item = Item.values().find { enum -> it.order == enum.order }
                item?.let { listener.onMenuItemClick(posts, it) }
                false
            }
            popupMenu.show()
        }
    }

    inner class ViewHolder(internal val binding: RowPostsBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.mediaAttachments.also {
                it.adapter = MediaAdapter(context, listener, 2)
            }
            binding.reactions.also {
                it.adapter = ReactionAdapter(context, settings.isEnableEmojiAnimation, listener)
                it.layoutManager = FlexboxLayoutManager(context)
            }
            binding.includePoll.poll.also {
                it.adapter = PollAdapter(context)
                it.layoutManager = GridLayoutManager(context, 1)
            }
            binding.include.showCounts = settings.isShowReactionsCount
        }
    }
}