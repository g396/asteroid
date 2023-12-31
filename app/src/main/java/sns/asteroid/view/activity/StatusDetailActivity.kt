package sns.asteroid.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.ActivityStatusDetailBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.view.fragment.recyclerview.timeline.TimelineFragment
import sns.asteroid.viewmodel.recyclerview.timeline.StatusDetailViewModel

/**
 * 投稿を個別画面で表示する
 * 会話の履歴(返信)がある場合はそれを読み込んで表示する
 */
class StatusDetailActivity: AppCompatActivity() {
    val binding by lazy { ActivityStatusDetailBinding.inflate(layoutInflater) }

    private val status by lazy { intent.getSerializableExtra("status") as Status }
    private val credential by lazy { intent.getSerializableExtra("credential") as Credential }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(String.format(getString(R.string.title_detail_posts), status.id))

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 1
            }
            override fun createFragment(position: Int): Fragment {
                return StatusDetailFragment.newInstance(credential, status)
            }
        }
        binding.viewPager.isUserInputEnabled = false
    }

    // Fragmentはプライベートにしてはならない
    class StatusDetailFragment : TimelineFragment() {
        override val viewModel: StatusDetailViewModel by viewModels {
            val credential = requireArguments().get("credential") as Credential
            val status = (requireArguments().get("status") as Status).apply { isSelected = true }
            StatusDetailViewModel.Factory(credential, status)
        }

        override val recyclerViewAdapter by lazy {
            TimelineAdapter(
                context = requireContext(),
                myAccountId = viewModel.credential.value!!.account_id,
                listener = this@StatusDetailFragment,
                columnContext = "thread",
            )
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.refresh.isEnabled = false
            binding.recyclerView.adapter = recyclerViewAdapter
            lifecycleScope.launch { viewModel.getParentAndChildStatuses() }
        }

        companion object {
            @JvmStatic
            fun newInstance(credential: Credential, status: Status): StatusDetailFragment {
                val bundle = Bundle().apply {
                    putSerializable("credential", credential)
                    putSerializable("status", status)
                    putSerializable("hide_header", true)
                }
                return StatusDetailFragment().apply {
                    arguments = bundle
                }
            }
        }
    }
}