package sns.asteroid.view.adapter.timeline

import android.view.View
import sns.asteroid.api.entities.FilterResult

/**
 * タイムラインのフィルタリング判定
 */
interface TimelineFilter {
    val columnContext: String

    fun filteringVisibility(filterResults: List<FilterResult>): Int {
        return if (filterResults.none { it.filter.context.contains(columnContext) }) View.VISIBLE else View.GONE
    }
    fun filteringMessageVisibility(filterResults: List<FilterResult>): Int {
        val hideWord = filterResults.filter {
            (it.filter.context.contains(columnContext)) and (it.filter.filter_action == "warn")
        }
        return if (hideWord.isNotEmpty()) View.VISIBLE else View.GONE
    }
}