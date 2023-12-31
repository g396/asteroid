package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.TagAdapter
import sns.asteroid.viewmodel.recyclerview.HashtagTrendsViewModel

class HashtagTrendsFragment: RecyclerViewFragment<Tag>() {
    override val viewModel: HashtagTrendsViewModel by viewModels {
        val columnInfo = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        HashtagTrendsViewModel.Factory(columnInfo, credential)
    }
    override val recyclerViewAdapter by lazy { TagAdapter(requireContext(), this) }
    override val title by lazy { getString(R.string.column_trends) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): HashtagTrendsFragment {
            val bundle = Bundle().apply {
                putSerializable("column", column.first)
                putSerializable("credential", column.second)
                putBoolean("show_add_menu", showAddMenu)
            }
            return HashtagTrendsFragment().apply {
                arguments = bundle
            }
        }
    }
}