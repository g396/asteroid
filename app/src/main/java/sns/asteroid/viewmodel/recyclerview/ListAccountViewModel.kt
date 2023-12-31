package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.user.ListsModel
import sns.asteroid.model.account.ListAccountModel

class ListAccountViewModel(
    columnInfo: ColumnInfo, credential: Credential, private val listId: String
    ): RecyclerViewViewModel<Account>(columnInfo, credential) {
    override val timelineModel = ListAccountModel(credential, listId)

    suspend fun addToList(account: Account) = withContext(Dispatchers.IO) {
        val result = ListsModel(credential.value!!).addAccountToList(account.id, listId)
        if (result) addAccounts(account)
    }

    suspend fun removeFromList(account: Account) = withContext(Dispatchers.IO) {
        val result = ListsModel(credential.value!!).removeAccountFromList(account.id, listId)
        if (result) removeAccounts(account)
    }

    private suspend fun addAccounts(account: Account) = withContext(Dispatchers.IO) {
        val list = contents.value!!.toMutableList().apply {
            add(size, account)
        }
        _contents.postValue(list)
    }

    private suspend fun removeAccounts(account: Account) = withContext(Dispatchers.IO) {
        val list = contents.value!!.toMutableList().apply {
            remove(account)
        }
        _contents.postValue(list)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val columnInfo: ColumnInfo,
        private val credential: Credential,
        private val listId: String,
        ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListAccountViewModel(columnInfo, credential, listId) as T
        }
    }
}