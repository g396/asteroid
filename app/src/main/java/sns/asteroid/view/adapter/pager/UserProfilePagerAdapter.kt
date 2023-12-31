package sns.asteroid.view.adapter.pager

import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.databinding.TabAccountBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.fragment.recyclerview.AccountListFragment
import sns.asteroid.view.fragment.recyclerview.FollowedTagsFragment
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.view.fragment.recyclerview.timeline.TimelineFragment

class UserProfilePagerAdapter(
    private val activity: FragmentActivity,
    tab: TabLayout,
    viewPager: ViewPager2,
    private val credential: Credential,
    private val account: Account,
): FragmentStateAdapter(activity), PagerAdapter {
    private var fragmentsHash = HashMap<Int, Int>() //Keyはカラムのハッシュ値 ValueはフラグメントのhashCode()

    private val list =
        if(credential.account_id == account.id) Subject.values().toList()
        else listOf(
            Subject.POSTS,
            Subject.MEDIA,
            Subject.FOLLOWING,
            Subject.FOLLOWERS,
        )

    init {
        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val fragment = getFragment(position) ?: return
                (fragment as? FragmentShowObserver)?.onFragmentShow()
            }
        }

        viewPager.also {
            it.adapter = this
            it.offscreenPageLimit = list.size
            it.registerOnPageChangeCallback(callback)
        }

        tab.also {
            it.addOnTabSelectedListener(TabSelectListener(this))
            it.setSelectedTabIndicatorColor(credential.accentColor)
            TabLayoutMediator(it, viewPager, TabConfig()).attach()
        }
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    override fun createFragment(position: Int): Fragment {
        val column = list[position]

        val columnInfo = ColumnInfo(
            credential.acct,
            column.value,
            account.id,
            account.display_name,
            -1,
        )

        return when (column) {
            Subject.POSTS -> TimelineFragment.newInstance(Pair(columnInfo, credential), hideHeader = true)
            Subject.MEDIA -> TimelineFragment.newInstance(Pair(columnInfo, credential), hideHeader = true)
            Subject.FOLLOWING -> AccountListFragment.newInstance(Pair(columnInfo, credential), hideHeader = true)
            Subject.FOLLOWERS -> AccountListFragment.newInstance(Pair(columnInfo, credential), hideHeader = true)
            Subject.FOLLOWED_TAGS -> FollowedTagsFragment.newInstance(Pair(columnInfo, credential))
        }.apply { fragmentsHash.put(position, hashCode()) }
    }

    override fun getFragment(position: Int): Fragment? {
        val hashCode = fragmentsHash[position] ?: return null
        return activity.supportFragmentManager.fragments.find { it.hashCode() == hashCode }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun getString(resId: Int): String {
        return activity.getString(resId)
    }

    inner class TabConfig: TabLayoutMediator.TabConfigurationStrategy {
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
            val subject = Subject.values()[position]

            val title = when(subject) {
                Subject.POSTS -> getString(R.string.title_posts)
                Subject.MEDIA -> getString(R.string.title_media)
                Subject.FOLLOWING -> getString(R.string.title_following)
                Subject.FOLLOWERS -> getString(R.string.title_followers)
                Subject.FOLLOWED_TAGS -> getString(R.string.title_following)
            }
            val iconResource = when(subject) {
                Subject.POSTS -> R.drawable.edit
                Subject.MEDIA -> R.drawable.image
                Subject.FOLLOWING -> R.drawable.follow
                Subject.FOLLOWERS -> R.drawable.relation
                Subject.FOLLOWED_TAGS -> R.drawable.hashtag
            }

            val customView = TabAccountBinding.inflate(activity.layoutInflater).apply {
                label.text = title
                icon.setImageResource(iconResource)
                icon.imageTintList = let {
                    val selectorArray = arrayOf(intArrayOf(0))
                    val colorArray = intArrayOf(credential.accentColor)
                    ColorStateList(selectorArray, colorArray)
                }
            }
            tab.customView = customView.root
            tab.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
        }
    }

    enum class Subject(val value: String) {
        POSTS("user_pin"),
        MEDIA("user_media"),
        FOLLOWING("following"),
        FOLLOWERS("followers"),
        FOLLOWED_TAGS("follow_tags"),
    }
}