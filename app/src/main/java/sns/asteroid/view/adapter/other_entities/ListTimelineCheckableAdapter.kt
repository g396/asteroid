package sns.asteroid.view.adapter.other_entities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.RowAddToListBinding

/**
 * ListTimelineエンティティ(ユーザの作成したリスト)をリスト形式で表示するアダプタ
 * トグルボタン付
 * (リストへの追加・削除へ使用する)
 */
class ListTimelineCheckableAdapter(
    private val context: Context,
    private val listener: ItemSelectListener,
    private val accentColor: Int,
): ListAdapter<Pair<ListTimeline, Boolean>, ListTimelineCheckableAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowAddToListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position).first
        val isAdded = getItem(position).second

        binding.columnTitle.text = item.title

        binding.button.isChecked = isAdded
        binding.button.setOnClickListener {
            if(isAdded) listener.onRemoveButtonClick(item, binding.progressBar, binding.button)
            else listener.onAddButtonClick(item, binding.progressBar, binding.button)
        }
    }

    inner class ViewHolder(val binding: RowAddToListBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.color = accentColor
        }
    }

    private class Diff: DiffUtil.ItemCallback<Pair<ListTimeline, Boolean>>() {
        override fun areItemsTheSame(oldItem: Pair<ListTimeline, Boolean>, newItem: Pair<ListTimeline, Boolean>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<ListTimeline, Boolean>, newItem: Pair<ListTimeline, Boolean>): Boolean {
            return oldItem == newItem
        }
    }

    interface ItemSelectListener {
        fun onAddButtonClick(list: ListTimeline, progressBar: ProgressBar, button: ToggleButton)
        fun onRemoveButtonClick(list: ListTimeline, progressBar: ProgressBar, button: ToggleButton)
    }
}