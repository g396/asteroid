package sns.asteroid.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sns.asteroid.api.entities.CustomEmoji
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.DialogEmojiReactionsBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.pager.EmojiPagerAdapter
import sns.asteroid.view.adapter.timeline.EventsListener
import sns.asteroid.view.fragment.emoji_selector.EmojiSelectorFragment
import sns.asteroid.viewmodel.EmojiListViewModel

class EmojiActionDialog : DialogFragment(), EmojiSelectorFragment.EmojiSelectorCallback {
    private val viewModel: EmojiListViewModel by viewModels()

    private var _binding: DialogEmojiReactionsBinding? = null
    val binding get() = _binding!!

    private var listener: EventsListener? = null

    val credential by lazy {
        requireArguments().getSerializable("credential") as Credential
    }
    val status by lazy {
        requireArguments().getSerializable("status") as Status
    }
    val hashcode by lazy {
        requireArguments().getInt("hashcode")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            val inflater = requireActivity().layoutInflater
            _binding = DialogEmojiReactionsBinding.inflate(inflater)

            binding.status.posts = status.reblog?: status
            binding.viewModel = viewModel

            setContentView(binding.root)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            lifecycleScope.launch {
                viewModel.getCustomEmojiCategories(credential.instance)
            }

            viewModel.emojiCategoryList.observe(requireActivity(), Observer {
                EmojiPagerAdapter(requireActivity(), binding.emojiTab, binding.emojiViewPager, it)
            })
        }
    }

    /**
     * 親Activityが再生成されると、空のコンストラクタでインスタンス再生成される為listenerがnullになる
     * →従ってコールバックできなくなる
     * そのような場合はダイアログ自体を閉じてしまう事にする
     */
    override fun onStart() {
        super.onStart()
        if(listener == null) dialog?.cancel()
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCustomEmojiSelect(emoji: CustomEmoji) {
        listener?.onEmojiButtonClick(status, true, emoji.shortcode)
        dismiss()
    }

    override fun onUnicodeEmojiSelect(unicodeString: String) {
        listener?.onEmojiButtonClick(status, true, unicodeString)
        dismiss()
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(eventsListener: EventsListener, credential: Credential, status: Status, hashCode: Int = 0): EmojiActionDialog {
            val bundle = Bundle().apply {
                putSerializable("credential", credential)
                putSerializable("status", status)
                putInt("hashcode", hashCode)
            }
            return EmojiActionDialog().apply {
                this.arguments = bundle
                this.listener = eventsListener
            }
        }
    }
}