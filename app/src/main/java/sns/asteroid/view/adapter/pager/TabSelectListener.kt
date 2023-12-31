package sns.asteroid.view.adapter.pager

import com.google.android.material.tabs.TabLayout
import sns.asteroid.view.fragment.recyclerview.Scroll

/**
 * タブを押した際に上へスクロールする為のリスナ
 */
class TabSelectListener(val adapter: PagerAdapter): TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        val position = tab?.position ?: return
        val fragment = adapter.getFragment(position)
        (fragment as? Scroll)?.scrollToTop()
    }
}