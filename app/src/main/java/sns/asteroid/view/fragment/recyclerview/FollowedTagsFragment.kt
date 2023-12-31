package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import androidx.fragment.app.viewModels
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.TagAdapter
import sns.asteroid.viewmodel.recyclerview.FollowedTagsViewModel

class FollowedTagsFragment: RecyclerViewFragment<Tag>() {
    override val viewModel: FollowedTagsViewModel by viewModels {
        val columnInfo = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().getSerializable("credential") as Credential
        FollowedTagsViewModel.Factory(columnInfo, credential)
    }

    override val recyclerViewAdapter by lazy { TagAdapter(requireContext(), this) }
    override val title: String  = "Tag"

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): FollowedTagsFragment {
            val arg = Bundle().apply {
                putSerializable("column", column.first)
                putSerializable("credential", column.second)
                putBoolean("show_add_menu", showAddMenu)
                putBoolean("hide_header", true)
            }
            return FollowedTagsFragment().apply {
                arguments = arg
            }
        }
    }
}