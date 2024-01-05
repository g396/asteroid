package sns.asteroid.view.fragment.recyclerview.timeline

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
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
        super.onFragmentShow()
        if(!viewModel.streamingClient.isConnecting() and viewModel.enableStreaming)
            resumeStreaming()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menu.findItem(R.id.action_streaming).also {
            it.isVisible = true
            if (viewModel.enableStreaming) {
                it.isChecked = true
                it.setIcon(R.drawable.streaming_on)
            } else {
                it.isChecked = false
                it.setIcon(R.drawable.streaming_off)
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
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    item.setIcon(R.drawable.streaming_on)
                    startStreaming()
                } else {
                    item.setIcon(R.drawable.streaming_off)
                    stopStreaming()
                }
            }
            else -> super.onMenuItemSelected(item)
        }
        return true
    }

    private fun startStreaming() {
        Toast.makeText(requireContext(), R.string.streaming_connect, Toast.LENGTH_SHORT).show()
        lifecycleScope.launch { viewModel.startStreaming() }
    }

    private fun stopStreaming() {
        Toast.makeText(requireContext(), R.string.streaming_disconnect, Toast.LENGTH_SHORT).show()
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