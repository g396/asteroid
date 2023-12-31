package sns.asteroid.view.adapter.pager

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import sns.asteroid.R
import sns.asteroid.databinding.TabTimelineBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.fragment.recyclerview.AccountListFragment
import sns.asteroid.view.fragment.recyclerview.HashtagTrendsFragment
import sns.asteroid.view.fragment.recyclerview.timeline.StatusesTrendsFragment
import sns.asteroid.view.fragment.recyclerview.SuggestionsFragment
import sns.asteroid.view.fragment.FragmentShowObserver

class TrendsPagerAdapter(
    val activity: FragmentActivity,
    tab: TabLayout,
    viewPager2: ViewPager2,
    private val credential: Credential,
) : FragmentStateAdapter(activity), PagerAdapter {
    private var columns =
        if (credential.instance == "fedibird.com")
            mutableListOf(Subject.HASHTAGS, Subject.SUGGESTIONS, Subject.DIRECTORY)
        else
            mutableListOf(Subject.STATUSES, Subject.HASHTAGS, Subject.SUGGESTIONS, Subject.DIRECTORY)

    /**
     * ViewPager2でのページの切替検知用
     */
    private val pageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            (getFragment(position) as? FragmentShowObserver)?.onFragmentShow()
        }
    }

    init {
        viewPager2.also {
            it.adapter = this
            it.offscreenPageLimit = 4
            it.registerOnPageChangeCallback(pageChangeCallback)
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
        val column = ColumnInfo(credential.acct, columns[position].str,-1)
        return when(columns[position]) {
            Subject.STATUSES -> StatusesTrendsFragment.newInstance(Pair(column, credential), showAddMenu = true)
            Subject.HASHTAGS -> HashtagTrendsFragment.newInstance(Pair(column, credential), showAddMenu = true)
            Subject.DIRECTORY -> AccountListFragment.newInstance(Pair(column, credential))
            Subject.SUGGESTIONS -> SuggestionsFragment.newInstance(Pair(column, credential))
        }
    }

    /**
     * アクティビティとフラグメント両方に再生成かかると
     * なんかfragmentsに同じカラム（古いやつ）が入っちゃうぽいので
     * findLastで一番新しいfragmentを拾ってくる(この実装が良いかはまた別)
     */
    override fun getFragment(position: Int): Fragment? {
        val clazz = when (columns[position]) {
            Subject.STATUSES -> StatusesTrendsFragment::class.java
            Subject.HASHTAGS -> HashtagTrendsFragment::class.java
            Subject.DIRECTORY -> AccountListFragment::class.java
            Subject.SUGGESTIONS -> SuggestionsFragment::class.java
        }
        return activity.supportFragmentManager.fragments.findLast {
            it.javaClass == clazz
        }
    }

    inner class TabConfig : TabLayoutMediator.TabConfigurationStrategy {
        private val subjects = Subject.values().toList()
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
            val icon = when (subjects[position]) {
                Subject.STATUSES -> R.drawable.column_local
                Subject.HASHTAGS -> R.drawable.hashtag
                Subject.DIRECTORY -> R.drawable.directory
                Subject.SUGGESTIONS -> R.drawable.follow
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

    enum class Subject(val str: String) {
        STATUSES("trends_statuses"),
        HASHTAGS("trends_hashtags"),
        SUGGESTIONS("suggestions"),
        DIRECTORY("directory"),
    }
}