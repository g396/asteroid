package sns.asteroid.view.fragment.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.R
import sns.asteroid.databinding.FragmentWebViewBinding
import sns.asteroid.model.user.MediaModel

class WebViewFragment : Fragment() {
    private var _binding: FragmentWebViewBinding? = null
    val binding: FragmentWebViewBinding get() = _binding!!

    private val url: String by lazy {
        requireArguments().getString("url")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebViewBinding.inflate(inflater)

        binding.webView.also {
            // 大きい画像を画面に合わせてリサイズ(2個ともいる)
            it.settings.loadWithOverviewMode = true
            it.settings.useWideViewPort = true

            // ズーム機能を有効
            it.settings.builtInZoomControls = true
            // ズームボタンを非表示
            it.settings.displayZoomControls = false

            it.loadUrl(url)
        }
        binding.floatingActionButton.also {
            it.setOnClickListener { download() }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }

    private fun download() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = withContext(Dispatchers.IO) { MediaModel.download(url) }
            val toast = if(result) getString(R.string.saved) else getString(R.string.failed_to_save)
            Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String): WebViewFragment {
            val bundle = Bundle().apply {
                putString("url", url)
            }

            return WebViewFragment().apply {
                arguments = bundle
            }
        }
    }
}