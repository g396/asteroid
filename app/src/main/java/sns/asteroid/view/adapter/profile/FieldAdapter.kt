package sns.asteroid.view.adapter.profile

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.RowFieldBinding
import sns.asteroid.model.util.ImageSourceGetter
import sns.asteroid.model.util.TextLinkMovementMethod

class FieldAdapter(val context: Context, val linkMovementMethod: TextLinkMovementMethod): ListAdapter<Triple<String, String, String?>, FieldAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowFieldBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.name = item.first
        binding.value = item.second
        binding.verified = !item.third.isNullOrBlank()
    }

    inner class ViewHolder(val binding: RowFieldBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.valueTextView.movementMethod = linkMovementMethod
        }
    }

    private class Diff: DiffUtil.ItemCallback<Triple<String, String, String?>>() {
        override fun areItemsTheSame(oldItem: Triple<String, String, String?>, newItem: Triple<String, String, String?>): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: Triple<String, String, String?>, newItem: Triple<String, String, String?>): Boolean {
            return oldItem == newItem
        }
    }
}