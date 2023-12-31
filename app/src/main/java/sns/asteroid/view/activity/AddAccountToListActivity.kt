package sns.asteroid.view.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.ActivityAddAccountToListBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.ListTimelineCheckableAdapter
import sns.asteroid.viewmodel.AddAccountToListViewModel

class AddAccountToListActivity : AppCompatActivity(), ListTimelineCheckableAdapter.ItemSelectListener {
    val viewModel: AddAccountToListViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        val account = intent.getSerializableExtra("account") as Account
        AddAccountToListViewModel.Factory(credential, account.id)
    }
    val binding: ActivityAddAccountToListBinding by lazy {
        ActivityAddAccountToListBinding.inflate(layoutInflater)
    }

    val adapter: ListTimelineCheckableAdapter by lazy {
        val credential = intent.getSerializableExtra("credential") as Credential
        ListTimelineCheckableAdapter(this, this, credential.accentColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = intent.getSerializableExtra("account") as Account
        binding.user.setAccount(account)
        binding.user.note.maxLines = 1
        binding.user.note.ellipsize = TextUtils.TruncateAt.END

        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }

        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })

        lifecycleScope.launch {
            viewModel.getInAccount()
        }

        setTitle(R.string.menu_add_to_list)
        setContentView(binding.root)
    }

    override fun onAddButtonClick(list: ListTimeline, progressBar: ProgressBar, button: ToggleButton) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.addAccount(list)
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }

    override fun onRemoveButtonClick(list: ListTimeline, progressBar: ProgressBar, button: ToggleButton) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.removeAccount(list)
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }
}
