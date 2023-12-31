package sns.asteroid.view.adapter.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.RowProfileItemBinding

class ProfileItemAdapter(
    val context: Context,
    ): ListAdapter<MutableMap<String, String>, ProfileItemAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = RowProfileItemBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val map = getItem(position).also {
            binding.setLabel(it["name"])
            binding.setValue(it["value"])
        }

        binding.label.addTextChangedListener {
            map["name"] = it.toString()
        }
        binding.value.addTextChangedListener {
            map["value"] = it.toString()
        }
    }

    class ViewHolder(val binding: RowProfileItemBinding): RecyclerView.ViewHolder(binding.root)

    class Diff: DiffUtil.ItemCallback<MutableMap<String, String>>() {
        override fun areItemsTheSame(oldItem: MutableMap<String, String>, newItem: MutableMap<String, String>): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: MutableMap<String, String>, newItem: MutableMap<String, String>): Boolean {
            return oldItem == newItem
        }
    }

}