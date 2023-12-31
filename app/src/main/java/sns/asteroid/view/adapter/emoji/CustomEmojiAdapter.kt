package sns.asteroid.view.adapter.emoji

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sns.asteroid.R
import sns.asteroid.api.entities.CustomEmoji
import sns.asteroid.databinding.GridEmojiBinding

class CustomEmojiAdapter(
    context: Context,
    private val isAnimationEnable: Boolean,
    private val callback: EmojiAdapterCallback,
): ListAdapter<CustomEmoji, CustomEmojiAdapter.ViewHolder>(Diff()){
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GridEmojiBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.customEmoji.apply {
            Glide.with(context)
                .load(item.url)
                .placeholder(R.drawable.sync)
                .dontTransform()
                .let { if(!isAnimationEnable) it.dontAnimate() else it }
                .into(this)
            setOnClickListener { callback.onCustomEmojiSelect(item) }
        }
    }

    class ViewHolder(val binding: GridEmojiBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<CustomEmoji>() {
        override fun areItemsTheSame(oldItem: CustomEmoji, newItem: CustomEmoji): Boolean {
            return oldItem.shortcode == newItem.shortcode
        }

        override fun areContentsTheSame(oldItem: CustomEmoji, newItem: CustomEmoji): Boolean {
            return oldItem == newItem
        }

    }
}