package sns.asteroid.view.fragment.recyclerview.timeline

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.viewmodel.recyclerview.timeline.HashTagTimelineViewModel

class HashtagTimelineFragment: TimelineStreamingFragment() {
    override val viewModel: HashTagTimelineViewModel by viewModels {
        val column = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        HashTagTimelineViewModel.Factory(column, credential)
    }

    override fun onFragmentShow() {
        super.onFragmentShow()

        if(viewModel.tag.value == null)
            lifecycleScope.launch { viewModel.getTag() }
    }

    /**
     * MenuProviderの実装
     * ヘッダー右上のメニューにハッシュタグのフォロー/解除ボタンを追加
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater).also {
            viewModel.tag.observe(viewLifecycleOwner, Observer {
                menu.findItem(R.id.follow_tag).isVisible = !it.following
                menu.findItem(R.id.unfollow_tag).isVisible = it.following
            })
        }
    }

    /**
     * MenuProviderの実装
     * ハッシュタグフォロー/解除ボタンを有効にする
     */
    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.follow_tag     -> lifecycleScope.launch { viewModel.followTag() }
            R.id.unfollow_tag   -> lifecycleScope.launch { viewModel.unfollowTag() }
            else -> return super.onMenuItemSelected(item)
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): HashtagTimelineFragment {
            val arg = Bundle().apply {
                putSerializable("column", column.first)
                putSerializable("credential", column.second)
                putSerializable("show_add_menu", showAddMenu)
            }
            return HashtagTimelineFragment().apply {
                arguments = arg
            }
        }

    }
}