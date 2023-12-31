package sns.asteroid.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential

abstract class BaseActivity: AppCompatActivity() {

    private val createPostsResult =
        // LifecycleOwners must call register before they are STARTED
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                onCreatePostsSuccess()
            }
        }

    private val editProfileResult =
        // LifecycleOwners must call register before they are STARTED
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                onProfileEdit()
            }
        }

    protected fun openBrowser(uri: Uri) {
        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(webIntent)
    }
    protected fun openAuthorizeActivity(isFirst: Boolean) {
        val intent = Intent(this, AuthorizeActivity::class.java).apply {
            putExtra("isFirst", isFirst)
        }
        startActivity(intent)
        finish()
    }

    protected fun openCreatePostsActivity(credential: Credential) {
        openCreatePostsActivity(credential, "", "")
    }
    protected fun openCreatePostsActivity(credential: Credential, text: String, visibility: String) {
        val intent = Intent(this, CreatePostsActivity::class.java).apply {
            putExtra("credential", credential)
            putExtra("visibility", visibility)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        createPostsResult.launch(intent)
    }
    protected fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    protected fun openManageColumns() {
        val intent = Intent(this, ManageColumnsActivity::class.java)
        startActivity(intent)
    }

    protected fun openManageAccounts() {
        val intent = Intent(this, SettingsManageAccountsActivity::class.java)
        startActivity(intent)
    }

    protected fun openSingleTimeline(credential: Credential, subject: String) {
        val column = ColumnInfo(credential.acct, subject, -1)
        val intent = Intent(this, SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    /**
     * acctがわからない場合に限りacctをnullとし、アカウントのURLを代替で引数に取る
     * ** (サーバのURLとacctの@より後は、一致しない場合があることに注意)
     * ** (例) https://social.vivaldi.net/@Vivaldi -> @Vivaldi@vivaldi.net
     */
    protected fun openAccount(credential: Credential, acct: String?, account: Account? = null, url: String? = null) {
        val intent = Intent(this, UserDetailActivity::class.java).apply {
            putExtra("data", credential)
            putExtra("url", url)
            putExtra("acct", acct)
            putExtra("account", account)
        }
        startActivity(intent)
    }

    protected fun openLists(credential: Credential) {
        val intent = Intent(this, ListsActivity::class.java).apply {
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    protected  fun openEditProfile(credential: Credential) {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("credential", credential)
        }
        editProfileResult.launch(intent)
    }

    protected fun openSearch(credential: Credential) {
        val intent = Intent(this, SearchActivity::class.java).apply {
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    protected fun openTrends(credential: Credential) {
        val intent = Intent(this, TrendsActivity::class.java).apply {
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    protected fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken ,0)
        } catch (_: Exception) {
        }
    }

    protected fun openAddToList(credential: Credential, account: Account) {
        val intent = Intent(this, AddAccountToListActivity::class.java).apply {
            putExtra("credential", credential)
            putExtra("account", account)
        }
        startActivity(intent)
    }

    protected fun openListAccounts(credential: Credential, listId: String) {
        val intent = Intent(this, ListAccountsActivity::class.java).apply {
            putExtra("credential", credential)
            putExtra("list_id", listId)
        }
        startActivity(intent)
    }

    protected open fun onCreatePostsSuccess() {
    }

    protected open fun onProfileEdit() {
    }
}