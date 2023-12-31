package sns.asteroid.view.adapter.emoji

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.GridUnicodeEmojiBinding
import sns.asteroid.model.emoji.UnicodeEmoji

class UnicodeEmojiAdapter (
    context: Context,
    private val callback: EmojiAdapterCallback
): ListAdapter<UnicodeEmoji, UnicodeEmojiAdapter.ViewHolder>(Diff()){
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GridUnicodeEmojiBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.emoji.apply {
            text = item.unicodeString
            setOnClickListener { callback.onUnicodeEmojiSelect(item.unicodeString) }
        }
    }

    class ViewHolder(val binding: GridUnicodeEmojiBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<UnicodeEmoji>() {
        override fun areItemsTheSame(oldItem: UnicodeEmoji, newItem: UnicodeEmoji): Boolean {
            return oldItem.unicodeString == newItem.unicodeString
        }

        override fun areContentsTheSame(oldItem: UnicodeEmoji, newItem: UnicodeEmoji): Boolean {
            return (oldItem.unicodeString == newItem.unicodeString) and (oldItem.name == newItem.name)
        }
    }
}