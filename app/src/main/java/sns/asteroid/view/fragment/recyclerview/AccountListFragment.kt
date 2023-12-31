package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsAdapter
import sns.asteroid.viewmodel.recyclerview.*

class AccountListFragment: RecyclerViewFragment<Account>() {
    override val viewModel: RecyclerViewViewModel<Account> by viewModels {
        val credential = requireArguments().getSerializable("credential") as Credential
        val columnInfo = requireArguments().getSerializable("column") as ColumnInfo
        AccountsViewModel.Factory(columnInfo, credential)
    }
    override val recyclerViewAdapter by lazy {
        AccountsAdapter(requireContext(), this)
    }
    override val title by lazy {
        val column = requireArguments().getSerializable("column") as ColumnInfo
        when(column.subject) {
            "block"         -> getString(R.string.title_block_list)
            "mute"          -> getString(R.string.title_mute_list)
            "directory"     -> getString(R.string.column_directory)
            "favourited_by" -> getString(R.string.column_favourited_by)
            "reblogged_by"  -> getString(R.string.column_reblogged_by)
            else -> column.option_title.ifBlank { "Unknown" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val column = requireArguments().getSerializable("column") as ColumnInfo
        if (column.subject == "directory") binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>, hideHeader: Boolean = false): AccountListFragment {
            val bundle = Bundle().apply {
                putSerializable("credential", column.second)
                putSerializable("column", column.first)
                putSerializable("hide_header", hideHeader)
            }
            return AccountListFragment().apply {
                arguments = bundle
            }
        }
    }
}