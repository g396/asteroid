package sns.asteroid.view.fragment.recyclerview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import sns.asteroid.R
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.TagAdapter
import sns.asteroid.viewmodel.recyclerview.HashtagSearchViewModel

class HashtagSearchFragment: RecyclerViewFragment<Tag>() {
    override val viewModel: HashtagSearchViewModel by viewModels {
        val credential = requireArguments().get("credential") as Credential
        val query = requireArguments().get("query") as String
        val tags = requireArguments().get("values") as Array<Tag>
        HashtagSearchViewModel.Factory(credential, query, tags.asList())
    }
    override val recyclerViewAdapter by lazy {
        TagAdapter(requireContext(), this@HashtagSearchFragment)
    }
    override val title by lazy { getString(R.string.column_search) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refresh.isEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(credential: Credential, query: String, values: List<Tag>): HashtagSearchFragment {
            val bundle = Bundle().apply {
                putSerializable("credential", credential)
                putSerializable("query", query)
                putSerializable("values", values.toTypedArray())
                putSerializable("hide_header", true)
            }
            return HashtagSearchFragment().apply {
                arguments = bundle
            }
        }
    }
}