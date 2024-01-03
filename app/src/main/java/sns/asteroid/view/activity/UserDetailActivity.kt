package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Field
import sns.asteroid.databinding.ActivityUserDetailBinding
import sns.asteroid.databinding.GeneralLoadingBinding
import sns.asteroid.databinding.GeneralTimeoutBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.view.adapter.pager.UserProfilePagerAdapter
import sns.asteroid.view.adapter.profile.FieldAdapter
import sns.asteroid.view.dialog.CredentialDialog
import sns.asteroid.view.dialog.SimpleCheckBoxDialog
import sns.asteroid.view.dialog.SimpleDialog
import sns.asteroid.viewmodel.UserDetailViewModel

class UserDetailActivity:
    BaseActivity(),
    MenuProvider,
    TextLinkMovementMethod.LinkCallback,
    CredentialDialog.CredentialSelectCallback {
    private val viewModel: UserDetailViewModel by viewModels {
        val credential = intent.getSerializableExtra("data") as Credential
        val url = intent.getStringExtra("url")
        val acct = intent.getStringExtra("acct")
        val account =  intent.getSerializableExtra("account") as Account?
        UserDetailViewModel.Factory(credential, url, acct, account)
    }
    private val binding: ActivityUserDetailBinding by lazy {
        ActivityUserDetailBinding.inflate(layoutInflater)
    }

    private val isMe by lazy {
        val credential = intent.getSerializableExtra("data") as Credential
        val acct = intent.getStringExtra("acct")
        val account =  intent.getSerializableExtra("account") as Account?
        (credential.acct == acct) or (credential.screenName == acct) or (credential.account_id == account?.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loading = GeneralLoadingBinding.inflate(layoutInflater)
        setContentView(loading.root)

        binding.toolbar.addMenuProvider(this)

        viewModel.acct.observe(this, Observer {
            binding.toolbar.title = String.format(getString(R.string.acct), it)
        })
        viewModel.toastMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        viewModel.relationship.observe(this, Observer {
            binding.toolbar.invalidateMenu()

            binding.followButton.also { button ->
                if(it.blocking) {
                    binding.followButtonImageResource = R.drawable.button_block
                    binding.followButtonAccentColor = getColor(R.color.gray0)
                    binding.followButtonTitle = getString(R.string.button_block)
                } else if (it.muting) {
                    binding.followButtonImageResource = R.drawable.button_mute
                    binding.followButtonAccentColor = getColor(R.color.gray0)
                    binding.followButtonTitle = getString(R.string.button_mute)
                } else if(it.following) {
                    binding.followButtonImageResource = R.drawable.button_follow_active
                    binding.followButtonAccentColor = viewModel.credential.accentColor
                    binding.followButtonTitle = getString(R.string.button_following)
                } else if (it.requested) {
                    binding.followButtonImageResource = R.drawable.button_follow_request
                    binding.followButtonAccentColor = getColor(R.color.gray0)
                    binding.followButtonTitle = getString(R.string.button_request)
                } else {
                    binding.followButtonImageResource = R.drawable.button_follow
                    binding.followButtonAccentColor = getColor(R.color.gray0)
                    binding.followButtonTitle = getString(R.string.button_follow)
                }
                button.visibility = View.VISIBLE
            }
        })

        viewModel.account.observe(this, Observer { account ->
            binding.account = account

            binding.fields.also {
                val list = account.convertedField ?: emptyList()
                it.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                (it.adapter as FieldAdapter).submitList(list)
            }
            binding.hitoWoMadowasuSuuji.also {
                val list = listOf(
                    Triple(getString(R.string.title_posts),"${account.statuses_count}", null),
                    Triple(getString(R.string.title_following),"${account.following_count}", null),
                    Triple(getString(R.string.title_followers),"${account.followers_count}", null),
                )
                (it.adapter as FieldAdapter).submitList(list)
            }
            binding.viewPager.also {
                it.adapter = UserProfilePagerAdapter(this, binding.tabLayoutCollapsed, binding.viewPager, viewModel.credential, account)
            }

            setContentView(binding.root)

            lifecycleScope.launch {
                if(!isMe) viewModel.getRelationship()
                else {
                    binding.editProfile.visibility = View.VISIBLE
                    binding.followButtonAccentColor = viewModel.credential.accentColor
                }
            }
        })

        binding.profile.movementMethod = TextLinkMovementMethod(this)

        binding.followButton.also {
            it.setOnClickListener { onFollowButtonClick() }
        }
        binding.editProfile.also {
            it.setOnClickListener { openEditProfile(viewModel.credential) }
        }
        binding.also {
            it.fields.adapter = FieldAdapter(this, TextLinkMovementMethod(this))
            it.fields.layoutManager = GridLayoutManager(this, 1)

            it.hitoWoMadowasuSuuji.adapter = FieldAdapter(this, TextLinkMovementMethod(this))
            it.hitoWoMadowasuSuuji.layoutManager = GridLayoutManager(this, 3)
            it.hitoWoMadowasuSuujiCard.visibility = if (SettingsValues.newInstance().isShowFollowersCount) View.VISIBLE else View.GONE
        }

        lifecycleScope.launch {
            if (!viewModel.getAccount()) {
                val binding = GeneralTimeoutBinding.inflate(layoutInflater)
                setContentView(binding.root)
            }
        }
    }

    /**
     * アカウントのacctから鯖のURLを抜き出して、鯖の情報のページを開く
     */
    private fun openAboutInstance() {
        val server = let {
            val regex = Regex(".*?://(.*?)/")
            val account = viewModel.account.value ?: return
            regex.find(account.url)?.groupValues?.getOrNull(1) ?: return
        }
        val intent = Intent(this, AboutInstanceActivity::class.java).apply {
            val data = intent.getSerializableExtra("data") as Credential
            putExtra("credential", data)
            putExtra("target", server)
        }
        startActivity(intent)
    }

    /**
     * MenuProviderの実装
     * 右上メニューを作成
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_user_profile, menu)
        menu.findItem(R.id.menu_block_list)?.isVisible = isMe
        menu.findItem(R.id.menu_mute_list)?.isVisible = isMe

        val isBlocked: Boolean? = viewModel.relationship.value?.blocking
        val isMuted: Boolean? = viewModel.relationship.value?.muting

        menu.findItem(R.id.menu_block)?.isVisible = (isBlocked == false)
        menu.findItem(R.id.menu_unblock)?.isVisible = (isBlocked == true)

        menu.findItem(R.id.menu_mute)?.isVisible = (isMuted == false)
        menu.findItem(R.id.menu_unmute)?.isVisible = (isMuted == true)

        val isFollowing = viewModel.relationship.value?.following ?: false
        menu.findItem(R.id.menu_add_to_list)?.isVisible = isFollowing or isMe

        val showReblogs = viewModel.relationship.value?.showing_reblogs ?: false
        menu.findItem(R.id.menu_show_reblogs)?.isVisible = isFollowing and !showReblogs
        menu.findItem(R.id.menu_hide_reblogs)?.isVisible = isFollowing and showReblogs

        val enableNotify = viewModel.relationship.value?.notifying ?: false
        menu.findItem(R.id.menu_enable_notify)?.isVisible = isFollowing and !enableNotify
        menu.findItem(R.id.menu_disable_notify)?.isVisible = isFollowing and enableNotify
        
        menu.findItem(R.id.menu_open_browser)?.isVisible = !viewModel.account.value?.url.isNullOrBlank()
    }

    /**
     * MenuProviderの実装
     */
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.menu_about_instance-> openAboutInstance()
            R.id.menu_block_list    -> openSingleTimeline(viewModel.credential, "block")
            R.id.menu_mute_list     -> openSingleTimeline(viewModel.credential, "mute")
            R.id.menu_add_column    -> lifecycleScope.launch { viewModel.addColumn() }
            R.id.menu_add_column_media -> lifecycleScope.launch { viewModel.addColumn(onlyMedia = true) }
            R.id.menu_other_account -> CredentialDialog.newInstance(this, getString(R.string.dialog_change_account)).show(supportFragmentManager, "tag")
            R.id.menu_add_to_list   -> openAddToList(viewModel.credential, viewModel.account.value!!)
            R.id.menu_enable_notify -> lifecycleScope.launch { viewModel.postUserAction(Accounts.PostAction.NOTIFY) }
            R.id.menu_disable_notify-> lifecycleScope.launch { viewModel.postUserAction(Accounts.PostAction.DISABLE_NOTIFY) }
            R.id.menu_show_reblogs  -> lifecycleScope.launch { viewModel.postUserAction(Accounts.PostAction.SHOW_BOOST) }
            R.id.menu_hide_reblogs  -> lifecycleScope.launch { viewModel.postUserAction(Accounts.PostAction.HIDE_BOOST) }
            R.id.menu_open_browser  -> openBrowser(viewModel.account.value!!.url.toUri())
            R.id.menu_mute          -> showMuteDialog()
        }

        val action = when (menuItem.itemId) {
            R.id.menu_block     -> Accounts.PostAction.BLOCK
            R.id.menu_unblock   -> Accounts.PostAction.UNBLOCK
            R.id.menu_unmute    -> Accounts.PostAction.UNMUTE
            else                -> return false
        }

        showDialog(action)
        return false
    }

    /**
     * TextLinkMovementMethod.LinkCallbackの実装
     * ユーザのプロフィールを別画面で開く
     * acctが分からない場合はURLを引数に取る
     */
    override fun onAccountURLClick(url: String) {
        openAccount(viewModel.credential, null, null, url)
    }

    /**
     * TextLinkMovementMethod.LinkCallbackの実装
     * ユーザのプロフィールを別画面で開く
     */
    override fun onWebFingerClick(acct: String) {
        openAccount(viewModel.credential, acct)
    }

    /**
     * TextLinkMovementMethod.LinkCallbackの実装
     * ハッシュタグのリンクをクリックした際にカラムを別画面で開く
     */
    override fun onHashtagClick(hashtag: String) {
        val credential = viewModel.credential
        val column = let {
            val subject = "hashtag"
            ColumnInfo(credential.acct, subject, hashtag, "#${hashtag}", -1)
        }
        val intent = Intent(this@UserDetailActivity, SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    /**
     * CredentialDialog.CredentialSelectCallbackの実装
     * 別のアカウントからユーザのプロフィールを開く
     */
    override fun onCredentialSelect(credential: Credential) {
        if (viewModel.credential == credential) return
        val webFinger =
            if (viewModel.acct.value!!.contains("@"))
                viewModel.acct.value
            else {
                val regex = Regex("@(.+)")
                val domain = regex.find(viewModel.credential.acct)?.groupValues?.get(0)
                viewModel.acct.value!!.plus(domain)
            }

        openAccount(credential, webFinger, null)
        Toast.makeText(this@UserDetailActivity, credential.acct, Toast.LENGTH_SHORT).show()
    }

    override fun onProfileEdit() {
        lifecycleScope.launch { viewModel.getAccount() }
    }

    private fun onFollowButtonClick() {
        val action =
            if (viewModel.relationship.value?.blocking == true)
                Accounts.PostAction.UNBLOCK
            else if (viewModel.relationship.value?.muting == true)
                Accounts.PostAction.UNMUTE
            else if(viewModel.relationship.value?.following == true)
                Accounts.PostAction.UNFOLLOW
            else if(viewModel.relationship.value?.requested == true)
                Accounts.PostAction.UNDO_REQUEST_FOLLOW
            else if(viewModel.account.value!!.locked)
                Accounts.PostAction.REQUEST_FOLLOW
            else
                Accounts.PostAction.FOLLOW

        when(action) {
            Accounts.PostAction.UNBLOCK -> showDialog(action).also { return }
            Accounts.PostAction.UNMUTE  -> showDialog(action).also { return }
            else -> {}
        }

        val settings = SettingsValues.newInstance()

        val isDialogEnable = when(action) {
            Accounts.PostAction.FOLLOW              -> settings.isDialogEnableOnFollow
            Accounts.PostAction.UNFOLLOW            -> settings.isDialogEnableOnUnFollow
            Accounts.PostAction.REQUEST_FOLLOW      -> settings.isDialogEnableOnFollow
            Accounts.PostAction.UNDO_REQUEST_FOLLOW -> settings.isDialogEnableOnUnFollow
            else -> true
        }

        if (isDialogEnable)
            showCheckboxDialog(action)
        else
            lifecycleScope.launch { viewModel.postUserAction(action) }
    }

    private fun showDialog(action: Accounts.PostAction){
        val listener = object : SimpleDialog.SimpleDialogListener {
            override fun onDialogAccept() {
                lifecycleScope.launch { viewModel.postUserAction(action) }
            }
            override fun onDialogCancel() {
            }
        }
        val title = when(action){
            Accounts.PostAction.BLOCK ->
                String.format(getString(R.string.dialog_block), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.UNBLOCK ->
                String.format(getString(R.string.dialog_unblock), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.MUTE ->
                String.format(getString(R.string.dialog_mute), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.UNMUTE ->
                String.format(getString(R.string.dialog_unmute), viewModel.credential.acct, viewModel.account.value?.acct)
            else ->
                return
        }

        SimpleDialog.newInstance(listener, title).show(supportFragmentManager, "tag")
    }

    private fun showMuteDialog(){
        val listener = object : SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            var isChecked = false

            override fun onDialogAccept() {
                lifecycleScope.launch {
                    if(isChecked) viewModel.postUserAction(Accounts.PostAction.MUTE_NOTIFICATION)
                    else viewModel.postUserAction(Accounts.PostAction.MUTE)
                }
            }
            override fun onDialogCancel() {
            }
            override fun onCheckedDialog(isChecked: Boolean) {
                this.isChecked = isChecked
            }
        }
        val title = String.format(getString(R.string.dialog_mute), viewModel.credential.acct, viewModel.account.value?.acct)
        val checkBoxText = getString(R.string.dialog_mute_checkbox)
        SimpleCheckBoxDialog.newInstance(listener, title, checkBoxText).show(supportFragmentManager, "tag")
    }

    private fun showCheckboxDialog(action: Accounts.PostAction){
        val settings = SettingsValues.newInstance()

        val listener = object : SimpleCheckBoxDialog.SimpleCheckboxDialogListener {
            override fun onDialogAccept() {
                lifecycleScope.launch { viewModel.postUserAction(action) }
            }
            override fun onDialogCancel() {
            }
            override fun onCheckedDialog(isChecked: Boolean) {
                val isEnable = !isChecked

                when(action) {
                    Accounts.PostAction.FOLLOW              -> settings.isDialogEnableOnFollow = isEnable
                    Accounts.PostAction.UNFOLLOW            -> settings.isDialogEnableOnUnFollow = isEnable
                    Accounts.PostAction.REQUEST_FOLLOW      -> settings.isDialogEnableOnFollow = isEnable
                    Accounts.PostAction.UNDO_REQUEST_FOLLOW -> settings.isDialogEnableOnUnFollow = isEnable
                    else                                    -> {}
                }
            }
        }
        val title = when(action){
            Accounts.PostAction.FOLLOW ->
                String.format(getString(R.string.dialog_follow), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.UNFOLLOW ->
                String.format(getString(R.string.dialog_unfollow), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.REQUEST_FOLLOW ->
                String.format(getString(R.string.dialog_follow_request), viewModel.credential.acct, viewModel.account.value?.acct)
            Accounts.PostAction.UNDO_REQUEST_FOLLOW ->
                String.format(getString(R.string.dialog_undo_follow_request), viewModel.credential.acct, viewModel.account.value?.acct)
            else ->
                return
        }
        val checkboxTitle = getString(R.string.dialog_show_never)

        SimpleCheckBoxDialog.newInstance(listener, title, checkboxTitle).show(supportFragmentManager, "tag")
    }
}