package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsAdapter
import sns.asteroid.viewmodel.recyclerview.AccountsViewModel

class SuggestionsFragment: RecyclerViewFragment<Account>() {
    override val viewModel: AccountsViewModel by viewModels {
        val credential = requireArguments().get("credential") as Credential
        val column = requireArguments().get("column") as ColumnInfo
        AccountsViewModel.Factory(column, credential)
    }

    override val recyclerViewAdapter by lazy {
        AccountsAdapter(requireContext(), this)
    }
    override val title by lazy { getString(R.string.column_suggestions) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refresh.isEnabled = false
        binding.recyclerView.adapter = recyclerViewAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(column: Pair<ColumnInfo, Credential>): SuggestionsFragment {
            val bundle = Bundle().apply {
                putSerializable("credential", column.second)
                putSerializable("column", column.first)
            }
            return SuggestionsFragment().apply {
                arguments = bundle
            }
        }
    }
}