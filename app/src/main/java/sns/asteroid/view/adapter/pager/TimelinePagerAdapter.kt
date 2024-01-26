package sns.asteroid.view.adapter.pager

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import sns.asteroid.R
import sns.asteroid.databinding.TabTimelineBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.view.fragment.recyclerview.*
import sns.asteroid.view.fragment.recyclerview.timeline.*
import sns.asteroid.view.fragment.recyclerview.HashtagTrendsFragment
import sns.asteroid.view.fragment.recyclerview.timeline.StatusesTrendsFragment

/**
 * ここから各カラム(タイムライン)のインスタンスをつくる
 */
class TimelinePagerAdapter(
    private val activity: FragmentActivity,
    private val viewPager2: ViewPager2,
    private val tabLayout: TabLayout? = null,
    private val enableAddMenu: Boolean = false,
) : FragmentStateAdapter(activity), PagerAdapter {
    private var columns = mutableListOf<Pair<ColumnInfo, Credential>>()

    /**
     * ViewPager2でのページの切替検知用
     * 親ActivityとFragment自身の双方にコールバックする
     */
    private val pageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            (activity as? PageChangeCallback)?.onPageChanged(position)
            (getFragment(position) as? FragmentShowObserver)?.onFragmentShow()

            // タブのインジケータの色をカラムの色に合わせる
            columns.getOrNull(position)?.let {
                tabLayout?.setSelectedTabIndicatorColor(it.second.accentColor)
            }
        }
    }

    init {
        viewPager2.also {
            it.adapter = this
            it.unregisterOnPageChangeCallback(pageChangeCallback)
            it.registerOnPageChangeCallback(pageChangeCallback)
        }

        tabLayout?.also {
            it.addOnTabSelectedListener(TabSelectListener(this))
        }
    }

    override fun getItemCount(): Int {
        return columns.size
    }

    override fun getItemId(position: Int): Long {
        return columns[position].first.hash.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return columns.find { it.first.hash.hashCode().toLong() == itemId } != null
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    override fun createFragment(position: Int): Fragment {
        val column = columns[position]
        val subject = column.first.subject

        return when (subject) {
            "notification"  -> NotificationFragment.newInstance(column, enableAddMenu)
            "mention"       -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "local"         -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "public"        -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "local_media"   -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "public_media"  -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "home"          -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "mix"           -> TimelineStreamingFragment.newInstance(column, enableAddMenu)
            "list"          -> ListTimelineFragment.newInstance(column, enableAddMenu)
            "hashtag"       -> HashtagTimelineFragment.newInstance(column, enableAddMenu)
            "trends_hashtags"-> HashtagTrendsFragment.newInstance(column)
            "trends_statuses"-> StatusesTrendsFragment.newInstance(column)
            "block"         -> AccountListFragment.newInstance(column)
            "mute"          -> AccountListFragment.newInstance(column)
            "favourited_by" -> AccountListFragment.newInstance(column)
            "reblogged_by"  -> AccountListFragment.newInstance(column)
            else            -> TimelineFragment.newInstance(column, enableAddMenu)
        }
    }

    /**
     * アクティビティとフラグメント両方に再生成かかると
     * なんかfragmentsに同じカラム（古いやつ）が入っちゃうぽいので
     * findLastで一番新しいfragmentを拾ってくる(この実装が良いかはまた別)
     */
    override fun getFragment(position: Int): Fragment? {
        val item = columns.getOrNull(position)
            ?: return null

        return activity.supportFragmentManager.fragments.findLast {
            item.first.hash == (it as? RecyclerViewFragment<*>)?.getColumnHash()
        }
    }

    /**
     * カラムの一覧を更新する
     */
    fun update(newList: List<Pair<ColumnInfo, Credential>>) {
        DiffUtil.calculateDiff(Diff(columns, newList)).apply {
            columns.clear()
            columns.addAll(newList)
        }.dispatchUpdatesTo(this)

        tabLayout?.also {
            TabLayoutMediator(it, viewPager2, TabConfig()).attach()
        }
        viewPager2.offscreenPageLimit = if (newList.isEmpty()) 1 else newList.size
    }

    fun getCurrentFragment(): Fragment? {
        val currentPosition = viewPager2.currentItem
        return getFragment(currentPosition)
    }

    fun getCurrentCredential(): Credential? {
        val currentPosition = viewPager2.currentItem
        val item = columns.getOrNull(currentPosition)
        return item?.second
    }


    inner class TabConfig: TabLayoutMediator.TabConfigurationStrategy {
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
            val subject = columns[position].first.subject
            val icon = when (subject) {
                "local" -> R.drawable.column_local
                "home" -> R.drawable.column_home
                "public" -> R.drawable.column_public
                "mix" -> R.drawable.column_mix
                "local_media" -> R.drawable.image
                "public_media" -> R.drawable.image
                "notification" -> R.drawable.column_notification
                "mention" -> R.drawable.mention
                "favourites" -> R.drawable.column_favourite
                "bookmarks" -> R.drawable.column_bookmarks
                "list" -> R.drawable.column_list
                "user_posts" -> R.drawable.user
                "user_media" -> R.drawable.image
                "hashtag" -> R.drawable.hashtag
                "trends_hashtags" -> R.drawable.trends
                "trends_statuses" -> R.drawable.trends
                else -> return
            }

            val customView = TabTimelineBinding.inflate(activity.layoutInflater).also { binding ->
                binding.root.minWidth = let {
                    val settings = SettingsValues.newInstance()
                    val base = settings.tabsWidth
                    val density = activity.resources.displayMetrics.density
                    (base * density).toInt()
                }
                binding.label.text = subject
                binding.icon.setImageResource(icon)

                val color = columns[position].second.accentColor

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.icon.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
                } else {
                    binding.icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                }
            }
            tab.customView = customView.root
            tab.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
        }
    }

    private class Diff(
        private val oldList: List<Pair<ColumnInfo, Credential>>,
        private val newList: List<Pair<ColumnInfo, Credential>>,
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }
        override fun getNewListSize(): Int {
            return newList.size
        }
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first.hash == newList[newItemPosition].first.hash
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first.hash == newList[newItemPosition].first.hash
        }
    }

    interface PageChangeCallback {
        fun onPageChanged(position: Int)
    }
}