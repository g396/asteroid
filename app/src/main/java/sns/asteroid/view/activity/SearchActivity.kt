package sns.asteroid.view.activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sns.asteroid.databinding.ActivitySearchBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.pager.SearchPagerAdapter
import sns.asteroid.viewmodel.SearchViewModel

class SearchActivity: BaseActivity() {
    val binding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater).also {
            it.viewModel = viewModel
        }
    }

    val viewModel: SearchViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        SearchViewModel.Factory(credential)
    }

    val adapter by lazy {
        SearchPagerAdapter(
            this,
            binding.tabs,
            binding.viewPager,
            viewModel.credential,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.search.observe(this, Observer { search ->
            binding.viewPager.also {
                adapter.update(viewModel.query.value!!, search)
            }
        })

        binding.viewPager.also {
            it.adapter = adapter
        }

        binding.search.also {
            it.setOnClickListener {
                lifecycleScope.launch { searchAll() }
            }
        }

        binding.query.also {
            it.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    lifecycleScope.launch { searchAll() }
                }
                return@setOnEditorActionListener true
            }
        }

        binding.appbar.also {
            it.toolbarIcon.setOnClickListener { openAccount(viewModel.credential, viewModel.credential.acct, null) }
        }
    }

    private fun searchAll() {
        lifecycleScope.launch {
            hideKeyboard()
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getAll()
            binding.progressBar.visibility = View.GONE
        }
    }
}