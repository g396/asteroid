package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsManageAccountsModel

class SettingsManageAccountViewModel: ViewModel() {
    private var _accounts = MutableLiveData<MutableList<Credential>>()
    val accounts: LiveData<MutableList<Credential>>
        get() = _accounts

    suspend fun getCredentials() {
        val accounts = withContext(Dispatchers.IO) {
            SettingsManageAccountsModel().getCredentials()
        }

        _accounts.value = accounts
    }

    suspend fun removeCredential(credential: Credential) {
        withContext(Dispatchers.IO) {
            SettingsManageAccountsModel().removeCredential(credential)
        }
    }

    suspend fun changeAccentColor(credential: Credential, color: Int) {
        withContext(Dispatchers.IO) {
            SettingsManageAccountsModel().changeAccentColor(credential, color)
        }
    }

    suspend fun updateAll(list: List<Credential>) {
        withContext(Dispatchers.IO) {
            SettingsManageAccountsModel().updateAll(list)
        }
    }
}