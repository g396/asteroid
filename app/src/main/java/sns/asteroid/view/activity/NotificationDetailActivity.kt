package sns.asteroid.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import sns.asteroid.R
import sns.asteroid.api.entities.Notification
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.ActivityNotificationDetailBinding
import sns.asteroid.databinding.FragmentNotificationDetailBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.other_entities.AccountsAdapter
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.view.fragment.recyclerview.timeline.TimelineFragment
import sns.asteroid.viewmodel.recyclerview.timeline.StatusDetailViewModel

/**
 * 通知の詳細を表示する
 *
 * 「Aさんと他◯◯人がブーストしました」のような通知を選択した際にこの画面を開き
 * リアクションしたユーザの一覧を合わせて表示する
 */
class NotificationDetailActivity: AppCompatActivity() {
    private lateinit var binding: ActivityNotificationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val credential = intent.getSerializableExtra("credential") as Credential
        val notification = intent.getSerializableExtra("notification") as Notification

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 1
            }
            override fun createFragment(position: Int): Fragment {
                return NotificationDetailFragment.newInstance(credential, notification)
            }
        }

        // 横スワイプ無効化
        binding.viewPager.isUserInputEnabled = false

        setTitle(R.string.title_detail_notification)
    }

    class NotificationDetailFragment : TimelineFragment() {
        override val viewModel: StatusDetailViewModel by viewModels {
            val credential = requireArguments().get("credential") as Credential
            val status = let {
                val notification = requireArguments().get("notification") as Notification
                notification.status!!
            }

            StatusDetailViewModel.Factory(credential, status)
        }

        override val recyclerViewAdapter by lazy {
            TimelineAdapter(
                context = requireContext(),
                myAccountId = viewModel.credential.value!!.account_id,
                listener = this@NotificationDetailFragment,
                columnContext = "notifications"
            ).also {
                val notification = requireArguments().get("notification") as Notification
                it.showActionButton(notification.status?.id)
            }
        }

        private var _binding: FragmentNotificationDetailBinding? = null
        private val nBinding get() = _binding!!

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentNotificationDetailBinding.inflate(layoutInflater)
            val binding = nBinding

            val notification = requireArguments().getSerializable("notification") as Notification

            binding.apply {
                account = notification.account
                accountCount = notification.otherAccount.size
                notificationType = notification.type

                notification.emoji_reaction?.let {
                    if (it.url.isNotEmpty())
                        binding.image = it.url
                    else
                        binding.unicodeEmoji = it.name
                }
            }

            binding.reactionUsers.apply {
                adapter = AccountsAdapter(context, this@NotificationDetailFragment).also {
                    val list = notification.otherAccount.toMutableList().ifEmpty { mutableListOf(notification.account) }
                    it.submitList(list)
                }
                layoutManager = LinearLayoutManager(context)
            }

            binding.status.also {
                it.adapter = recyclerViewAdapter
                it.layoutManager = LinearLayoutManager(context)
                it.itemAnimator = object: DefaultItemAnimator(){}.also { it.supportsChangeAnimations = false }
                registerForContextMenu(it)
            }

            viewModel.toastMessage.observe(viewLifecycleOwner, Observer {
                if(it.isEmpty()) return@Observer
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.toastMessage.postValue("")
            })
            viewModel.contents.observe(viewLifecycleOwner, Observer {
                recyclerViewAdapter.submitList(it)
            })

            return binding.root
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        companion object {
            @JvmStatic
            fun newInstance(credential: Credential, notification: Notification): NotificationDetailFragment {
                val bundle = Bundle().apply {
                    putSerializable("credential", credential)
                    putSerializable("notification", notification)
                }
                return NotificationDetailFragment().apply {
                    arguments = bundle
                }
            }
        }

    }

}