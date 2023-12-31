package sns.asteroid.view.adapter

import androidx.recyclerview.widget.DiffUtil
import sns.asteroid.api.entities.ContentInterface

class ContentDiffUtil<T: ContentInterface>: DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}