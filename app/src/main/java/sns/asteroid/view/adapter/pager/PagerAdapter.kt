package sns.asteroid.view.adapter.pager

import androidx.fragment.app.Fragment

interface PagerAdapter {
    fun getFragment(position: Int): Fragment?
}