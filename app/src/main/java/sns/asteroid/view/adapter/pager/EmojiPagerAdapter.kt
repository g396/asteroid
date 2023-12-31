package sns.asteroid.view.adapter.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import sns.asteroid.databinding.TabTextBinding
import sns.asteroid.model.emoji.EmojiModel
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.view.fragment.emoji_selector.CustomEmojiSelectorFragment
import sns.asteroid.view.fragment.emoji_selector.UnicodeEmojiSelectorFragment

class EmojiPagerAdapter(
    private val activity: FragmentActivity,
    tab: TabLayout,
    viewPager2: ViewPager2,
    item: EmojiModel.EmojiCategoryList,
) : FragmentStateAdapter(activity) {
    private val categories = item.categorySet.toList()
    private val domain = item.domain

    private val pageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val fragment = getFragment(position) ?: return
            (fragment as? FragmentShowObserver)?.onFragmentShow()
        }
    }

    init {
        viewPager2.adapter = this
        viewPager2.unregisterOnPageChangeCallback(pageChangeCallback)
        viewPager2.registerOnPageChangeCallback(pageChangeCallback)

        TabLayoutMediator(tab, viewPager2, TabConfig()).attach()
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun getItemId(position: Int): Long {
        return categories[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return categories.find { it.hashCode().toLong() == itemId } != null
    }

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]
        return if (category == "Unicode Emojis") UnicodeEmojiSelectorFragment.newInstance(category)
        else CustomEmojiSelectorFragment.newInstance(domain, category)
    }

    private fun getFragment(position: Int): Fragment? {
        return if (categories.getOrNull(position) == "Unicode Emojis") activity.supportFragmentManager.fragments.findLast {
            it is UnicodeEmojiSelectorFragment
        } else activity.supportFragmentManager.fragments.findLast {
            val domain = (it as? CustomEmojiSelectorFragment)?.getDomain()
            val category = (it as? CustomEmojiSelectorFragment)?.getCategory()
            (domain == this.domain) and (category == categories.getOrNull(position))
        }
    }

    inner class TabConfig: TabLayoutMediator.TabConfigurationStrategy {
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
            tab.customView = TabTextBinding.inflate(activity.layoutInflater).also {
                it.textView.text = let {
                    val item = categories.getOrNull(position)
                    if (item == "Unicode Emojis") "Unicode"
                    else item
                }
            }.root
        }
    }
}