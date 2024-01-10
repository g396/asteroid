package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityDraftBinding
import sns.asteroid.databinding.RowDraftBinding
import sns.asteroid.db.entities.Draft
import sns.asteroid.view.dialog.SimpleDialog
import sns.asteroid.viewmodel.DraftViewModel

class DraftActivity: AppCompatActivity() {
    val binding: ActivityDraftBinding by lazy { ActivityDraftBinding.inflate(layoutInflater) }
    val viewModel: DraftViewModel by viewModels()

    private val adapter by lazy { DraftAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_draft)
        setContentView(binding.root)

        viewModel.drafts.observe(this, Observer {
            adapter.submitList(it)
        })
        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }

        lifecycleScope.launch {
            viewModel.getAll()
        }
    }

    inner class RemoveDialogListener(private val position: Int): SimpleDialog.SimpleDialogListener {
        override fun onDialogAccept() {
            lifecycleScope.launch { viewModel.delete(position) }
        }
        override fun onDialogCancel() {
        }
    }

    inner class DraftAdapter: ListAdapter<Draft, ViewHolder>(Diff()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(this@DraftActivity)
            val binding = RowDraftBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val item = getItem(position)
            binding.draft = item

            binding.root.setOnClickListener {
                val intent = Intent().apply {
                    val index = currentList.indexOf(item)
                    putExtra("position", index)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
            binding.remove.setOnClickListener {
                val index = currentList.indexOf(item)
                val listener = RemoveDialogListener(index)
                SimpleDialog.newInstance(listener, getString(R.string.dialog_delete))
                    .show(supportFragmentManager, "tag")
            }
        }
    }

    inner class ViewHolder(val binding: RowDraftBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<Draft>() {
        override fun areItemsTheSame(oldItem: Draft, newItem: Draft): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Draft, newItem: Draft): Boolean {
            return oldItem == newItem
        }
    }
}