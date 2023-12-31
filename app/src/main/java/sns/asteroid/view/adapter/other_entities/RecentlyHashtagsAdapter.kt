package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.RowRecentlyHashtagBinding

class RecentlyHashtagsAdapter(
    val context: Context,
    val listener: OnHashtagSelectListener
    ): ListAdapter<String, RecentlyHashtagsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowRecentlyHashtagBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val tag = getItem(position)

        binding.tag = tag
        binding.root.setOnClickListener { listener.onHashtagSelect(tag) }
        binding.remove.setOnClickListener { listener.onRemoveButtonClick(tag)}
    }

    class ViewHolder(val binding: RowRecentlyHashtagBinding): RecyclerView.ViewHolder(binding.root)

    interface OnHashtagSelectListener {
        fun onHashtagSelect(hashtag: String)
        fun onRemoveButtonClick(hashtag: String)
    }

    class Diff: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}