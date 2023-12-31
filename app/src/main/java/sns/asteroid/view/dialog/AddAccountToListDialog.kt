package sns.asteroid.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.databinding.DialogRecyclerViewBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsCheckableAdapter
import sns.asteroid.view.adapter.timeline.TimelineFooterAdapter
import sns.asteroid.viewmodel.recyclerview.AccountsViewModel
import sns.asteroid.viewmodel.recyclerview.ListAccountViewModel

class AddAccountToListDialog: DialogFragment(),
    AccountsCheckableAdapter.ItemSelectListener,
    TimelineFooterAdapter.OnClickListener
{
   private val listAccountViewModel: ListAccountViewModel by activityViewModels {
       val credential = requireArguments().getSerializable("credential") as Credential
       val columnInfo = ColumnInfo(credential.acct, "dummy", -1)
       val listId = requireArguments().getString("list_id") as String
       ListAccountViewModel.Factory(columnInfo, credential, listId)
   }
    private val followingViewModel: AccountsViewModel by viewModels {
        val credential = requireArguments().getSerializable("credential") as Credential
        val columnInfo = ColumnInfo(credential.acct, "following", credential.account_id, "dummy", -1)
        AccountsViewModel.Factory(columnInfo, credential)
    }
    private val adapter by lazy {
        AccountsCheckableAdapter(requireContext(), this)
    }
    private var _binding: DialogRecyclerViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRecyclerViewBinding.inflate(layoutInflater)

        return Dialog(requireContext()).apply {
            binding.recyclerView.also {
                it.adapter = ConcatAdapter().apply {
                    addAdapter(adapter)
                    addAdapter(TimelineFooterAdapter(requireContext(), this@AddAccountToListDialog))
                }
                it.layoutManager = LinearLayoutManager(requireContext())
            }
            binding.title = getString(R.string.title_add_to_list)

            setContentView(binding.root)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        followingViewModel.contents.observe(this, Observer { followings ->
            val inLists = listAccountViewModel.contents.value
                ?: return@Observer
            val data = followings.associateWith { inLists.contains(it) }
                .toList()

            val current = let {
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            adapter.submitList(data, Runnable {
                if(current <= 0) binding.recyclerView.scrollToPosition(0)
            })
        })
        listAccountViewModel.contents.observe(this, Observer { inLists ->
            val followings = followingViewModel.contents.value
                ?: return@Observer
            val data = followings.associateWith { inLists.contains(it) }
                .toList()

            val current = let {
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            adapter.submitList(data, Runnable {
                if(current <= 0) binding.recyclerView.scrollToPosition(0)
            })
        })

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            followingViewModel.getLatestContents()
            binding.progressBar.visibility = View.GONE
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAccountClick(account: Account) {
    }

    override fun onAddButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            listAccountViewModel.addToList(account)
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }

    override fun onRemoveButtonClick(account: Account, progressBar: ProgressBar, button: ToggleButton) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            listAccountViewModel.removeFromList(account)
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }

    override fun onReadMoreClick(progressBar: ProgressBar, button: Button) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            followingViewModel.getOlderContents()
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }
    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(credential: Credential, listId: String): AddAccountToListDialog {
            val arg = Bundle().apply {
                putSerializable("credential", credential)
                putString("list_id", listId)
            }
            return AddAccountToListDialog().apply {
                this.arguments = arg
            }
        }
    }
}