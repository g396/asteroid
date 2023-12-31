package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsAdapter
import sns.asteroid.viewmodel.recyclerview.AccountSearchViewModel

class AccountSearchFragment : RecyclerViewFragment<Account>() {
    override val viewModel: AccountSearchViewModel by viewModels {
        val credential = requireArguments().get("credential") as Credential
        val query = requireArguments().get("query") as String
        val accounts = requireArguments().get("values") as Array<Account>
        AccountSearchViewModel.Factory(credential, query, accounts.asList())
    }

    override val recyclerViewAdapter by lazy {
        AccountsAdapter(requireContext(), this)
    }
    override val title by lazy { getString(R.string.column_search) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(credential: Credential, query: String, values: List<Account>): AccountSearchFragment {
            val bundle = Bundle().apply {
                putSerializable("credential", credential)
                putSerializable("query", query)
                putSerializable("values", values.toTypedArray())
                putSerializable("hide_header", true)
            }
            return AccountSearchFragment().apply {
                arguments = bundle
            }
        }
    }
}