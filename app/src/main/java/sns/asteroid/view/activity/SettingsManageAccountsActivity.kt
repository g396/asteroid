package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivitySettingsManageAccountsBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.SpaceAdapter
import sns.asteroid.view.adapter.db.CredentialAdapter
import sns.asteroid.view.adapter.sort.ItemDragCallback
import sns.asteroid.view.dialog.ColorPickDialog
import sns.asteroid.view.dialog.SimpleDialog
import sns.asteroid.viewmodel.SettingsManageAccountViewModel

class SettingsManageAccountsActivity:
    BaseActivity(),
    CredentialAdapter.ItemListener,
    ItemDragCallback.ItemMoveListener
{
    private lateinit var binding: ActivitySettingsManageAccountsBinding
    private val viewModel: SettingsManageAccountViewModel by viewModels()

    private val credentialAdapter: CredentialAdapter by lazy {
        val helperCallback = ItemDragCallback(this)
        val helper = ItemTouchHelper(helperCallback).apply {
            attachToRecyclerView(binding.recyclerView)
        }
        CredentialAdapter(this, this, itemTouchHelper = helper, isShowButton = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsManageAccountsBinding.inflate(layoutInflater)

        setTitle(R.string.title_settings_manage_accounts)
        setContentView(binding.root)

        viewModel.accounts.observe(this, Observer {
            val currentSize = credentialAdapter.currentList.size

            val commitCallback = Runnable {
                if(currentSize == 0) binding.recyclerView.scrollToPosition(0)
                else if(currentSize < it.size) binding.recyclerView.scrollToPosition(it.size-1)
            }
            credentialAdapter.submitList(it, commitCallback)
        })

        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = ConcatAdapter().also { concat ->
                concat.addAdapter(credentialAdapter)
                concat.addAdapter(SpaceAdapter(this, 1))
            }
        }
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AuthorizeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { viewModel.getCredentials() }
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        credentialAdapter.moveItem(fromPosition, toPosition)
        lifecycleScope.launch { viewModel.updateAll(credentialAdapter.currentList) }
    }

    override fun onCredentialSelect(credential: Credential) {
        showColorPicker(credential)
    }

    override fun onAvatarClick(credential: Credential) {
        openAccount(credential, credential.acct)
    }

    override fun onMenuItemClick(credential: Credential, item: CredentialAdapter.Item) {
        when(item) {
            CredentialAdapter.Item.MANAGE_PROFILE -> openEditProfile(credential)
            CredentialAdapter.Item.CHANGE_COLOR   -> showColorPicker(credential)
            CredentialAdapter.Item.REMOVE         -> showRemoveDialog(credential)
        }
    }

    private fun showRemoveDialog(credential: Credential) {
        val callback = object : SimpleDialog.SimpleDialogListener {
            override fun onDialogAccept() {
                lifecycleScope.launch {
                    viewModel.removeCredential(credential)
                    viewModel.getCredentials()
                }
            }
            override fun onDialogCancel() {
            }
        }
        val message = String.format(getString(R.string.dialog_remove_accounts), credential.acct)

        SimpleDialog.newInstance(callback, message).show(supportFragmentManager, "tag")
    }

    private fun showColorPicker(credential: Credential) {
        val callback = object : ColorPickDialog.ColorSelectCallback {
            override fun onColorSelect(colorCode: Int) {
                lifecycleScope.launch {
                    viewModel.changeAccentColor(credential, colorCode)
                    viewModel.getCredentials()
                }
            }
        }
        ColorPickDialog.newInstance(callback).show(supportFragmentManager, "tag")
    }
}