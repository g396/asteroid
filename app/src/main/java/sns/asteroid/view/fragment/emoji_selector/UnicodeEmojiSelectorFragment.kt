package sns.asteroid.view.fragment.emoji_selector

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.view.adapter.emoji.UnicodeEmojiAdapter

class UnicodeEmojiSelectorFragment: EmojiSelectorFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = UnicodeEmojiAdapter(requireContext(), this)

        viewModel.unicodeEmojis.observe(viewLifecycleOwner, Observer {
            binding.recyclerView.visibility =
                if (it.isEmpty()) View.GONE
                else View.VISIBLE
            binding.noMatches.root.visibility =
                if (it.isEmpty()) View.VISIBLE
                else View.GONE
            binding.loading.root.visibility = View.GONE

            adapter.submitList(it)
        })

        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = FlexboxLayoutManager(context)
        }
    }

    override fun loadEmojis() {
        lifecycleScope.launch { viewModel.getUnicodeEmojis() }
    }

    companion object {
        @JvmStatic
        fun newInstance(category: String): UnicodeEmojiSelectorFragment {
            val bundle = Bundle().apply {
                putString("category", category)
            }
            return UnicodeEmojiSelectorFragment().apply {
                arguments = bundle
            }
        }
    }
}