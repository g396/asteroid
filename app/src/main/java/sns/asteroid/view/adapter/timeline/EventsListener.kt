package sns.asteroid.view.adapter.timeline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.view.activity.*
import sns.asteroid.view.dialog.CredentialDialog
import sns.asteroid.view.dialog.EmojiActionDialog
import sns.asteroid.view.dialog.SimpleCheckBoxDialog
import sns.asteroid.viewmodel.recyclerview.StatusViewModelInterface

interface EventsListener {
    val viewModel: StatusViewModelInterface
    val lifecycleScope: LifecycleCoroutineScope
    fun requireActivity(): FragmentActivity
    fun requireContext(): Context
    fun startActivity(intent: Intent)
    fun getString(resId: Int): String

    /**
     * EventsListenerの実装
     * 別画面でメディアを表示する
     */
    fun onMediaClick(mediaAttachments: List<MediaAttachment>, index: Int) {
        val intent = Intent(requireContext(), MediaPreviewActivity::class.java).apply {
            putExtra(MediaPreviewActivity.STATUS, mediaAttachments.toTypedArray())
            putExtra("index", index)
        }
        startActivity(intent)
    }

    /**
     * EventsListenerの実装
     * 投稿をふぁぼる or ふぁぼ解除
     *
     * 投稿に対して絵文字リアクションボタンを表示する際は、ふぁぼボタンを省略し
     * 代わりにメニューの中に項目として表示するので
     * ふぁぼボタンのON/OFF切替は行わない
     */
    fun onFavouriteMenuSelect(status: Status) {
        val positive = !status.favourited

        val settings = SettingsValues.getInstance()

        val isDialogEnable =
            if(positive) settings.isDialogEnableOnFavourite
            else settings.isDialogEnableOnUndoFavourite

        val title =
            if(positive) getString(R.string.dialog_favourite)
            else getString(R.string.dialog_undo_favourite)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            lifecycleScope.launch {
                if (positive) viewModel.postFavourite(status.id)
                else viewModel.postUnFavourite(status.id)
            }
        }

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                if(positive)
                    settings.isDialogEnableOnFavourite = !isChecked
                else
                    settings.isDialogEnableOnUndoFavourite = !isChecked
            }
            override fun onDialogAccept() {
                exec()
            }
            override fun onDialogCancel() {
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            exec()
    }

    /**
     * 公開範囲を指定してブースト
     */
    fun onBoostMenuSelect(status: Status, visibility: Visibility) {
        val settings = SettingsValues.getInstance()

        val isDialogEnable = settings.isDialogEnableOnBoost
        val title = getString(R.string.dialog_reblog)
        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            when(visibility) {
                Visibility.PUBLIC -> lifecycleScope.launch { viewModel.postBoostPublic(status.id) }
                Visibility.UNLISTED -> lifecycleScope.launch { viewModel.postBoostUnlisted(status.id) }
                Visibility.PRIVATE -> lifecycleScope.launch { viewModel.postBoostPrivate(status.id) }
            }
        }

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                settings.isDialogEnableOnBoost = !isChecked
            }
            override fun onDialogAccept() {
                exec()
            }
            override fun onDialogCancel() {
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            exec()
    }

    /**
     * EventsListenerの実装
     * 投稿をふぁぼる or ふぁぼ解除
     * ダイアログでキャンセルした場合は、ふぁぼボタンの状態をもとに戻す
     */
    fun onFavouriteButtonClick(status: Status, button: ToggleButton) {
        val positive = button.isChecked
        val settings = SettingsValues.getInstance()

        val isDialogEnable =
            if(positive) settings.isDialogEnableOnFavourite
            else settings.isDialogEnableOnUndoFavourite

        val title =
            if(positive) getString(R.string.dialog_favourite)
            else getString(R.string.dialog_undo_favourite)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            lifecycleScope.launch {
                val result =
                    if (positive) viewModel.postFavourite(status.id)
                    else viewModel.postUnFavourite(status.id)
                if (!result)
                    button.isChecked = status.favourited
            }
        }

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                if(positive)
                    settings.isDialogEnableOnFavourite = !isChecked
                else
                    settings.isDialogEnableOnUndoFavourite = !isChecked
            }
            override fun onDialogAccept() {
                exec()
            }
            override fun onDialogCancel() {
                button.isChecked = status.favourited
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            exec()
    }

    /**
     * EventsListenerの実装
     * 投稿をブースト or ブースト解除
     * ダイアログでキャンセルした場合は、ブーストボタンの状態をもとに戻す
     */
    fun onBoostButtonClick(status: Status, button: ToggleButton) {
        val positive = button.isChecked
        val settings = SettingsValues.getInstance()

        val isDialogEnable =
            if(positive) settings.isDialogEnableOnBoost
            else settings.isDialogEnableOnUndoBoost

        val title =
            if(positive) getString(R.string.dialog_reblog)
            else getString(R.string.dialog_undo_reblog)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            lifecycleScope.launch {
                val result =
                    if (positive) viewModel.postBoost(status.id)
                    else viewModel.postUnBoost(status.id)
                if (!result)
                    button.isChecked = status.reblogged
            }
        }

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                if(positive)
                    settings.isDialogEnableOnBoost = !isChecked
                else
                    settings.isDialogEnableOnUndoBoost = !isChecked
            }
            override fun onDialogAccept() {
                exec()
            }
            override fun onDialogCancel() {
                button.isChecked = status.reblogged
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            exec()
    }

    /**
     * EventsListenerの実装
     * 投稿をブックマーク or ブックマーク解除
     * ダイアログでキャンセルした場合は、ブックマークボタンの状態をもとに戻す
     */
    fun onBookmarkButtonClick(status: Status, button: ToggleButton) {
        val positive = button.isChecked
        val settings = SettingsValues.getInstance()

        val isDialogEnable =
            if(positive) settings.isDialogEnableOnBookmark
            else settings.isDialogEnableOnUndoBookmark

        val title =
            if(positive) getString(R.string.dialog_bookmark)
            else getString(R.string.dialog_undo_bookmark)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            lifecycleScope.launch {
                val result =
                    if (positive) viewModel.postBookmark(status.id)
                    else viewModel.postUnBookmark(status.id)
                if (!result)
                    button.isChecked = status.bookmarked
            }
        }

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                if(positive)
                    settings.isDialogEnableOnBookmark = !isChecked
                else
                    settings.isDialogEnableOnUndoBookmark = !isChecked
            }
            override fun onDialogAccept() {
                exec()
            }
            override fun onDialogCancel() {
                button.isChecked = status.bookmarked
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            exec()
    }

    fun onFavouriteButtonLongClick(uri: String) {
        val settings = SettingsValues.getInstance()
        val isDialogEnable = settings.isDialogEnableOnFavourite

        val showDialog: (Credential) -> Unit = { credential ->
            val dialogCallback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
                override fun onCheckedDialog(isChecked: Boolean) {
                    settings.isDialogEnableOnFavourite = !isChecked
                }
                override fun onDialogAccept() {
                    lifecycleScope.launch { viewModel.postFavourite(credential, uri) }
                }
                override fun onDialogCancel() {
                }
            }
            val title = getString(R.string.dialog_favourite)
            val checkBoxTitle = getString(R.string.dialog_show_never)

            SimpleCheckBoxDialog.newInstance(dialogCallback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        }

        val callback = object : CredentialDialog.CredentialSelectCallback {
            override fun onCredentialSelect(credential: Credential) {
                if(isDialogEnable) showDialog(credential)
                else lifecycleScope.launch { viewModel.postFavourite(credential, uri) }
            }
        }
        CredentialDialog.newInstance(callback, getString(R.string.favourite_other_account))
            .show(requireActivity().supportFragmentManager, "tag")
    }

    fun onBoostButtonLongClick(uri: String) {
        val settings = SettingsValues.getInstance()
        val isDialogEnable = settings.isDialogEnableOnBoost

        val showDialog: (Credential) -> Unit = { credential ->
            val dialogCallback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
                override fun onCheckedDialog(isChecked: Boolean) {
                    settings.isDialogEnableOnBoost = !isChecked
                }
                override fun onDialogAccept() {
                    lifecycleScope.launch { viewModel.postBoost(credential, uri) }
                }
                override fun onDialogCancel() {
                }
            }
            val title = getString(R.string.dialog_reblog)
            val checkBoxTitle = getString(R.string.dialog_show_never)

            SimpleCheckBoxDialog.newInstance(dialogCallback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        }

        val callback = object : CredentialDialog.CredentialSelectCallback {
            override fun onCredentialSelect(credential: Credential) {
                if(isDialogEnable) showDialog(credential)
                else lifecycleScope.launch { viewModel.postBoost(credential, uri) }
            }
        }
        CredentialDialog.newInstance(callback, getString(R.string.boost_other_account))
            .show(requireActivity().supportFragmentManager, "tag")
    }

    fun onBookmarkButtonLongClick(uri: String) {
        val settings = SettingsValues.getInstance()
        val isDialogEnable = settings.isDialogEnableOnBookmark

        val showDialog: (Credential) -> Unit = { credential ->
            val dialogCallback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
                override fun onCheckedDialog(isChecked: Boolean) {
                    settings.isDialogEnableOnBookmark = !isChecked
                }
                override fun onDialogAccept() {
                    lifecycleScope.launch { viewModel.postBookmark(credential, uri) }
                }
                override fun onDialogCancel() {
                }
            }
            val title = getString(R.string.dialog_bookmark)
            val checkBoxTitle = getString(R.string.dialog_show_never)

            SimpleCheckBoxDialog.newInstance(dialogCallback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        }

        val callback = object : CredentialDialog.CredentialSelectCallback {
            override fun onCredentialSelect(credential: Credential) {
                if(isDialogEnable) showDialog(credential)
                else lifecycleScope.launch { viewModel.postBookmark(credential, uri) }
            }
        }
        CredentialDialog.newInstance(callback, getString(R.string.bookmark_other_account))
            .show(requireActivity().supportFragmentManager, "tag")
    }

    /**
     * EventsListenerの実装
     * その投稿が何かに対しての返信だった場合、返信マークを表示し
     * それが押されたら別画面で会話の流れを表示する
     */
    fun onStatusSelect(status: Status) {
        val intent = Intent(requireContext(), StatusDetailActivity::class.java).apply {
            putExtra("credential", viewModel.credential.value)
            putExtra("status", status)
        }
        startActivity(intent)
    }

    /**
     * EventsListenerの実装
     * 返信ボタンを押したら別画面で返信の投稿を作成する
     */
    fun onReplyButtonClick(status: Status) {
        val intent = Intent(requireContext(), CreatePostsActivity::class.java).apply {
            putExtra("credential", viewModel.credential.value)
            putExtra("reply_to", status)
        }
        startActivity(intent)
    }

    /**
     * EventsListenerの実装
     * ユーザのアイコンを押したら別画面でプロフィールを表示
     */
    fun onAccountClick(acct: String) {
        val intent = Intent(requireContext(), UserDetailActivity::class.java).apply {
            putExtra("acct", acct)
            putExtra("data", viewModel.credential.value)
        }
        startActivity(intent)
    }

    /**
     * AccountsAdapter.AccountsAdapterListenerの実装
     * ユーザのアイコンを押したら別画面でプロフィールを表示
     */
    fun onAccountClick(account: Account) {
        val intent = Intent(requireContext(), UserDetailActivity::class.java).apply {
            putExtra("account", account)
            putExtra("acct", account.acct)
            putExtra("data", viewModel.credential.value)
        }
        startActivity(intent)
    }

    /**
     * EventsListenerの実装
     * 投稿のハッシュタグを押したら別画面でそのタイムラインを表示
     */
    fun onHashtagClick(hashtag: String) {
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

    /**
     * EventsListenerの実装
     * アンケートに投票を行う
     */
    fun onVoteButtonClick(id: String, choices: List<Int>, progressBar: ProgressBar) {
        lifecycleScope.launch {
            if (choices.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_select), Toast.LENGTH_SHORT).show()
            }
            else {
                progressBar.visibility = View.VISIBLE
                viewModel.vote(id, choices)
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * EventsListenerの実装
     * 絵文字リアクション対応鯖ではふぁぼボタンを絵文字ボタンに置き換える
     *
     * 絵文字ボタンを押したら絵文字の一覧をダイアログで表示する
     * 投稿に対してすでに付いている絵文字を押した場合は、その絵文字でリアクションする(または取消)
     */
    fun onEmojiButtonClick(status: Status, isPut: Boolean, shortCode: String) {
        val settings = SettingsValues.getInstance()

        val isDialogEnable =
            if (isPut) settings.isDialogEnableOnEmojiReaction
            else settings.isDialogEnableOnUndoEmojiReaction

        val title =
            if (isPut) String.format(getString(R.string.dialog_emoji), shortCode)
            else String.format(getString(R.string.dialog_undo_emoji), shortCode)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val exec = {
            lifecycleScope.launch {
                if (isPut) viewModel.putEmojiAction(status.id, shortCode)
                else viewModel.deleteEmojiAction(status.id, shortCode)
            }
        }

        val callback = object : SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onCheckedDialog(isChecked: Boolean) {
                if (isPut)
                    settings.isDialogEnableOnEmojiReaction = !isChecked
                else
                    settings.isDialogEnableOnUndoEmojiReaction = !isChecked
            }

            override fun onDialogAccept() {
                exec()
            }

            override fun onDialogCancel() {
            }
        }

        if (shortCode.isEmpty()) {
            EmojiActionDialog.newInstance(this, viewModel.credential.value!!, status, hashCode())
                .show(requireActivity().supportFragmentManager, "tag")
        } else if (isDialogEnable) {
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        } else {
            exec()
        }
    }

    fun onMenuItemClick(status: Status, item: Item) {
        when(item) {
            Item.MENU_DELETE                    -> deletePosts(status)
            Item.MENU_PIN                       -> pinThePosts(status)
            Item.MENU_UNPIN                     -> unPinThePosts(status)
            Item.MENU_FAVOURITE                 -> onFavouriteMenuSelect(status)
            Item.MENU_UNFAVOURITE               -> onFavouriteMenuSelect(status)
            Item.MENU_BOOST_PUBLIC              -> onBoostMenuSelect(status, Visibility.PUBLIC)
            Item.MENU_BOOST_UNLISTED            -> onBoostMenuSelect(status, Visibility.UNLISTED)
            Item.MENU_BOOST_PRIVATE             -> onBoostMenuSelect(status, Visibility.PRIVATE)
            Item.MENU_FAVOURITE_OTHER_ACCOUNT   -> onFavouriteButtonLongClick(status.uri)
            Item.MENU_BOOKMARK_OTHER_ACCOUNT    -> onBookmarkButtonLongClick(status.uri)
            Item.MENU_BOOST_OTHER_ACCOUNT       -> onBoostButtonLongClick(status.uri)
            Item.MENU_WHO_FAVOURITED            -> openWhoFavourited(status.id)
            Item.MENU_WHO_BOOSTED               -> openWhoReblogged(status.id)
            Item.MENU_OPEN_BROWSER              -> openBrowser(Uri.parse(status.uri))
            Item.MENU_COPY_CLIPBOARD            -> copyToClipboard(status.uri)
        }
    }

    /**
     * 選択した投稿を削除する(自分の投稿のみ)
     */
    private fun deletePosts(status: Status) {
        val settings = SettingsValues.getInstance()

        val isDialogEnable = settings.isDialogEnableOnDeleteStatus

        val title = getString(R.string.dialog_delete)

        val checkBoxTitle = getString(R.string.dialog_show_never)

        val callback = object: SimpleCheckBoxDialog.SimpleCheckboxDialogListener{
            override fun onCheckedDialog(isChecked: Boolean) {
                settings.isDialogEnableOnDeleteStatus = !isChecked
            }
            override fun onDialogAccept() {
                lifecycleScope.launch { viewModel.deleteStatus(status.id) }
            }
            override fun onDialogCancel() {
            }
        }

        if(isDialogEnable)
            SimpleCheckBoxDialog.newInstance(callback, title, checkBoxTitle)
                .show(requireActivity().supportFragmentManager, "tag")
        else
            lifecycleScope.launch { viewModel.deleteStatus(status.id) }
    }

    /**
     * 投稿をピン留めする(自分の投稿のみ)
     */
    private fun pinThePosts(status: Status) {
        lifecycleScope.launch { viewModel.pinThePosts(status.id) }
    }

    /**
     * 投稿のピン留めを解除する(自分の投稿のみ)
     */
    private fun unPinThePosts(status: Status) {
        lifecycleScope.launch { viewModel.unPinThePosts(status.id) }
    }

    private fun openWhoFavourited(id: String) {
        val credential = viewModel.credential.value!!
        val column = ColumnInfo(credential.acct, "favourited_by", id, "", -1)
        val intent = Intent(requireActivity(), SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    private fun openWhoReblogged(id: String) {
        val credential = viewModel.credential.value!!
        val column = ColumnInfo(credential.acct, "reblogged_by", id, "", -1)
        val intent = Intent(requireActivity(), SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    /**
     * 文字列をクリップボードにコピーする
     */
    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
        Toast.makeText(requireContext(), getString(R.string.url_copied), Toast.LENGTH_SHORT).show()
    }

    private fun openBrowser(uri: Uri) {
        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(webIntent)
    }

    enum class Item(val order: Int) {
        MENU_DELETE(0),
        MENU_PIN(1),
        MENU_UNPIN(2),
        MENU_FAVOURITE(3),
        MENU_UNFAVOURITE(4),
        MENU_BOOST_PUBLIC(5),
        MENU_BOOST_UNLISTED(6),
        MENU_BOOST_PRIVATE(7),
        MENU_FAVOURITE_OTHER_ACCOUNT(8),
        MENU_BOOST_OTHER_ACCOUNT(9),
        MENU_BOOKMARK_OTHER_ACCOUNT(10),
        MENU_WHO_FAVOURITED(11),
        MENU_WHO_BOOSTED(12),
        MENU_OPEN_BROWSER(13),
        MENU_COPY_CLIPBOARD(14),
    }

    enum class Visibility {
        PUBLIC,
        UNLISTED,
        PRIVATE,
    }

    companion object {
        const val MY_POSTS = 0
        const val FAVOURITE = 1
        const val BOOST = 2
        const val OTHER_ACCOUNT = 3
        const val WHO_ACTIONED = 4
        const val GENERAL = 5
    }
}