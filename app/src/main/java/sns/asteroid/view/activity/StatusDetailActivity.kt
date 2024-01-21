package sns.asteroid.view.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.ActivityStatusDetailBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.timeline.EventsListener
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.viewmodel.recyclerview.timeline.StatusDetailViewModel

/**
 * 投稿を個別画面で表示する
 * 会話の履歴(返信)がある場合はそれを読み込んで表示する
 */
class StatusDetailActivity: AppCompatActivity(), EventsListener  {
    val binding by lazy { ActivityStatusDetailBinding.inflate(layoutInflater) }

    val status by lazy {
        intent.getSerializableExtra("status") as Status
    }

    override val viewModel: StatusDetailViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        StatusDetailViewModel.Factory(credential, status)
    }
    override val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope

    val recyclerViewAdapter by lazy {
        TimelineAdapter(
            context = requireContext(),
            myAccountId = viewModel.credential.value!!.account_id,
            listener = this,
            columnContext = "thread",
        ).also {
            it.showActionButton(status.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(String.format(getString(R.string.title_detail_posts), status.id))

        binding.recyclerView.also {
            it.adapter = recyclerViewAdapter
            it.layoutManager = LinearLayoutManager(this)
            it.setHasFixedSize(true)
            it.itemAnimator = object: DefaultItemAnimator(){}.apply { supportsChangeAnimations = false }

            registerForContextMenu(it)
        }

        viewModel.contents.observe(this, Observer {
            recyclerViewAdapter.submitList(it)
        })

        lifecycleScope.launch {
            viewModel.getParentAndChildStatuses()
        }
    }

    override fun requireActivity(): FragmentActivity {
        return this
    }

    override fun requireContext(): Context {
        return this
    }
}