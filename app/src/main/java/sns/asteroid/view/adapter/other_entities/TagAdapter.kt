package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.Tag
import sns.asteroid.databinding.RowTagBinding
import sns.asteroid.view.adapter.ContentDiffUtil

class TagAdapter(val context: Context, val listener: OnHashtagSelectListener):
    ListAdapter<Tag, TagAdapter.ViewHolder>(ContentDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowTagBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.tag = getItem(position)

        val tag = getItem(position)
        binding.root.setOnClickListener { listener.onHashtagSelect(tag) }
    }

    class ViewHolder(val binding: RowTagBinding): RecyclerView.ViewHolder(binding.root)

    interface OnHashtagSelectListener {
        fun onHashtagSelect(tag: Tag)
    }
}