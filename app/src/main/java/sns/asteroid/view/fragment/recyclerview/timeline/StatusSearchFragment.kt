package sns.asteroid.view.fragment.recyclerview.timeline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.viewmodel.recyclerview.timeline.StatusSearchViewModel

class StatusSearchFragment : TimelineFragment() {
    override val viewModel: StatusSearchViewModel by viewModels {
        val credential = requireArguments().get("credential") as Credential
        val query = requireArguments().get("query") as String
        val statuses = requireArguments().get("values") as Array<Status>
        StatusSearchViewModel.Factory(credential, query, statuses.asList())
    }

    override val recyclerViewAdapter by lazy {
        val credential = requireArguments().get("credential") as Credential
        TimelineAdapter(requireContext(), credential.account_id, this, "search")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(credential: Credential, query: String, values: List<Status>): StatusSearchFragment {
            val bundle = Bundle().apply {
                putSerializable("credential", credential)
                putSerializable("query", query)
                putSerializable("values", values.toTypedArray())
                putSerializable("hide_header", true)
            }
            return StatusSearchFragment().apply {
                arguments = bundle
            }
        }
    }
}