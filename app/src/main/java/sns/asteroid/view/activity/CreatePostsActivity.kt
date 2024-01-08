package sns.asteroid.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.CustomEmoji
import sns.asteroid.api.entities.Status
import sns.asteroid.databinding.ActivityCreatePostsBinding
import sns.asteroid.databinding.RowMediaSquareBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.emoji.CustomEmojiParser
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.ISO639Lang
import sns.asteroid.view.adapter.pager.EmojiPagerAdapter
import sns.asteroid.view.adapter.poll.CreatePollAdapter
import sns.asteroid.view.adapter.spinner.LanguageAdapter
import sns.asteroid.view.adapter.spinner.VisibilityAdapter
import sns.asteroid.view.adapter.time.TimeSpinnerAdapter
import sns.asteroid.view.dialog.*
import sns.asteroid.view.fragment.emoji_selector.EmojiSelectorFragment
import sns.asteroid.viewmodel.CreatePostsViewModel
import sns.asteroid.viewmodel.EmojiListViewModel

/**
 * 投稿画面
 */
class CreatePostsActivity: AppCompatActivity(), EmojiSelectorFragment.EmojiSelectorCallback {
    private val viewModel: CreatePostsViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential?
        val replyTo = intent.getSerializableExtra("reply_to") as Status?
        val intentText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val visibility = intent.getStringExtra("visibility")
        CreatePostsViewModel.Factory(credential, replyTo, intentText, visibility)
    }
    private val emojiViewModel: EmojiListViewModel by viewModels()

    private val binding: ActivityCreatePostsBinding by lazy { ActivityCreatePostsBinding.inflate(layoutInflater) }

    private var editTextFocus: EditText? = null

    companion object {
        const val REQUEST_CODE_IMAGE = 20
        const val REQUEST_CODE_VIDEO = 30
        const val REQUEST_CODE_AUDIO = 40
    }

    /**
     * リスナの設定・ViewModelの設定等
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getContentFromIntent()
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.emojiViewModel = emojiViewModel
        binding.replyStatus = viewModel.replyTo

        viewModel.credential.observe(this, Observer {
            binding.credential = it
            lifecycleScope.launch { emojiViewModel.getCustomEmojiCategories(it.instance) }
        })
        viewModel.media.observe(this@CreatePostsActivity, Observer {
            binding.media = it
            (binding.images.adapter as MediaAdapter).submitList(it)
        })
        viewModel.language.observe(this@CreatePostsActivity, Observer {
            LanguageAdapter(this, binding.language, it)
        })
        viewModel.toastMessage.observe(this@CreatePostsActivity, Observer {
            Toast.makeText(this@CreatePostsActivity, it, Toast.LENGTH_SHORT).show()
        })
        emojiViewModel.emojiCategoryList.observe(this@CreatePostsActivity, Observer {
            EmojiPagerAdapter(this, binding.emojiTab, binding.emojiViewPager, it)
        })

        binding.apply {
            send.setOnClickListener { showDialog() }
            cw.setOnClickListener { showWarningText() }
            addImage.setOnClickListener(MediaButtonClickListener())
            hashtag.setOnClickListener(HashtagButtonClickListener())
            customEmoji.setOnClickListener { openEmojiSelector() }
            textArea.setOnClickListener { showKeyboard() }
            avatar.setOnClickListener { selectOtherAccount() }
        }

        binding.images.also {
            it.adapter = MediaAdapter(this@CreatePostsActivity)
            it.layoutManager = GridLayoutManager(this@CreatePostsActivity, 4)
        }
        binding.selectVisibility.also {
            it.adapter = VisibilityAdapter(this@CreatePostsActivity, it)
        }
        binding.includeCreatePoll.also { poll ->
            binding.poll.setOnClickListener {
                poll.root.visibility =
                    if (binding.poll.isChecked) View.VISIBLE
                    else View.GONE
            }
            poll.recyclerView.also {
                it.adapter = CreatePollAdapter(this@CreatePostsActivity)
                it.layoutManager = LinearLayoutManager(this@CreatePostsActivity)
            }
            poll.days.adapter = TimeSpinnerAdapter(this@CreatePostsActivity, viewModel.days)
            poll.hours.adapter = TimeSpinnerAdapter(this@CreatePostsActivity, viewModel.hours)
            poll.minutes.adapter = TimeSpinnerAdapter(this@CreatePostsActivity, viewModel.mins)
        }

        binding.content.addTextChangedListener(object: TextWatcher {
            private var isFirst = true
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                if (isFirst) {
                    isFirst = false
                    binding.content.setSelection(s.length)
                }
            }
        })

        showKeyboard()
        onBackPressedDispatcher.addCallback(BackKeyCallback())
    }

    /**
     * テキストボックスに対するフォーカスが変わった際に呼び出される
     * 絵文字パレットの検索用テキストボックスを、絵文字の入力対象から除外する為に
     * 直前のフォーカスがどのテキストボックスに当たっていたかを保持しておく必要がある
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        (currentFocus as? EditText)?.let { if(it != binding.includeSearchEmoji.query) editTextFocus = it }
    }

    /**
     * ファイルを選択して戻ってきた後
     * 添付ファイルのリストにファイルを追加する
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        lifecycleScope.launch {
            val uri = data?.data?: return@launch

            when(requestCode) {
                REQUEST_CODE_IMAGE -> viewModel.addMedia(CreatePostsViewModel.Property.IMAGE, uri)
                REQUEST_CODE_VIDEO -> viewModel.addMedia(CreatePostsViewModel.Property.VIDEO, uri)
                REQUEST_CODE_AUDIO -> viewModel.addMedia(CreatePostsViewModel.Property.AUDIO, uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * ファイル取得の権限を許可or拒否した後の処理
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@CreatePostsActivity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                return
            }
        }

        openIntentToAddMedia(requestCode)
    }

    /**
     * EmojiSelectorFragment.EmojiSelectorCallback
     * 絵文字パレットからカスタム絵文字のショートコードを入力する
     */
    override fun onCustomEmojiSelect(emoji: CustomEmoji) {
        val editText = (currentFocus as? EditText) ?: return

        if (editText != binding.includeSearchEmoji.query) {
            CustomEmojiParser.inputEmojiToEditText(editText, emoji)
        } else {
            editTextFocus?.let {
                it.requestFocus()
                CustomEmojiParser.inputEmojiToEditText(it, emoji)
            }
        }
    }

    /**
     * EmojiSelectorFragment.EmojiSelectorCallback
     * 絵文字パレットからUnicode絵文字を入力する
     */
    override fun onUnicodeEmojiSelect(unicodeString: String) {
        val editText = (currentFocus as? EditText) ?: return
        if (editText != binding.includeSearchEmoji.query) {
            CustomEmojiParser.inputEmojiToEditText(editText, unicodeString)
        } else {
            editTextFocus?.let {
                it.requestFocus()
                CustomEmojiParser.inputEmojiToEditText(it, unicodeString)
            }
        }
    }

    /**
     * MediaAdapterCallback
     * サムネイルクリック時にダイアログ表示→確認後に添付メディアを削除
     */
    private fun onRemoveButtonClick(position: Int, uri: Uri) {
        val listener = object : SimpleDialog.SimpleDialogListener {
            override fun onDialogAccept() {
                viewModel.removeImage(uri)
            }
            override fun onDialogCancel() {
            }
        }
        SimpleDialog.newInstance(listener, getString(R.string.dialog_remove_image))
            .show(supportFragmentManager, "tag")
    }

    private fun onThumbnailClick(position: Int, uri: Uri) {
        onRemoveButtonClick(position, uri)
    }

    private fun onDescriptionButtonClick(position: Int, uri: Uri) {
        val description = viewModel.getDescription(uri)
        val listener = object : SimpleTextInputDialog.SimpleTextInputDialogListener {
            override fun onInputText(text: String) {
                viewModel.addDescription(uri, text)
            }
            override fun onDialogCancel() {
            }
        }
        SimpleTextInputDialog.newInstance(
            listener,
            getString(R.string.dialog_description_title),
            getString(R.string.dialog_description_hint),
            description
        ).show(supportFragmentManager, "tag")
    }

    /**
     * メディアの選択画面を起動する
     */
    private fun openIntentToAddMedia(requestCode: Int) {
        val mimeType = when(requestCode) {
            REQUEST_CODE_IMAGE -> "image/*"
            REQUEST_CODE_VIDEO -> "video/*"
            REQUEST_CODE_AUDIO -> "audio/*"
            else -> "image/*"
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
            it.addCategory(Intent.CATEGORY_OPENABLE)
            it.type = mimeType
        }
        startActivityForResult(intent, requestCode)
    }

    /**
     * 警告文テキストボックスの表示・非表示
     */
    private fun showWarningText() {
        binding.spoilerText.visibility =
            if (binding.cw.isChecked) View.VISIBLE
            else View.GONE

        if (binding.spoilerText.visibility == View.VISIBLE) binding.spoilerText.requestFocus()
        else binding.content.requestFocus()
    }

    /**
     * 絵文字セレクタの表示・非表示
     */
    private fun openEmojiSelector() {
        binding.emojiSelector.apply {
            visibility =
                if (visibility == View.VISIBLE) View.GONE
                else View.VISIBLE

            if (visibility == View.VISIBLE)
                lifecycleScope.launch { emojiViewModel.getCustomEmojiCategories(viewModel.credential.value!!.instance) }
        }
    }

    /**
     * ソフトウェアキーボードを表示する
     * (onCreate()時とテキストエリアの範囲外を押した時)
     */
    private fun showKeyboard() {
        val editText = binding.content.also { editTextFocus = it }
        editText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, 0)
    }

    /**
     * 投稿ボタンを押した際に確認ダイアログを表示する
     * 設定で無効化している場合は飛ばして投稿する
     */
    private fun showDialog() {
        val settings = SettingsValues.getInstance()

        if (settings.isDialogEnableOnPostStatus) {
            val title = String.format(getString(R.string.dialog_create_post), viewModel.credential.value!!.acct)
            val checkBoxTitle = getString(R.string.dialog_show_never)

            val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
                override fun onCheckedDialog(isChecked: Boolean) {
                    settings.isDialogEnableOnPostStatus = !isChecked
                }
                override fun onDialogAccept() {
                    postStatus()
                }
                override fun onDialogCancel() {
                }
            }
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle).show(supportFragmentManager, "tag")
        } else {
            postStatus()
        }
    }

    /**
     * 投稿を送信する
     * TODO: 双方向データバインディングしたらこんなに記述いらなくなる
     */
    private fun postStatus() {
        lifecycleScope.launch {
            val button = binding.send.apply {
                isClickable = false
            }

            val loading = LoadingDialog().apply {
                show(supportFragmentManager, "tag")
            }

            val pollOption = if (binding.poll.isChecked) {
                val recyclerView = binding.includeCreatePoll.recyclerView
                val adapter = recyclerView.adapter as CreatePollAdapter
                adapter.getList()
            } else null

            val result = viewModel.postStatuses(pollOption)
            if (result) {
                setResult(RESULT_OK, Intent())
                finish()
            }

            button.isClickable = true
            loading.dismiss()
        }
    }

    /**
     * 投稿アカウントの切り替え用ダイアログを表示する
     * (返信時やメディアのアップロード試行後は不可)
     */
    private fun selectOtherAccount() {
        if (viewModel.replyTo != null) {
            Toast.makeText(this, getString(R.string.select_account_reply), Toast.LENGTH_SHORT).show()
            return
        } else if (viewModel.mediaAttachments.isNotEmpty()) {
            Toast.makeText(this, getString(R.string.select_account_media), Toast.LENGTH_SHORT).show()
            return
        }

        val callback = object : CredentialDialog.CredentialSelectCallback {
            override fun onCredentialSelect(credential: Credential) {
                if (credential == viewModel.credential.value) return
                else viewModel.selectOtherAccount(credential)
            }
        }

        CredentialDialog.newInstance(callback, title = getString(R.string.dialog_change_account))
            .show(supportFragmentManager, "tag")
    }

    /**
     * 画像・テキスト等をintentから受け取る
     */
    private fun getContentFromIntent() {
        // 共有機能から画像を受け取った場合
        if(intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri ?: return
            lifecycleScope.launch { viewModel.addMedia(CreatePostsViewModel.Property.IMAGE, uri) }
            return
        }
    }

    /**
     * 戻るボタンが押された際、内容が空でない場合は確認ダイアログを出す
     */
    inner class BackKeyCallback: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.emojiSelector.visibility == View.VISIBLE) {
                binding.emojiSelector.visibility = View.GONE
                return
            }
            val isNotEmpty = (viewModel.media.value!!.isNotEmpty()) or (binding.content.text.isNotBlank())

            if(isNotEmpty) {
                val listener = object : SimpleDialog.SimpleDialogListener {
                    override fun onDialogAccept() {
                        finish()
                    }
                    override fun onDialogCancel() {
                    }
                }
                SimpleDialog.newInstance(listener, getString(R.string.dialog_create_post_delete))
                    .show(supportFragmentManager, "tag")
            } else {
                finish()
            }
        }
    }


    inner class HashtagButtonClickListener(): OnClickListener {
        override fun onClick(v: View?) {
            val popupMenu = PopupMenu(this@CreatePostsActivity, v)

            popupMenu.menu.apply {
                add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.manage))
                viewModel.hashtags.value?.forEach {
                    add(Menu.NONE+1, Menu.NONE+1, Menu.NONE+1, it)
                }
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == Menu.NONE) {
                    val intent = Intent(this@CreatePostsActivity, RecentlyHashtagsActivity::class.java)
                    startActivity(intent)
                } else
                    inputHashtag(menuItem.title.toString())
                false
            }
            popupMenu.show()
        }

        private fun inputHashtag(hashtag: String) {
            val editText = (currentFocus as? EditText)

            val focus =
                if (editText != binding.includeSearchEmoji.query) editText
                else editTextFocus

            focus?.apply {
                val start = text.substring(0, selectionStart)
                val end = text.substring(selectionEnd, text.length)

                val shortCode = StringBuilder().let {
                    if(!start.endsWith(" ") and start.isNotEmpty()) it.append(" ")
                    it.append("#$hashtag")
                    if(!end.startsWith(" ") and end.isNotEmpty()) it.append(" ")
                    it.toString()
                }

                val str = StringBuilder().apply {
                    append(start)
                    append(shortCode)
                    append(end)
                    toString()
                }
                val current = selectionStart + shortCode.length
                setText(str.toString())

                try {
                    setSelection(current)
                } catch (e: IndexOutOfBoundsException) {
                    setSelection(text.length)
                }
            }
        }
    }

    /**
     * メディア添付用のボタンを押した際に呼び出す
     *
     * アップロード可能な個数の制限
     * (i) 画像は同時に4枚まで(Madtodon標準の仕様)
     * (ii) 動画・オーディオは1つまで
     * (iii) 画像・動画等、複数種類の混在は不可
     * (iv) 投票の併用は不可
     */
    inner class MediaButtonClickListener: OnClickListener {
        override fun onClick(v: View?) {
            if (viewModel.media.value!!.size >= 4) {
                Toast.makeText(this@CreatePostsActivity, getString(R.string.media_attachments_limit), Toast.LENGTH_SHORT).show()
                return
            }
            if (viewModel.property.value == CreatePostsViewModel.Property.VIDEO) {
                Toast.makeText(this@CreatePostsActivity, getString(R.string.media_xor), Toast.LENGTH_SHORT).show()
                return
            }
            if (viewModel.property.value == CreatePostsViewModel.Property.AUDIO) {
                Toast.makeText(this@CreatePostsActivity, getString(R.string.media_xor), Toast.LENGTH_SHORT).show()
                return
            }
            if(binding.poll.isChecked) {
                Toast.makeText(this@CreatePostsActivity, getString(R.string.media_poll_xor), Toast.LENGTH_SHORT).show()
                return
            }

            val popupMenu = PopupMenu(this@CreatePostsActivity, v)

            popupMenu.menu.apply {
                add(Menu.NONE, REQUEST_CODE_IMAGE, Menu.NONE, getString(R.string.context_img))

                if(viewModel.media.value!!.isEmpty()) {
                    add(Menu.NONE, REQUEST_CODE_VIDEO, Menu.NONE, getString(R.string.context_video))
                    add(Menu.NONE, REQUEST_CODE_AUDIO, Menu.NONE, getString(R.string.context_audio))
                }
            }
            popupMenu.setOnMenuItemClickListener {
                val requestCode = it.itemId
                checkPermissionAndStartIntent(requestCode)
                false
            }
            popupMenu.show()
        }
    }

    /**
     * ファイル選択時の権限を確認
     */
    private fun checkPermissionAndStartIntent(requestCode: Int) {
        val permissions =
            if (Build.VERSION.SDK_INT >= 33) mutableListOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
            ) else mutableListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

        val requests = mutableListOf<String>().apply {
            permissions.forEach {
                val permission = ContextCompat.checkSelfPermission(this@CreatePostsActivity, it)
                val isNotGranted = permission != PackageManager.PERMISSION_GRANTED
                if (isNotGranted) add(it)
            }
        }.toTypedArray()

        if (requests.isNotEmpty())
            ActivityCompat.requestPermissions(this@CreatePostsActivity, requests, requestCode)
        else
            openIntentToAddMedia(requestCode)
    }

    /**
     * 添付メディア用のアダプタクラス
     */
    inner class MediaAdapter(
        val context: Context,
    ): ListAdapter<Pair<Uri, Bitmap>, MediaAdapter.ViewHolder>(AdapterDiffUtil()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(context)
            val binding = RowMediaSquareBinding.inflate(layoutInflater)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val media = getItem(position)
            val binding = holder.binding

            binding.image.apply {
                setImageBitmap(media.second)
                setOnClickListener { onThumbnailClick(position, media.first) }
            }
            binding.button.apply {
                setOnClickListener { onRemoveButtonClick(position, media.first) }
            }
            binding.alt.apply {
                setOnClickListener { onDescriptionButtonClick(position, media.first) }
            }
        }

        inner class ViewHolder(val binding: RowMediaSquareBinding): RecyclerView.ViewHolder(binding.root)
    }

    inner class AdapterDiffUtil: DiffUtil.ItemCallback<Pair<Uri, Bitmap>>() {
        override fun areItemsTheSame(oldItem: Pair<Uri, Bitmap>, newItem: Pair<Uri, Bitmap>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<Uri, Bitmap>, newItem: Pair<Uri, Bitmap>): Boolean {
            return oldItem.first == newItem.first
        }
    }
}