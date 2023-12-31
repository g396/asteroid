package sns.asteroid.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.databinding.DialogRecyclerViewBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsManageAccountsModel
import sns.asteroid.view.adapter.db.CredentialAdapter

class CredentialDialog: DialogFragment(), CredentialAdapter.ItemListener {
    private var _binding: DialogRecyclerViewBinding? = null
    private val binding get() = _binding!!

    var callback: CredentialSelectCallback? = null

    private val adapter by lazy { CredentialAdapter(requireContext(), this) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRecyclerViewBinding.inflate(layoutInflater)

        return Dialog(requireContext()).apply {
            binding.title = requireArguments().getString("title")

            binding.recyclerView.also {
                it.layoutManager = LinearLayoutManager(requireContext())
                it.adapter = adapter
            }

            setContentView(binding.root)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val credentials = withContext(Dispatchers.IO) { SettingsManageAccountsModel().getCredentials() }
            adapter.submitList(credentials)
        }
    }

    /**
     * 親Activityが再生成されると、空のコンストラクタでインスタンス再生成される為listenerがnullになる
     * →従ってコールバックできなくなる
     * そのような場合はダイアログ自体を閉じてしまう事にする
     */
    override fun onStart() {
        super.onStart()
        if(callback == null) dialog?.cancel()
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCredentialSelect(credential: Credential) {
        callback?.onCredentialSelect(credential)
        dismiss()
    }

    override fun onAvatarClick(credential: Credential) {
        callback?.onCredentialSelect(credential)
        dismiss()
    }

    override fun onMenuItemClick(credential: Credential, item: CredentialAdapter.Item) {
        // このダイアログではメニューを表示しない
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(callback: CredentialSelectCallback, title: String): CredentialDialog {
            val arg = Bundle().apply {
                putString("title", title)
            }
            return CredentialDialog().apply {
                this.arguments = arg
                this.callback = callback
            }
        }
    }

    interface CredentialSelectCallback{
        fun onCredentialSelect(credential: Credential)
    }
}