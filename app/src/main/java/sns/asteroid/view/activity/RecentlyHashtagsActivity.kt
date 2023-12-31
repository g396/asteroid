package sns.asteroid.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityRecentlyHashtagsBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.RecentlyHashtagsAdapter
import sns.asteroid.view.dialog.CredentialDialog
import sns.asteroid.view.dialog.HashtagInputDialog
import sns.asteroid.viewmodel.RecentlyHashtagsViewModel

class RecentlyHashtagsActivity:
    AppCompatActivity(),
    RecentlyHashtagsAdapter.OnHashtagSelectListener,
    HashtagInputDialog.HashtagDialogListener,
    CredentialDialog.CredentialSelectCallback,
    MenuProvider,
    OnClickListener {
    val viewModel: RecentlyHashtagsViewModel by viewModels()
    val binding: ActivityRecentlyHashtagsBinding by lazy {
         ActivityRecentlyHashtagsBinding.inflate(layoutInflater)
    }
    val adapter by lazy { RecentlyHashtagsAdapter(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addMenuProvider(this)
        setTitle(R.string.title_recently_hashtags)
        setContentView(binding.root)

        viewModel.hashtags.observe(this, Observer {
            adapter.submitList(it.toList())
        })
        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }
        binding.floatingActionButton.also {
            it.setOnClickListener(this)
        }
    }

    override fun onHashtagSelect(hashtag: String) {
    }

    override fun onRemoveButtonClick(hashtag: String) {
        lifecycleScope.launch { viewModel.remove(hashtag) }
    }

    override fun onClick(v: View?) {
        HashtagInputDialog.newInstance(this).show(supportFragmentManager, "tag")
    }

    override fun onInputHashtag(hashtag: String) {
        if (hashtag.isNotBlank()) lifecycleScope.launch { viewModel.add(hashtag) }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(getString(R.string.menu_import_hashtag))
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        CredentialDialog.newInstance(this, getString(R.string.dialog_change_account)).show(supportFragmentManager, "tag")
        return false
    }

    override fun onCredentialSelect(credential: Credential) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.importHashtags(credential)
            binding.progressBar.visibility = View.GONE
        }
    }
}