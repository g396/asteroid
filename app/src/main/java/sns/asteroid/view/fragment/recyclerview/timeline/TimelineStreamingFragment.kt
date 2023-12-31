package sns.asteroid.view.fragment.recyclerview.timeline

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.viewModels
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.viewmodel.recyclerview.timeline.TimelineStreamingViewModel

open class TimelineStreamingFragment: TimelineFragment() {
    override val viewModel: TimelineStreamingViewModel by viewModels {
        val column = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        TimelineStreamingViewModel.Factory(column, credential)
    }

    override fun onFragmentShow() {
        lifecycleScope.launch { viewModel.reloadCredential() }

        if(!viewModel.isLoaded)
            startStreaming()
        if(!viewModel.isLoaded or !viewModel.streamingClient.isConnecting()) {
            lifecycleScope.launch {
                resumeStreaming()
                loadLatest()
            }
        }
    }

    /**
     * MenuProvider
     * リロードボタンが押された時に
     * ストリーミングも再接続したいので更にオーバーライド
     * (ちなみにストリーミング接続・切断ボタンは無効化中なので使わない)
     */
    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_refresh -> {
                resumeStreaming()
                reload()
            }
            R.id.action_streaming -> {
                if (item.isChecked) {
                    item.isChecked = false
                    item.setIcon(R.drawable.streaming_off)
                    stopStreaming()
                } else {
                    item.isChecked = true
                    item.setIcon(R.drawable.streaming_on)
                    startStreaming()
                }
            }
            else -> super.onMenuItemSelected(item)
        }
        return true
    }

    private fun startStreaming() {
        lifecycleScope.launch { viewModel.startStreaming() }
    }

    private fun stopStreaming() {
        lifecycleScope.launch { viewModel.stopStreaming() }
    }

    private fun resumeStreaming() {
        lifecycleScope.launch { viewModel.resumeStreaming() }
    }

    companion object {
        @JvmStatic
        fun newInstance(data: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): TimelineStreamingFragment {
            val bundle = Bundle().apply {
                putSerializable("column", data.first)
                putSerializable("credential", data.second)
                putSerializable("show_add_menu", showAddMenu)
            }
            return TimelineStreamingFragment().apply {
                arguments = bundle
            }
        }
    }
}