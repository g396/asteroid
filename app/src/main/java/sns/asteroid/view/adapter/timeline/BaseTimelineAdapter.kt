package sns.asteroid.view.adapter.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import androidx.core.net.toUri
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.R
import sns.asteroid.api.entities.ContentInterface
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.RowPostsBinding
import sns.asteroid.databinding.RowPostsFilterBinding
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.view.adapter.ContentDiffUtil
import sns.asteroid.view.adapter.poll.PollAdapter
import sns.asteroid.view.adapter.timeline.sub.MediaAdapter
import sns.asteroid.view.adapter.timeline.sub.ReactionAdapter

/**
 * TL表示用Adapterと通知表示用Adapterの共通部分
 */
abstract class BaseTimelineAdapter<T: ContentInterface>(
    private val context: Context,
    private val listener: EventsListener,
): ListAdapter<T, RecyclerView.ViewHolder>(ContentDiffUtil<T>()) {
    abstract val columnContext: String

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_FILTER = 1
        const val VIEW_TYPE_HIDDEN = 2
    }

    /**
     * データ差替時のアニメーションを無効にする
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.itemAnimator = object: DefaultItemAnimator(){}.apply {
            supportsChangeAnimations = false
        }
        recyclerView.setHasFixedSize(true)
    }

    /**
     * ミュートするワードが含まれている場合に
     * 空のView又は警告文を表示するViewをbindする
     */
    override fun getItemViewType(position: Int): Int {
        if (getStatus(position)?.useFilter != true)
            return VIEW_TYPE_DEFAULT

        val filter = getStatus(position)?.filtered
            ?: return VIEW_TYPE_DEFAULT

        return if (filter.none { it.filter.context.contains(columnContext) })
            VIEW_TYPE_DEFAULT
        else if (filter.any { it.filter.filter_action == "hide" })
            VIEW_TYPE_HIDDEN
        else
            VIEW_TYPE_FILTER
    }

    /**
     * 警告文(ミュートを適用したフィルターのカテゴリ)を表示
     */
    protected fun bindFilterText(binding: RowPostsFilterBinding, position: Int) {
        val status = getStatus(position)?.reblog?: getStatus(position)
            ?.also { binding.posts = it }
        val parentStatus = getParentStatus(position) ?: return

        binding.filterText.setOnClickListener {
            status?.useFilter = false
            // ストリーミング中だとpositionがだんだんズレていくので再度取得する
            findPositions(parentStatus).forEach {
                notifyItemChanged(it)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected open fun bindStatus(binding: RowPostsBinding, position: Int) {
        val status = getStatus(position) ?: return
        val parentStatus = getParentStatus(position) ?: return

        // For BindingAdapter
        binding.posts = status
        binding.boostedBy =
            if(parentStatus.reblog != null) {
                val user = parentStatus.account.convertedDisplayName.ifBlank { parentStatus.account.acct }
                String.format(context.getString(R.string.boosted_by), user)
            } else null
        binding.boostVisibility = parentStatus.visibility

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

        // show or hide content warning
        binding.cw.root.setOnClickListener {
            status.isShowContent = !status.isShowContent
            findPositions(status).forEach {
                notifyItemChanged(it)
            }
        }

        binding.boostBy.setOnClickListener {
            listener.onAccountClick(parentStatus.account)
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
            (adapter as? MediaAdapter)?.submitList(status.media_attachments, status.sensitive)
        }
        binding.reactions.apply {
            val emojis = status.emoji_reactions ?: emptyList()
            (adapter as? ReactionAdapter)?.submitList(emojis, status)
        }
        binding.includePoll.poll.apply {
            val poll = status.poll ?: return@apply
            (adapter as? PollAdapter)?.submitPoll(poll)
        }
    }

    abstract fun getStatus(position: Int): Status?
    abstract fun getParentStatus(position: Int): Status?
    abstract fun findPositions(status: Status): List<Int>

}