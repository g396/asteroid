package sns.asteroid.view.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import sns.asteroid.databinding.ActivitySingleTimelineBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.view.fragment.recyclerview.AccountListFragment
import sns.asteroid.view.fragment.recyclerview.*
import sns.asteroid.view.fragment.recyclerview.timeline.*

class SingleTimelineActivity : BaseActivity() {
    private val binding by lazy { ActivitySingleTimelineBinding.inflate(layoutInflater) }

    private val credential by lazy { intent.getSerializableExtra("credential") as Credential }
    private val adapter by lazy { TimelinePagerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewPager.also {
            it.adapter = adapter
            it.isUserInputEnabled = false
            it.registerOnPageChangeCallback(PageChangeCallback())
        }

        binding.floatingActionButton.apply {
            setOnClickListener { openCreatePostsActivity(credential) }
        }
    }

    inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val fragment = adapter.fragment
            (fragment as? FragmentShowObserver)?.onFragmentShow()
        }
    }

    inner class TimelinePagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
        var fragment: Fragment? = let {
            val fragments = activity.supportFragmentManager.fragments
            if (fragments.size > 0) fragments[0]
            else null
        }
        override fun getItemCount(): Int {
            return 1
        }

        @Suppress("MoveVariableDeclarationIntoWhen")
        override fun createFragment(position: Int): Fragment {
            val columnInfo =  intent.getSerializableExtra("column") as ColumnInfo
            val column = Pair(columnInfo, credential)

            return when(columnInfo.subject) {
                "notification" -> NotificationFragment.newInstance(column, true)
                "mention"   -> TimelineStreamingFragment.newInstance(column, true)
                "local"     -> TimelineStreamingFragment.newInstance(column, true)
                "public"    -> TimelineStreamingFragment.newInstance(column, true)
                "home"      -> TimelineStreamingFragment.newInstance(column, true)
                "mix"       -> TimelineStreamingFragment.newInstance(column, true)
                "list"      -> ListTimelineFragment.newInstance(column, true)
                "hashtag"   -> HashtagTimelineFragment.newInstance(column, true)
                "block"     ->  AccountListFragment.newInstance(column)
                "mute"      ->  AccountListFragment.newInstance(column)
                "favourited_by" -> AccountListFragment.newInstance(column)
                "reblogged_by" -> AccountListFragment.newInstance(column)
                else        -> TimelineFragment.newInstance(column, true)
            }.also {
                binding.floatingActionButton.visibility = View.VISIBLE
                fragment = it
            }
        }
    }
}