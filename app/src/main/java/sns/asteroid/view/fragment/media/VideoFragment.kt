package sns.asteroid.view.fragment.media

import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import sns.asteroid.R
import sns.asteroid.databinding.FragmentVideoBinding
import sns.asteroid.model.user.MediaModel

class VideoFragment : Fragment(), OnSeekBarChangeListener, OnPreparedListener,
    OnCompletionListener {
    private var videoSrc: String? = null
    private var _binding: FragmentVideoBinding? = null
    private var syncSeekBarJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    companion object {
        const val VIDEO_SOURCE = "video_src"

        @JvmStatic
        fun newInstance(src: String) = VideoFragment().apply {
            arguments = Bundle().apply { putString(VIDEO_SOURCE, src) }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoSrc = it.getString(VIDEO_SOURCE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)

        videoSrc?.let { binding.videoView.setVideoURI(it.toUri()) }

        binding.apply {
            progressBar.visibility = View.VISIBLE

            videoView.also {
                val mediaController = MediaController(this@VideoFragment.context)
                mediaController.setAnchorView(it)
                it.setOnPreparedListener(this@VideoFragment)
                it.setOnCompletionListener(this@VideoFragment)
            }

            seekBar.min = 0
            seekBar.setOnSeekBarChangeListener(this@VideoFragment)
        }
        binding.floatingActionButton.also {
            it.setOnClickListener { download() }
        }
        binding.button.setOnClickListener {
            if(binding.button.isChecked) {
                val position = try {
                    mediaPlayer?.currentPosition ?: 0
                } catch (e: IllegalStateException) {
                    0
                }
                binding.videoView.resume()
                binding.videoView.seekTo(position)
                binding.progressBar.visibility = View.VISIBLE
            }
            else {
                binding.videoView.pause()
            }
        }
        return binding.root
    }

    /**
     * ユーザがシークバーを操作したときに動画の再生位置を変更する
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(fromUser) binding.videoView.seekTo(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        syncSeekBarJob?.cancel()
        binding.videoView.stopPlayback()
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        binding.videoView.resume()
    }

    /**
     * 動画再生の準備ができたときに呼び出される
     */
    override fun onPrepared(mp: MediaPlayer?) {
        if (mp != null) {
            mediaPlayer = mp
        }
        // set VideoView's ratio
        ConstraintSet().apply {
            val ratio = "${mp?.videoWidth}:${mp?.videoHeight}"
            clone(binding.root)
            setDimensionRatio(binding.videoView.id, ratio)
            applyTo(binding.root)
        }

        binding.apply {
            progressBar.visibility = View.GONE
            button.visibility = View.VISIBLE
            button.isChecked = true
            mp?.videoWidth
            mp?.videoHeight
            seekBar.max = videoView.duration
            syncSeekBarJob = lifecycleScope.launch { syncSeekBar() }
            videoView.start()
        }
    }

    /**
     * 最後までいったときにループさせる
     */
    override fun onCompletion(mp: MediaPlayer?) {
        binding.apply {
            mp?.seekTo(0)
            mp?.start()
        }
    }

    /**
     * 再生位置をシークバーに反映させる処理
     * TODO: IllegalStateExceptionの原因をちゃんと特定したい
     */
    private suspend fun syncSeekBar() = withContext(Dispatchers.IO) {
        while (true) {
            Thread.sleep(50)
            binding.apply {
                try {
                    if(videoView.isPlaying) seekBar.progress = videoView.currentPosition
                } catch (_: java.lang.IllegalStateException) {
                }
            }
        }
    }

    private fun download() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = withContext(Dispatchers.IO) { MediaModel.download(videoSrc!!) }
            val toast =
                if (result) getString(R.string.saved) else getString(R.string.failed_to_save)
            Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }
}