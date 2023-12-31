package sns.asteroid.view.fragment.recyclerview.timeline

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.activity.ListAccountsActivity
import sns.asteroid.viewmodel.recyclerview.timeline.ListTimelineViewModel

class ListTimelineFragment: TimelineStreamingFragment() {
    override val viewModel: ListTimelineViewModel by viewModels {
        val column = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        ListTimelineViewModel.Factory(column, credential)
    }

    /**
     * リストのタイトルの更新を確認する
     */
    override fun onFragmentShow() {
        if(!viewModel.isLoaded) lifecycleScope.launch { viewModel.getInfo() }
        super.onFragmentShow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.list.observe(viewLifecycleOwner, Observer {
            binding.appBarLayout.title = it.title
            binding.appBarLayout.toolbar.invalidateMenu()
        })
    }

    /**
     * MenuProviderの実装
     * アカウント一覧のショートカット追加
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater).also {
            menu.findItem(R.id.show_accounts_list).isVisible = true
            menu.findItem(R.id.replies_policy).isVisible = true

            viewModel.list.observe(viewLifecycleOwner, Observer {
                menu.findItem(R.id.exclusive_true).isVisible = (it.exclusive == false)
                menu.findItem(R.id.exclusive_false).isVisible = (it.exclusive == true)
            })
        }
    }

    /**
     * MenuProviderの実装
     */
    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.show_accounts_list -> openAccountsList()
            R.id.replies_policy -> manageRepliesPolicy()
            R.id.exclusive_true -> lifecycleScope.launch { viewModel.setExclusive(true) }
            R.id.exclusive_false -> lifecycleScope.launch { viewModel.setExclusive(false) }
            else -> return super.onMenuItemSelected(item)
        }
        return true
    }

    private fun openAccountsList() {
        val intent = Intent(context, ListAccountsActivity::class.java).apply {
            val credential = requireArguments().getSerializable("credential") as Credential
            val column = requireArguments().getSerializable("column") as ColumnInfo
            putExtra("credential", credential)
            putExtra("list_id", column.option_id)
        }
        startActivity(intent)
    }

    private fun manageRepliesPolicy() {
        val popupMenu = PopupMenu(requireContext(), binding.appBarLayout.toolbar)
            .apply { gravity = Gravity.END }

        popupMenu.menu.apply {
            add(Menu.NONE, Menu.FIRST + 0, Menu.FIRST + 0, getString(R.string.menu_replies_policy_none))
            add(Menu.NONE, Menu.FIRST + 1, Menu.FIRST + 1, getString(R.string.menu_replies_policy_list))
            add(Menu.NONE, Menu.FIRST + 2, Menu.FIRST + 2, getString(R.string.menu_replies_policy_followed))
        }
        popupMenu.setOnMenuItemClickListener {
            lifecycleScope.launch {
                when(it.itemId) {
                    Menu.FIRST + 0 -> viewModel.setRepliesPolicy("none")
                    Menu.FIRST + 1 -> viewModel.setRepliesPolicy("list")
                    Menu.FIRST + 2 -> viewModel.setRepliesPolicy("followed")
                }
            }
            false
        }
        popupMenu.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): ListTimelineFragment {
            val arg = Bundle().apply {
                putSerializable("column", column.first)
                putSerializable("credential", column.second)
                putSerializable("show_add_menu", showAddMenu)
            }
            return ListTimelineFragment().apply {
                arguments = arg
            }
        }
    }
}