package sns.asteroid.view.fragment.recyclerview

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Notification
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.activity.NotificationDetailActivity
import sns.asteroid.view.adapter.timeline.EventsListener
import sns.asteroid.view.adapter.timeline.NotificationAdapter
import sns.asteroid.viewmodel.AnnouncementsViewModel
import sns.asteroid.viewmodel.recyclerview.NotificationViewModel

class NotificationFragment:
    RecyclerViewFragment<Notification>(),
    MenuProvider,
    EventsListener,
    NotificationAdapter.NotificationEventListener
{
    override val viewModel: NotificationViewModel by viewModels {
        val column = requireArguments().getSerializable("column") as ColumnInfo
        val credential = requireArguments().getSerializable("credential") as Credential
        NotificationViewModel.Factory(column, credential)
    }

    override val recyclerViewAdapter: NotificationAdapter by lazy {
        NotificationAdapter(requireContext(), listener = this, notificationListener = this)
    }
    override val title by lazy { getString(R.string.column_notifications) }

    override val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope

    private val announcementsViewModel: AnnouncementsViewModel by viewModels {
        val credential = requireArguments().getSerializable("credential") as Credential
        AnnouncementsViewModel.Factory(credential)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        announcementsViewModel.announcements.observe(viewLifecycleOwner, Observer {
            binding.appBarLayout.toolbar.invalidateMenu()
            binding.messageBar.textView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT).trimEnd()
            binding.messageBar.textView.movementMethod = LinkMovementMethod()
        })

        startStreaming()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)

        menu.findItem(R.id.announcements).apply {
            iconTintList = let {
                val accentColor = viewModel.credential.value!!.accentColor
                val selectorArray = arrayOf(intArrayOf(0))
                val colorArray = intArrayOf(accentColor)
                ColorStateList(selectorArray, colorArray)
            }
            isVisible = announcementsViewModel.announcements.value?.isEmpty() == false
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.announcements -> { binding.messageBar.root.isVisible = !binding.messageBar.root.isVisible }
            else -> return super.onMenuItemSelected(item)
        }
        return true
    }

    override fun onFragmentShow() {
        lifecycleScope.launch { viewModel.reloadCredential() }
        if(!viewModel.isLoaded or !viewModel.streamingClient.isConnecting()) {
            lifecycleScope.launch {
                resumeStreaming()
                loadLatest()
                getAnnouncements()
            }
        }
    }

    override fun onFollowRequestAccept(account: Account) {
        lifecycleScope.launch { viewModel.acceptFollowRequest(account) }
    }

    override fun onFollowRequestReject(account: Account) {
        lifecycleScope.launch { viewModel.rejectFollowRequest(account) }
    }

    override fun onNotificationSelect(notification: Notification) {
        Intent(context, NotificationDetailActivity::class.java).apply {
            putExtra("credential", viewModel.credential.value)
            putExtra("notification", notification)
        }.run {
            startActivity(this)
        }
    }

    override fun onAccountClick(acct: String) {
        super<EventsListener>.onAccountClick(acct)
    }
    override fun onAccountClick(account: Account) {
        super<EventsListener>.onAccountClick(account)
    }

    private fun startStreaming() {
        lifecycleScope.launch { viewModel.startStreaming() }
    }

    private fun resumeStreaming() {
        lifecycleScope.launch { viewModel.resumeStreaming() }
    }

    private fun getAnnouncements() {
        lifecycleScope.launch { announcementsViewModel.getAll() }
    }

    companion object {
        @JvmStatic
        fun newInstance(data: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false): NotificationFragment {
            return NotificationFragment().apply {
                data?.let {
                    arguments = Bundle().apply {
                        putSerializable("column", data.first)
                        putSerializable("credential", data.second)
                        putSerializable("show_add_menu", showAddMenu)
                    }
                }
            }
        }
    }
}