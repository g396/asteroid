package sns.asteroid.view.fragment.recyclerview

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.ContentInterface
import sns.asteroid.api.entities.Tag
import sns.asteroid.databinding.FragmentTimelineBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.view.activity.SingleTimelineActivity
import sns.asteroid.view.activity.UserDetailActivity
import sns.asteroid.view.adapter.other_entities.AccountsAdapter
import sns.asteroid.view.adapter.other_entities.TagAdapter
import sns.asteroid.view.adapter.timeline.TimelineFooterAdapter
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.viewmodel.recyclerview.RecyclerViewViewModel

/**
 * タイムラインを表示するFragmentの共通部分を定義する
 *  (ViewBindingのオブジェクトに依存するもの)
 *  ViewBindingに依存しない場合はBaseFragmentを優先する
 */
abstract class RecyclerViewFragment<T: ContentInterface>:
    Fragment(),
    MenuProvider,
    FragmentShowObserver,
    TimelineFooterAdapter.OnClickListener,
    AccountsAdapter.AccountsAdapterListener,
    TagAdapter.OnHashtagSelectListener,
    Scroll
{
    abstract val viewModel: RecyclerViewViewModel<T>

    private var _binding: FragmentTimelineBinding? = null
    protected val binding get() = _binding!!

    abstract val recyclerViewAdapter: ListAdapter<T, *>
    abstract val title: String

    fun getColumnHash(): String {
        val column = requireArguments().get("column") as ColumnInfo
        return column.hash
    }

    /**
     * Viewの設定
     * ViewModelの値の変更通知の設定
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)

        binding.appBarLayout.also {
            it.title = title
            it.toolbar.setOnClickListener { scrollToTop() }
            it.toolbarIcon.setOnClickListener { onAccountClick(viewModel.credential.value!!.acct) }
            it.toolbar.visibility =
                if (requireArguments().getBoolean("hide_header", false)) View.GONE
                else View.VISIBLE
            it.toolbar.addMenuProvider(this)
        }
        binding.recyclerView.also {
            val concatAdapter = ConcatAdapter().apply {
                addAdapter(recyclerViewAdapter)
                addAdapter(TimelineFooterAdapter(requireContext(), this@RecyclerViewFragment))
            }
            it.adapter = concatAdapter
            it.layoutManager = LinearLayoutManager(requireContext())

            it.setHasFixedSize(true)
            it.itemAnimator = object: DefaultItemAnimator(){}.apply { supportsChangeAnimations = false }

            registerForContextMenu(it)
        }
        binding.refresh.also {
            it.setOnRefreshListener { loadLatest() }
        }
        binding.messageBar.imageButton.also {
            it.setOnClickListener { binding.messageBar.root.visibility = View.GONE }
        }

        viewModel.credential.observe(viewLifecycleOwner, Observer {
            val tab = binding.appBarLayout
            tab.avatarUrl = it.avatarStatic
            tab.acct = it.acct
            tab.color = it.accentColor
            tab.toolbar.invalidateMenu()
        })
        viewModel.contents.observe(viewLifecycleOwner, Observer {
            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
            val oldPosition = layoutManager.findFirstVisibleItemPosition()

            val oldSize = recyclerViewAdapter.currentList.size
            val newSize = it.size

            val commitCallback = Runnable {
                if((oldPosition < 1) and (oldSize < newSize)) binding.recyclerView.scrollToPosition(0)
            }
            recyclerViewAdapter.submitList(it, commitCallback)
        })
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()) return@Observer
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.toastMessage.postValue("")
        })

        return binding.root
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * MenuProviderの実装
     * ヘッダー右上のメニューをセットする
     * (今のところはリロードボタンしか置いてないのでメニューは表示されない)
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu).also {
            menu.findItem(R.id.action_streaming).apply { isVisible = false }

            menu.findItem(R.id.add_column).apply {
                isVisible = requireArguments().getBoolean("show_add_menu", false)
            }
            menu.findItem(R.id.action_refresh).apply {
                this.iconTintList = let {
                    val accentColor = viewModel.credential.value!!.accentColor
                    val selectorArray = arrayOf(intArrayOf(0))
                    val colorArray = intArrayOf(accentColor)
                    ColorStateList(selectorArray, colorArray)
                }
            }
        }
    }

    /**
     * MenuProviderの実装
     * ヘッダーのリロードボタンが押されたらタイムラインをリロードする
     */
    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add_column -> addColumn()
            R.id.action_refresh -> reload()
        }
        return true
    }

    /**
     * FragmentShowObserverの実装
     * ViewPagerでoffScreenPageLimitを設定していると
     * onResume()の呼出タイミングが画面表示毎で無くなってしまうので
     * 親Activityからこのメソッドを呼出しすることで代替する
     */
    override fun onFragmentShow() {
        lifecycleScope.launch { viewModel.reloadCredential() }
        if(!viewModel.isLoaded) loadLatest()
    }

    /**
     * TimelineFooterAdapter.OnClickListenerの実装
     * 古い投稿の読み込み中にローディングのマークを表示する
     */
    override fun onReadMoreClick(progressBar: ProgressBar, button: Button) {
        lifecycleScope.launch {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.getOlderContents()
            progressBar.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }

    /**
     * Scrollの実装
     * 親Activityで
     * タブを2回押すなどして先頭へスクロールしたい時に使用
     */
    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }



    /**
     * AccountsAdapter.AccountsAdapterListenerの実装
     * ユーザのアイコンを押したら別画面でプロフィールを表示
     */
    override fun onAccountClick(account: Account) {
        val intent = Intent(context, UserDetailActivity::class.java).apply {
            putExtra("account", account)
            putExtra("acct", account.acct)
            putExtra("data", viewModel.credential.value)
        }
        startActivity(intent)
    }

    /**
     * EventsListenerの実装
     * ユーザのアイコンを押したら別画面でプロフィールを表示
     */
    override fun onAccountClick(acct: String) {
        val intent = Intent(requireContext(), UserDetailActivity::class.java).apply {
            putExtra("acct", acct)
            putExtra("data", viewModel.credential.value)
        }
        startActivity(intent)
    }

    override fun onHashtagSelect(tag: Tag) {
        val hashtag = tag.name
        val credential = viewModel.credential.value!!
        val column = let {
            val subject = "hashtag"
            ColumnInfo(credential.acct, subject, hashtag, "#${hashtag}", -1)
        }
        val intent = Intent(requireContext(), SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    protected fun loadLatest() {
        lifecycleScope.launch {
            binding.refresh.isRefreshing = true
            viewModel.getLatestContents()
            binding.refresh.isRefreshing = false
        }
    }

    protected fun reload() {
        lifecycleScope.launch {
            binding.refresh.isRefreshing = true
            viewModel.reloadContents()
            binding.refresh.isRefreshing = false
        }
    }

    private fun addColumn() {
        lifecycleScope.launch { viewModel.addColumn() }
    }
}