package sns.asteroid.view.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.CustomEmoji
import sns.asteroid.databinding.ActivityTimelineBinding
import sns.asteroid.databinding.NavHeaderDrawerBinding
import sns.asteroid.model.emoji.CustomEmojiParser
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.view.adapter.pager.EmojiPagerAdapter
import sns.asteroid.view.adapter.pager.TimelinePagerAdapter
import sns.asteroid.view.adapter.spinner.DrawerCredentialAdapter
import sns.asteroid.view.adapter.spinner.VisibilityAdapter
import sns.asteroid.view.dialog.SimpleCheckBoxDialog
import sns.asteroid.view.fragment.emoji_selector.EmojiSelectorFragment
import sns.asteroid.view.fragment.recyclerview.RecyclerViewFragment
import sns.asteroid.view.fragment.FragmentShowObserver
import sns.asteroid.viewmodel.EmojiListViewModel
import sns.asteroid.viewmodel.TimelineActivityViewModel

/**
 * メイン画面
 * (スプラッシュ後に一番最初に出す画面)
 * カラムの一覧を表示
 */
class TimelineActivity:
    BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    TimelinePagerAdapter.PageChangeCallback,
    EmojiSelectorFragment.EmojiSelectorCallback
{
    private val viewModel: TimelineActivityViewModel by viewModels()
    private val emojiViewModel: EmojiListViewModel by viewModels()

    private val binding: ActivityTimelineBinding by lazy {
        ActivityTimelineBinding.inflate(layoutInflater)
    }
    private val headerDrawerBinding: NavHeaderDrawerBinding by lazy {
        NavHeaderDrawerBinding.inflate(layoutInflater)
    }

    private val adapter by lazy {
        TimelinePagerAdapter(this, binding.include.tabs, binding.include.viewPager)
    }
    private val drawerCredentialSpinner by lazy {
        DrawerCredentialAdapter(this, headerDrawerBinding)
    }
    private val visibilitySpinner by lazy {
        VisibilityAdapter(this, binding.include.postField.selectVisibility)
    }

    private var settingsValues = SettingsValues.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.credentials.observe(this@TimelineActivity, Observer {
            if (it.isEmpty())
                openAuthorizeActivity(isFirst = true)
            else
                drawerCredentialSpinner.submitList(it)
        })
        viewModel.columns.observe(this@TimelineActivity, Observer {
            adapter.update(it)

            binding.include.emojiSelector.root.visibility =
                if (it.isNotEmpty()) binding.include.emojiSelector.root.visibility
                else View.GONE

            setContentView(binding.root)
        })
        viewModel.selectedHashtag.observe(this@TimelineActivity, Observer {
            val color = if (it.isNotBlank()) getColor(R.color.blue) else (0xFF888888).toInt()
            binding.include.postField.hashtag.imageTintList = let {
                val selectorArray = arrayOf(intArrayOf(0))
                val colorArray = intArrayOf(color)
                ColorStateList(selectorArray, colorArray)
            }
        })

        emojiViewModel.emojiCategoryList.observe(this@TimelineActivity, Observer {
            EmojiPagerAdapter(this, binding.include.emojiSelector.emojiTab, binding.include.emojiSelector.emojiViewPager, it)
        })

        // for Data-binding
        binding.include.emojiSelector.viewModel = emojiViewModel

        binding.include.postField.send.apply {
            setOnClickListener(PostButtonClickListener())
        }
        binding.include.postField.selectVisibility.apply {
            adapter = visibilitySpinner
        }
        binding.include.postField.customEmoji.apply {
            setOnClickListener(EmojiButtonClickListener())
        }
        binding.include.postField.hashtag.apply {
            setOnClickListener(HashtagButtonClickListener())
        }
        binding.include.postField.content.apply {
            setOnEditorActionListener(EditorActionListener())
        }

        binding.include.floatingActionButton.apply {
            setOnClickListener(FloatingActionButtonClickListener())
        }
        binding.include.menuButton.apply {
            setOnClickListener { binding.drawerLayout.open() }
        }
        binding.navView.apply {
            addHeaderView(headerDrawerBinding.root)
            setNavigationItemSelectedListener(this@TimelineActivity)
        }

        headerDrawerBinding.icon.apply {
            setOnClickListener {
                val credential = headerDrawerBinding.credential ?: return@setOnClickListener
                openAccount(credential, credential.acct, null)
            }
        }

        onBackPressedDispatcher.addCallback(SystemBackKeyPressListener())
    }

    /**
     * 画面復帰毎に
     * 1.DB読込
     * 2.スリープ防止設定を確認して都度反映する
     * 3.各カラムのonFragmentShow()を呼び出す
     * (onResumeとかが非表示のカラムに対しても呼び出されるので)
     */
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.loadCredentials()
            viewModel.loadColumns()
            viewModel.loadHashtags()
        }

        settingsValues = SettingsValues.newInstance().also {
            if (it.isDisableSleep)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            binding.include.postField.content.imeOptions =
                if(it.sendWithEnterKey) EditorInfo.IME_ACTION_SEND
                else EditorInfo.IME_ACTION_DONE
            binding.include.postField.root.isVisible =
                it.isShowQuickPostArea and (resources.getInteger(R.integer.dynamic_visibility_h360)==0)
            binding.include.postField.selectVisibility.isVisible =
                it.isShowQuickPostVisibility
            binding.include.postField.customEmoji.isVisible =
                it.isShowQuickPostEmoji
            binding.include.postField.hashtag.isVisible =
                it.isShowQuickPostHashtag
            binding.include.tabs.tabMode =
                if(SettingsValues.getInstance().isStaticTabsWidth) TabLayout.MODE_AUTO
                else TabLayout.MODE_FIXED
        }

        // ストリーミング接続の自動再開に必要・・・
        if(adapter.itemCount > 0) {
            val fragment = adapter.getCurrentFragment() ?: return
            (fragment as? FragmentShowObserver)?.onFragmentShow()
        }
    }

    /**
     * 投稿の送信が完了した際に呼び出される
     * (投稿画面から戻ってきた場合も含む)
     */
    override fun onCreatePostsSuccess() {
        binding.include.postField.content.text = null
        binding.include.emojiSelector.root.visibility = View.GONE
    }

    /**
     * ドロワーのメニューを選択した際に呼び出される
     * ドロワーからタイムラインを開く際は
     * ドロワー上で参照中のアカウントに対するタイムラインを開く
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val credential = headerDrawerBinding.credential ?: return false

        when (item.itemId) {
            R.id.nav_manage_profile -> openEditProfile(credential)
            R.id.nav_manage_columns -> openManageColumns()
            R.id.nav_settings       -> openSettings()
            R.id.nav_lists          -> openLists(credential)
            R.id.nav_search         -> openSearch(credential)
            R.id.nav_trends         -> openTrends(credential)
            R.id.nav_manage_accounts-> openManageAccounts()
        }

        val subject = when (item.itemId) {
            R.id.nav_home           -> "home"
            R.id.nav_local          -> "local"
            R.id.nav_mix            -> "mix"
            R.id.nav_public         -> "public"
            R.id.nav_bookmarks      -> "bookmarks"
            R.id.nav_favourites     -> "favourites"
            R.id.nav_notifications  -> "notification"
            R.id.nav_reply          -> "mention"
            else -> return false
        }

        openSingleTimeline(credential, subject)
        return false
    }

    /**
     * ViewPager2で表示中のカラムが変わったら
     * (1) ドロワーに表示しているアカウントの表示をカラムに対応するものに切り替える
     * (2) 絵文字一覧をカラムに対応したサーバのものに切り替える(取得する)
     */
    override fun onPageChanged(position: Int) {
        val credential = viewModel.columns.value?.getOrNull(position)?.second
            ?: return

        drawerCredentialSpinner.setSelection(credential)

        if (binding.include.emojiSelector.root.isVisible)
            lifecycleScope.launch { emojiViewModel.getCustomEmojiCategories(credential.instance) }
    }

    /**
     * 絵文字セレクタで選択したカスタム絵文字のショートコードを
     * 投稿エリアに入力する
     */
    override fun onCustomEmojiSelect(emoji: CustomEmoji) {
        val editText = binding.include.postField.content
        CustomEmojiParser.inputEmojiToEditText(editText, emoji)
    }

    /**
     * 絵文字セレクタで選択したUnicode絵文字を
     * 投稿エリアに入力する
     */
    override fun onUnicodeEmojiSelect(unicodeString: String) {
        val editText = binding.include.postField.content
        CustomEmojiParser.inputEmojiToEditText(editText, unicodeString)
    }

    /**
     * 投稿ボタンを押した時
     *
     * 1.設定で有効になっていたら確認ダイアログを表示
     * (Ⅰ) はいを押したら投稿
     * (Ⅱ) キャンセルを押したら何もしない
     * (Ⅲ) 「次回以降表示しない」のチェックボックスを押したら設定を更新
     *
     * 2.ダイアログを表示しない設定になっていたらそのまま投稿
     */
    inner class PostButtonClickListener: OnClickListener, SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
        override fun onClick(v: View?) {
            if (binding.include.postField.content.text.isEmpty()) {
                Toast.makeText(this@TimelineActivity, R.string.empty, Toast.LENGTH_SHORT).show()
                return
            }

            val credential = adapter.getCurrentCredential() ?: return

            if (settingsValues.isDialogEnableOnPostStatus) {
                val title = String.format(getString(R.string.dialog_create_post), credential.acct)
                val checkBoxTitle = getString(R.string.dialog_show_never)

                SimpleCheckBoxDialog.newInstance(this, title, checkBoxTitle).show(supportFragmentManager, "tag")
            } else {
                postStatus()
            }
        }
        override fun onCheckedDialog(isChecked: Boolean) {
            settingsValues.isDialogEnableOnPostStatus = !isChecked
        }
        override fun onDialogAccept() {
            postStatus()
        }
        override fun onDialogCancel() {
        }

        private fun postStatus() {
            lifecycleScope.launch {
                binding.include.emojiSelector.root.visibility = View.GONE
                hideKeyboard()

                binding.include.postField.send.isClickable = false
                binding.include.progressBar.visibility = View.VISIBLE

                val text = binding.include.postField.content.text.toString().let {
                    if (!viewModel.selectedHashtag.value.isNullOrBlank()) it.plus(" #${viewModel.selectedHashtag.value}")
                    else it
                }
                val visibility = visibilitySpinner.getCurrentItem()

                val success = let {
                    val currentFragment = adapter.getCurrentFragment()
                    val vm = (currentFragment as? RecyclerViewFragment<*>)?.viewModel
                    vm?.postStatus(text, visibility) ?: false
                }

                binding.include.progressBar.visibility = View.INVISIBLE
                binding.include.postField.send.isClickable = true

                if(success) {
                    binding.include.postField.content.text = null
                    viewModel.loadHashtags()
                }
            }
        }
    }

    /**
     * 絵文字セレクタを展開or閉じるボタン
     * 展開したタイミングで一覧を読み込む
     */
    inner class EmojiButtonClickListener: OnClickListener {
        override fun onClick(v: View?) {
            binding.include.emojiSelector.root.visibility =
                if (binding.include.emojiSelector.root.visibility == View.VISIBLE) View.GONE
                else View.VISIBLE

            if (binding.include.emojiSelector.root.visibility == View.VISIBLE) lifecycleScope.launch {
                val credential = adapter.getCurrentCredential() ?: return@launch
                emojiViewModel.getCustomEmojiCategories(credential.instance)
            }
        }
    }

    inner class HashtagButtonClickListener: OnClickListener {
        override fun onClick(v: View?) {
            val popupMenu = PopupMenu(this@TimelineActivity, v)

            popupMenu.menu.apply {
                add(Menu.NONE, Menu.NONE+0, Menu.NONE+0, getString(R.string.manage))
                if (!viewModel.selectedHashtag.value.isNullOrEmpty()) {
                    add(Menu.NONE, Menu.NONE+2, Menu.NONE+2, "実況モードを解除")
                }
                viewModel.hashtags.value?.forEach {
                    add(Menu.NONE+1, Menu.NONE+1, Menu.NONE+1, it)
                }
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if(menuItem.itemId == Menu.NONE+2) {
                    viewModel.setSelectedHashtag("")
                    Toast.makeText(this@TimelineActivity, "実況モードを解除しました", Toast.LENGTH_SHORT).show()
                } else if(menuItem.itemId == Menu.NONE) {
                    val intent = Intent(this@TimelineActivity, RecentlyHashtagsActivity::class.java)
                    startActivity(intent)
                } else {
                    viewModel.setSelectedHashtag(menuItem.title.toString())
                    Toast.makeText(this@TimelineActivity, "実況モード #${menuItem.title}", Toast.LENGTH_SHORT).show()
                }
                false
            }
            popupMenu.show()
        }
    }

    /**
     * 新規投稿作成ボタン
     * 表示中のカラムに合わせてアカウントを指定して投稿画面を開く
     */
    inner class FloatingActionButtonClickListener: OnClickListener {
        override fun onClick(v: View?) {
            val credential = adapter.getCurrentCredential() ?: return
            val text = binding.include.postField.content.text.toString().let {
                if (!viewModel.selectedHashtag.value.isNullOrBlank()) it.plus(" #${viewModel.selectedHashtag.value}")
                else it
            }
            val visibility = visibilitySpinner.getCurrentItem()
            openCreatePostsActivity(credential, text, visibility)
        }

    }

    /**
     * ソフトウェアキーボードのEnterを送信ボタンに置換えた時に必要
     */
    inner class EditorActionListener: TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> hideKeyboard()
                EditorInfo.IME_ACTION_SEND -> PostButtonClickListener().onClick(binding.include.postField.send)
            }
            return true
        }
    }

    /**
     * 端末の戻るキーを押したら
     * 1.ドロワーや絵文字セレクタが展開されていたら閉じる
     * 2.そうでない場合
     * (1)一定秒数フラグを立てるro
     * (2)フラグが立ってる間にもう一度戻るキーを押したら、アプリ終了
     */
    inner class SystemBackKeyPressListener: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(binding.drawerLayout.isOpen) {
                binding.drawerLayout.close()
            }
            else if (binding.include.emojiSelector.root.visibility == View.VISIBLE) {
                binding.include.emojiSelector.root.visibility = View.GONE
            }
            else if (!viewModel.isStandbyClose) {
                Toast.makeText(this@TimelineActivity, getString(R.string.press_back_button_again), Toast.LENGTH_SHORT).show()
                lifecycleScope.launch { viewModel.standbyClose() }
            }
            else {
                finish()
            }
        }
    }
}