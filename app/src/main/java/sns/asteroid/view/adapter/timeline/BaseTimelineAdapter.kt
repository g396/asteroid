package sns.asteroid.view.adapter.timeline

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.api.entities.ContentInterface
import sns.asteroid.api.entities.Status
import sns.asteroid.view.adapter.ContentDiffUtil
import sns.asteroid.view.adapter.timeline.viewholder.FilterViewHolder

/**
 * TL表示用Adapterと通知表示用Adapterの共通部分
 */
abstract class BaseTimelineAdapter<T: ContentInterface>: ListAdapter<T, RecyclerView.ViewHolder>(ContentDiffUtil<T>()) {
    abstract val columnContext: String

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_FILTER = 1
        const val VIEW_TYPE_HIDDEN = 2
    }

    /**
     * データ差替時のアニメーションを無効にする
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.itemAnimator = object: DefaultItemAnimator(){}.apply {
            supportsChangeAnimations = false
        }
        recyclerView.setHasFixedSize(true)
    }

    /**
     * ミュートするワードが含まれている場合に
     * 空のView又は警告文を表示するViewをbindする
     */
    override fun getItemViewType(position: Int): Int {
        if (getStatus(position)?.useFilter != true)
            return VIEW_TYPE_DEFAULT

        val filter = getStatus(position)?.filtered
            ?: return VIEW_TYPE_DEFAULT

        return if (filter.none { it.filter.context.contains(columnContext) })
            VIEW_TYPE_DEFAULT
        else if (filter.any { it.filter.filter_action == "hide" })
            VIEW_TYPE_HIDDEN
        else
            VIEW_TYPE_FILTER
    }

    /**
     * 警告文(ミュートを適用したフィルターのカテゴリ)を表示
     */
    protected fun onBindFilterViewHolder(holder: FilterViewHolder, position: Int) {
        val binding = holder.binding
        val status = getStatus(position)?.reblog?: getStatus(position)
            ?.also { binding.posts = it }
        val parentStatus = getParentStatus(position) ?: return

        binding.filterText.setOnClickListener {
            status?.useFilter = false
            // ストリーミング中だとpositionがだんだんズレていくので再度取得する
            findPositions(parentStatus).forEach {
                notifyItemChanged(it)
            }
        }
    }

    abstract fun getStatus(position: Int): Status?
    abstract fun getParentStatus(position: Int): Status?
    abstract fun findPositions(status: Status): List<Int>

}