package sns.asteroid.view.fragment.media

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.R
import sns.asteroid.databinding.FragmentImageBinding
import sns.asteroid.model.user.MediaModel

/**
 * 画像の詳細ビュー
 */
class ImageFragment : Fragment(){
    private var imageSrc: String? = null
    private var _binding: FragmentImageBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageSrc = it.getString(IMAGE_SOURCE)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)

        val resourceReady = object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                binding.imageView.setImageDrawable(resource)
                binding.progressBar.visibility = View.GONE
            }
        }

        Glide.with(requireContext())
            .load(imageSrc)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.drawable.question)
            .into(resourceReady)

        binding.floatingActionButton.setOnClickListener {
            lifecycleScope.launch {
                // TODO: ViewModelつくる
                binding.progressBar.visibility = View.VISIBLE
                val result = withContext(Dispatchers.IO) { MediaModel.download(imageSrc!!) }
                val msg =
                    if(result) getString(R.string.saved)
                    else getString(R.string.failed_to_save)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }

        return binding.root
    }

    companion object {
        val IMAGE_SOURCE = "image_src"
        /**
         * @param src Image's URL
         * @return A new instance of fragment ImageFragment.
         */
        @JvmStatic
        fun newInstance(src: String) =
            ImageFragment().apply {
                arguments = Bundle().apply { putString(IMAGE_SOURCE, src) }
            }
    }
}