package sns.asteroid.view.adapter.profile

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.Field
import sns.asteroid.databinding.RowFieldBinding
import sns.asteroid.model.util.TextLinkMovementMethod

class FieldAdapter(val context: Context, val linkMovementMethod: TextLinkMovementMethod): ListAdapter<Field, FieldAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowFieldBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.verified.apply {
            visibility = if(item.verified_at.isNullOrBlank()) View.GONE else View.VISIBLE
        }

        binding.name.apply {
            text = Html.fromHtml(item.name, Html.FROM_HTML_MODE_COMPACT)
        }
        binding.value.apply {
            text = Html.fromHtml(item.value, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    inner class ViewHolder(val binding: RowFieldBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.value.movementMethod = linkMovementMethod
        }
    }

    private class Diff: DiffUtil.ItemCallback<Field>() {
        override fun areItemsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem == newItem
        }
    }
}