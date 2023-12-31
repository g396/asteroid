package sns.asteroid.view.adapter.timeline.sub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sns.asteroid.R
import sns.asteroid.api.entities.EmojiReaction
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.RowEmojiReactionBinding
import sns.asteroid.view.adapter.timeline.EventsListener

/**
 * 投稿についたリアクション(絵文字)を表示する
 * Fedibird独自機能用
 */

class ReactionAdapter(
    val context: Context,
    private val isAnimationEnable: Boolean,
    val listener: EventsListener,
):ListAdapter<EmojiReaction, ReactionAdapter.ViewHolder>(Diff()) {
    private lateinit var status: Status
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowEmojiReactionBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val reaction = getItem(position)

        binding.emojiUnicode.visibility =
            if(reaction.url.isEmpty()) View.VISIBLE
            else View.GONE
        binding.emoji.visibility =
            if(reaction.url.isNotEmpty()) View.VISIBLE
            else View.GONE

        if (reaction.url.isEmpty())
            binding.emojiUnicode.text = reaction.name
        else
            Glide.with(context)
                .load(reaction.url)
                .placeholder(R.drawable.sync)
                .dontTransform() // 勝手に解像度落ちないように
                .let { if(!isAnimationEnable) it.dontAnimate() else it }
                .into(binding.emoji)

        binding.count.text = reaction.count.toString()
        binding.root.setOnClickListener {
            val isPut = !reaction.me
            val name =
                if (reaction.domain == null) reaction.name
                else (reaction.name + "@" + reaction.domain)
            listener.onEmojiButtonClick(status, isPut, name)
        }

        if(reaction.me) {
            binding.background.setBackgroundResource(R.drawable.background_reaction_me)
        } else {
            binding.background.setBackgroundResource(R.drawable.background_reaction)
        }
    }

    fun submitList(list: List<EmojiReaction>, status: Status) {
        this.status = status
        submitList(list)
    }

    class ViewHolder(val binding: RowEmojiReactionBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<EmojiReaction>() {
        override fun areItemsTheSame(oldItem: EmojiReaction, newItem: EmojiReaction): Boolean {
            return oldItem == newItem // Unicode絵文字がnameだと判別できない・・・
        }
        override fun areContentsTheSame(oldItem: EmojiReaction, newItem: EmojiReaction): Boolean {
            return oldItem == newItem
        }
    }
}