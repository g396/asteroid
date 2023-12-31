package sns.asteroid.view.fragment.recyclerview.timeline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.viewmodel.recyclerview.timeline.StatusesTrendsViewModel

class StatusesTrendsFragment: TimelineFragment() {
    override val viewModel: StatusesTrendsViewModel by viewModels {
        val columnInfo = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        StatusesTrendsViewModel.Factory(columnInfo, credential)
    }
    override val recyclerViewAdapter by lazy {
        val credential = requireArguments().get("credential") as Credential
        TimelineAdapter(requireContext(), credential.account_id, this, "search")
    }
    override val title by lazy { getString(R.string.column_trends) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): StatusesTrendsFragment {
            val bundle = Bundle().apply {
                putSerializable("column", column.first)
                putSerializable("credential",column.second)
                putBoolean("show_add_menu", showAddMenu)
            }
            return StatusesTrendsFragment().apply {
                arguments = bundle
            }
        }
    }
}