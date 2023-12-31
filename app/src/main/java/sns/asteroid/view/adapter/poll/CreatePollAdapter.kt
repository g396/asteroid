package sns.asteroid.view.adapter.poll

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.RowCreatePollBinding

class CreatePollAdapter(
    val context: Context,
): ListAdapter<MutableMap<String, String>, CreatePollAdapter.ViewHolder>(Diff()) {
    init {
        submitList(listOf(
            mutableMapOf(Pair("value", "")),
            mutableMapOf(Pair("value", "")),
            mutableMapOf(Pair("value", "")),
            mutableMapOf(Pair("value", "")),
            )
        )
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = RowCreatePollBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val map = getItem(position).also {
            binding.value = it["value"]
        }

        binding.editText.addTextChangedListener {
            map["value"] = it.toString()
        }
    }

    fun getList(): List<String> {
        return mutableListOf<String>().apply {
            currentList.forEach { add(it["value"] ?: "")}
        }
    }

    class ViewHolder(val binding: RowCreatePollBinding): RecyclerView.ViewHolder(binding.root)

    class Diff: DiffUtil.ItemCallback<MutableMap<String, String>>() {
        override fun areItemsTheSame(oldItem: MutableMap<String, String>, newItem: MutableMap<String, String>): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: MutableMap<String, String>, newItem: MutableMap<String, String>): Boolean {
            return oldItem == newItem
        }
    }

}