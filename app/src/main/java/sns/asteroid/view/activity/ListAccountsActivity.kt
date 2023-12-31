package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.databinding.ActivityListAccountsBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsCheckableAdapter
import sns.asteroid.view.dialog.AddAccountToListDialog
import sns.asteroid.viewmodel.recyclerview.ListAccountViewModel

class ListAccountsActivity : AppCompatActivity(), AccountsCheckableAdapter.ItemSelectListener {
    private val viewModel: ListAccountViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        val columnInfo = ColumnInfo(credential.acct, "accounts_in_lists", -1)
        val listId = intent.getStringExtra("list_id") as String
        ListAccountViewModel.Factory(columnInfo, credential, listId)
    }

    val binding by lazy { ActivityListAccountsBinding.inflate(layoutInflater) }

    private val adapter by lazy {
        AccountsCheckableAdapter(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.contents.observe(this, Observer {
            val data = it.associateWith { true }
                .toList()
            val current = let {
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            adapter.submitList(data, Runnable {
                if(current <= 0) binding.recyclerView.scrollToPosition(0)
            })
        })
        viewModel.toastMessage.observe(this, Observer {
            if(it.isNotEmpty()){
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })

        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }
        binding.floatingActionButton.also {
            it.setOnClickListener {
                val credential = intent.getSerializableExtra("credential") as Credential
                val listId = intent.getStringExtra("list_id") as String
                AddAccountToListDialog.newInstance(credential, listId).show(supportFragmentManager, "tag")
            }
        }
        binding.includeAppbar.also {
            val credential = intent.getSerializableExtra("credential") as Credential
            it.acct = credential.acct
            it.avatarUrl = credential.avatarStatic
            it.color = credential.accentColor
            it.title = getString(R.string.title_accounts_list)
        }

        setContentView(binding.root)
        loadLatestUsers()
    }

    override fun onAccountClick(account: Account) {
        openUser(account)
    }

    override fun onAddButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton) {
    }

    override fun onRemoveButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.removeFromList(account)
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }

    private fun loadLatestUsers() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getLatestContents()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun openUser(account: Account) {
        val intent = Intent(this, UserDetailActivity::class.java).apply {
            putExtra("account", account)
            putExtra("acct", account.acct)
            putExtra("data", viewModel.credential.value)
        }
        startActivity(intent)
    }

}