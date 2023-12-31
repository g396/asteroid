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
import sns.asteroid.api.entities.Search
import sns.asteroid.databinding.TabTimelineBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.fragment.NoMatchesFragment
import sns.asteroid.view.fragment.recyclerview.AccountSearchFragment
import sns.asteroid.view.fragment.recyclerview.HashtagSearchFragment
import sns.asteroid.view.fragment.recyclerview.timeline.StatusSearchFragment

/**
 * 検索画面のページングで使用する
 * ViewPager2とTabLayoutはここで設定する
 */
class SearchPagerAdapter(
    val activity: FragmentActivity,
    tab: TabLayout,
    viewPager2: ViewPager2,
    private val credential: Credential,
) : FragmentStateAdapter(activity), PagerAdapter {
    private var columns = mutableListOf(
        Pair(Subject.STATUSES, ""),
        Pair(Subject.ACCOUNTS, ""),
        Pair(Subject.HASHTAGS, ""),
    )
    private var search: Search? = null

    init {
        viewPager2.also {
            it.adapter = this
            it.offscreenPageLimit = 3
        }

        tab.also {
            TabLayoutMediator(it, viewPager2, TabConfig()).attach()
            it.addOnTabSelectedListener(TabSelectListener(this))
            it.setSelectedTabIndicatorColor(credential.accentColor)
        }
    }

    override fun getItemCount(): Int {
        return columns.size
    }

    override fun getItemId(position: Int): Long {
        return columns[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return columns.find { it.hashCode().toLong() == itemId } != null
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    override fun createFragment(position: Int): Fragment {
        val subject = columns[position].first
        val query = columns[position].second

        if(query.isBlank()) return Fragment()

        return when(subject) {
            Subject.ACCOUNTS ->
                if (search?.accounts.isNullOrEmpty()) NoMatchesFragment()
                else AccountSearchFragment.newInstance(credential, query, search!!.accounts)

            Subject.STATUSES ->
                if (search?.statuses.isNullOrEmpty()) NoMatchesFragment()
                else StatusSearchFragment.newInstance(credential, query, search!!.statuses)

            Subject.HASHTAGS ->
                if (search?.hashtags.isNullOrEmpty()) NoMatchesFragment()
                else HashtagSearchFragment.newInstance(credential, query, search!!.hashtags)
        }
    }

    fun update(query: String, search: Search) {
        this.search = search

        val newList = listOf(
            Pair(Subject.STATUSES, query),
            Pair(Subject.ACCOUNTS, query),
            Pair(Subject.HASHTAGS, query),
        )

        DiffUtil.calculateDiff(Diff(columns, newList)).apply {
            columns.clear()
            columns.addAll(newList)
        }.dispatchUpdatesTo(this)
    }

    /**
     * アクティビティとフラグメント両方に再生成かかると
     * なんかfragmentsに同じカラム（古いやつ）が入っちゃうぽいので
     * findLastで一番新しいfragmentを拾ってくる(この実装が良いかはまた別)
     */
    override fun getFragment(position: Int): Fragment? {
        val clazz = when(position) {
            0 -> StatusSearchFragment::class.java
            1 -> AccountSearchFragment::class.java
            else -> HashtagSearchFragment::class.java
        }

        return activity.supportFragmentManager.fragments.findLast {
            it.javaClass == clazz
        }
    }

    inner class TabConfig : TabLayoutMediator.TabConfigurationStrategy {
        private val subjects = Subject.values().toList()
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
            val icon = when (subjects[position]) {
                Subject.STATUSES -> R.drawable.search
                Subject.ACCOUNTS -> R.drawable.user
                Subject.HASHTAGS -> R.drawable.hashtag
            }

            val customView = TabTimelineBinding.inflate(activity.layoutInflater).also { binding ->
                binding.icon.setImageResource(icon)

                val color = credential.accentColor

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
        private val oldList: List<Pair<*,*>>,
        private val newList: List<Pair<*,*>>
        ): DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }
        override fun getNewListSize(): Int {
            return newList.size
        }
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first == newList[newItemPosition].first
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    enum class Subject {
        STATUSES,
        ACCOUNTS,
        HASHTAGS,
    }
}