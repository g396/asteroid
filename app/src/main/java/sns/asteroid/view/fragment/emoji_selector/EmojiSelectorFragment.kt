package sns.asteroid.view.fragment.emoji_selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import sns.asteroid.api.entities.CustomEmoji
import sns.asteroid.databinding.FragmentEmojiBinding
import sns.asteroid.view.adapter.emoji.EmojiAdapterCallback
import sns.asteroid.view.dialog.EmojiActionDialog
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.viewmodel.EmojiListViewModel

abstract class EmojiSelectorFragment: Fragment(), FragmentShowObserver, EmojiAdapterCallback {
    val viewModel: EmojiListViewModel by viewModels({
        parentFragmentManager.fragments.lastOrNull { fragment ->
            fragment is EmojiActionDialog
        } ?: requireActivity()
    })

    private var _binding: FragmentEmojiBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmojiBinding.inflate(inflater, container, false)

        viewModel.query.observe(viewLifecycleOwner, Observer {
            loadEmojis()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCustomEmojiSelect(customEmoji: CustomEmoji) {
        val fragment = parentFragmentManager.fragments.lastOrNull { fragment ->
            fragment is EmojiActionDialog
        }
        if (fragment is EmojiSelectorCallback) {
            fragment.onCustomEmojiSelect(customEmoji)
        } else if (activity is EmojiSelectorCallback) {
            (activity as EmojiSelectorCallback).onCustomEmojiSelect(customEmoji)
        }
    }

    override fun onUnicodeEmojiSelect(unicodeString: String) {
        val fragment = parentFragmentManager.fragments.lastOrNull { fragment ->
            fragment is EmojiActionDialog
        }
        if (fragment is EmojiSelectorCallback) {
            fragment.onUnicodeEmojiSelect(unicodeString)
        } else if (activity is EmojiSelectorCallback) {
            (activity as EmojiSelectorCallback).onUnicodeEmojiSelect(unicodeString)
        }
    }

    override fun onFragmentShow() {
        loadEmojis()
    }

    abstract fun loadEmojis()

    interface EmojiSelectorCallback {
        fun onCustomEmojiSelect(emoji: CustomEmoji)
        fun onUnicodeEmojiSelect(unicodeString: String)
    }
}