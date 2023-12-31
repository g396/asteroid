package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.AccountSearchModel

class AccountSearchViewModel(
    columnInfo: ColumnInfo, credential: Credential, query: String, accounts: List<Account>
): RecyclerViewViewModel<Account>(columnInfo, credential) {
    override val timelineModel = AccountSearchModel(credential, query, offset = accounts.size)

    init {
        _contents.postValue(accounts)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val credential: Credential,
        private val query: String,
        private val accounts: List<Account>,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val columnInfo = ColumnInfo("dummy", "search", -1)
            return AccountSearchViewModel(columnInfo, credential, query, accounts) as T
        }
    }
}