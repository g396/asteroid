package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.RowListBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.ContentDiffUtil

class ListTimelineAdapter(
    val context: Context,
    val credential: Credential,
    val listener: ListTimelineAdapterListener,
): ListAdapter<ListTimeline, ListTimelineAdapter.ViewHolder>(ContentDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = RowListBinding.inflate(layoutInflater)
        return ViewHolder(binding, credential.accentColor)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding

        binding.columnTitle.setText(item.title)
        binding.root.setOnClickListener { listener.onListSelect(item) }
        binding.remove.setOnClickListener { listener.onDeleteButtonClick(item) }
        binding.relation.setOnClickListener { listener.onShowAccountsButtonClick(item) }
        binding.edit.setOnClickListener { listener.onUpdateButtonClick(item) }
    }

    class ViewHolder(val binding: RowListBinding, val color: Int): RecyclerView.ViewHolder(binding.root){
        init {
            binding.icon.imageTintList = let {
                val selectorArray = arrayOf(intArrayOf(0))
                val colorArray = intArrayOf(color)
                ColorStateList(selectorArray, colorArray)
            }
        }
    }

    interface ListTimelineAdapterListener {
        fun onListSelect(list: ListTimeline)
        fun onDeleteButtonClick(list: ListTimeline)
        fun onShowAccountsButtonClick(list: ListTimeline)
        fun onUpdateButtonClick(list: ListTimeline)
    }
}