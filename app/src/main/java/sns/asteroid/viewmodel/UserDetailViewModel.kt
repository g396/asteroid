package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Relationship
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.SearchModel
import sns.asteroid.model.user.AccountsModel
import sns.asteroid.model.user.RelationshipModel
import sns.asteroid.model.user.UserActionModel

class UserDetailViewModel(
    val credential: Credential,
    val url: String?,
    acct: String?,
    account: Account?,
): ViewModel() {
    private val _relationship = MutableLiveData<Relationship>()
    val relationship: LiveData<Relationship>
        get() = _relationship

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val _acct = MutableLiveData<String>()
    val acct: LiveData<String>
        get() = _acct
    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account>
        get() = _account

    init {
        account?.let { _account.value = it }
        acct?.let { _acct.value = it }
    }

    suspend fun getAccount(): Boolean {
        return if(url != null) {
            findAccount(url)
        } else if(acct.value != null) withContext(Dispatchers.IO) {
            val result = AccountsModel(credential).getAccountByAcct(acct.value!!)
            result.account?.let { _account.postValue(it) }
            result.toastMessage?.let { _toastMessage.postValue(it) }
            result.isSuccess
        } else false
    }

    suspend fun getRelationship(){
        val result = withContext(Dispatchers.IO) { RelationshipModel(credential, account.value!!.id).getRelationship() }
        if(result.isSuccess) result.relationship?.let { _relationship.value = it.first() }
        else result.toastMessage?.let { _toastMessage.value = it }
    }

    suspend fun postUserAction(action: Accounts.PostAction) {
        val result = withContext(Dispatchers.IO) { UserActionModel(credential).postUserAction(account.value!!.id, action) }
        _toastMessage.value = result.toastMessage
        result.relationship?.let { _relationship.value = it }
    }

    suspend fun addColumn(onlyMedia: Boolean = false) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        val dao = db.columnInfoDao()

        withContext(Dispatchers.IO) {
            val subject = if(onlyMedia) "user_media" else "user_posts"
            val size = dao.getAll().size
            val columnInfo = ColumnInfo(credential.acct, subject, account.value!!.id, account.value!!.acct, size)
            dao.insert(columnInfo)
        }

        _toastMessage.value = getString(R.string.added_column)
    }

    private suspend fun findAccount(url: String): Boolean {
        val account = withContext(Dispatchers.IO) { SearchModel(credential).findAccount(url) }?.also {
            _account.postValue(it)
            _acct.postValue(it.acct)
        }
        return (account != null)
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(val credential: Credential, val url: String?, val acct: String?, val account: Account?) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDetailViewModel(credential, url, acct, account) as T
        }
    }
}