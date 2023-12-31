package sns.asteroid.view.fragment.emoji_selector

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.view.adapter.emoji.CustomEmojiAdapter

class CustomEmojiSelectorFragment: EmojiSelectorFragment() {
    private val settings = SettingsValues.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CustomEmojiAdapter(requireContext(), settings.isEnableEmojiAnimation, this)

        viewModel.customEmojis.observe(viewLifecycleOwner, Observer {
            val emojis = it.find { list -> list.category == getCategory() }
                ?: return@Observer

            binding.recyclerView.visibility =
                if (emojis.emojisList.isEmpty()) View.GONE
                else View.VISIBLE
            binding.noMatches.root.visibility =
                if (emojis.emojisList.isEmpty()) View.VISIBLE
                else View.GONE
            binding.loading.root.visibility = View.GONE

            adapter.submitList(emojis.emojisList)
        })

        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = FlexboxLayoutManager(requireContext())
        }
    }

    override fun loadEmojis() {
        lifecycleScope.launch { viewModel.getCustomEmojis(getDomain()!!) }
    }

    fun getDomain(): String? {
        return requireArguments().getString("domain")
    }

    fun getCategory(): String? {
        return requireArguments().getString("category")
    }

    companion object {
        @JvmStatic
        fun newInstance(domain: String, category: String): CustomEmojiSelectorFragment {
            val bundle = Bundle().apply {
                putString("domain", domain)
                putString("category", category)
            }
            return CustomEmojiSelectorFragment().apply {
                arguments = bundle
            }
        }
    }
}