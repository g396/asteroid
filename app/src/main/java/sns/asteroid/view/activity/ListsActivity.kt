package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.ActivityListsBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.SpaceAdapter
import sns.asteroid.view.adapter.other_entities.ListTimelineAdapter
import sns.asteroid.view.dialog.SimpleDialog
import sns.asteroid.view.dialog.SimpleTextInputDialog
import sns.asteroid.viewmodel.ListsViewModel

class ListsActivity: BaseActivity(),
    OnClickListener,
    ListTimelineAdapter.ListTimelineAdapterListener,
    SimpleTextInputDialog.SimpleTextInputDialogListener {
    private lateinit var binding: ActivityListsBinding
    private val viewModel: ListsViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        ListsViewModel.Factory(credential)
    }
    private val adapter by lazy {
        val credential = intent.getSerializableExtra("credential") as Credential
        ListTimelineAdapter(this, credential, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            refresh.setOnRefreshListener { getAll() }
            recyclerView.also {
                it.adapter = ConcatAdapter().also { concat ->
                    concat.addAdapter(adapter)
                    concat.addAdapter(SpaceAdapter(this@ListsActivity, 1))
                }
                it.layoutManager = GridLayoutManager(this@ListsActivity, 1)
            }
            appBarLayout.also {
                it.avatarUrl = viewModel.credential.avatarStatic
                it.title = getString(R.string.column_list)
                it.acct = viewModel.credential.acct
                it.color = viewModel.credential.accentColor
                it.toolbarIcon.setOnClickListener { openAccount(viewModel.credential, viewModel.credential.acct, null) }
            }
        }

        binding.floatingActionButton.also {
            it.setOnClickListener(this)
        }

        viewModel.apply {
            lists.observe(this@ListsActivity, Observer {
                val currentSize = adapter.currentList.size

                val commitCallback = Runnable {
                    if(currentSize == 0) binding.recyclerView.scrollToPosition(0)
                    else if(currentSize < it.size) binding.recyclerView.scrollToPosition(it.size-1)
                }
                adapter.submitList(it, commitCallback)
            })
            toastMessage.observe(this@ListsActivity, Observer {
                Toast.makeText(this@ListsActivity, it, Toast.LENGTH_SHORT).show()
            })
        }

        getAll()
    }

    override fun onClick(v: View?) {
        val title = getString(R.string.dialog_create_a_new_list)
        val hint = getString(R.string.dialog_hint_list)
        SimpleTextInputDialog.newInstance(this, title, hint).show(supportFragmentManager, "tag")
    }

    override fun onListSelect(list: ListTimeline) {
        val intent = Intent(this, SingleTimelineActivity::class.java).apply {
            val column =
                ColumnInfo(viewModel.credential.acct, "list", list.id, list.title, -1)
            putExtra("column", column)
            putExtra("credential", viewModel.credential)
        }
        startActivity(intent)
    }

    override fun onDeleteButtonClick(list: ListTimeline) {
        val listener = object: SimpleDialog.SimpleDialogListener {
            override fun onDialogAccept() {
                lifecycleScope.launch { viewModel.deleteList(list.id) }
            }
            override fun onDialogCancel() {
            }
        }
        val title = String.format(getString(R.string.dialog_delete_list), list.title)
        SimpleDialog.newInstance(listener, title).show(supportFragmentManager, "tag")
    }

    override fun onUpdateButtonClick(list: ListTimeline) {
        val listener = object: SimpleTextInputDialog.SimpleTextInputDialogListener {
            override fun onInputText(text: String) {
                lifecycleScope.launch { viewModel.updateList(list.id, text) }
            }
            override fun onDialogCancel() {
            }
        }
        val title = "New title..."
        val hint = getString(R.string.dialog_hint_list)

        SimpleTextInputDialog.newInstance(listener, title, hint)
            .show(supportFragmentManager, "tag")
    }

    override fun onShowAccountsButtonClick(list: ListTimeline) {
        openListAccounts(viewModel.credential, list.id)
    }

    override fun onInputText(text: String) {
        if (text.isBlank()) {
            val toast = getString(R.string.empty)
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.createList(text)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDialogCancel() {
    }

    private fun getAll() {
        lifecycleScope.launch {
            binding.refresh.isRefreshing = true
            viewModel.getAll()
            binding.refresh.isRefreshing = false
        }
    }
}