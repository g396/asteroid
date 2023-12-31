package sns.asteroid.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.user.AccountsModel
import sns.asteroid.model.user.MediaModel
import sns.asteroid.model.settings.SettingsManageAccountsModel

class EditProfileViewModel(val credential: Credential): ViewModel() {
    val toastMessage = MutableLiveData<String>()
    val account = MutableLiveData<Account>()

    val avatarUrl = MutableLiveData<String>()
    val headerUrl = MutableLiveData<String>()
    val note = MutableLiveData<String>()
    val displayName = MutableLiveData<String>()
    val fields = MutableLiveData<MutableList<MutableMap<String, String>>>()
    val isLocked = MutableLiveData<Boolean>()
    val isBot = MutableLiveData<Boolean>()

    private val avatarByteArray = MutableLiveData<ByteArray>()
    private val headerByteArray = MutableLiveData<ByteArray>()

    suspend fun update(): Boolean {
        return withContext(Dispatchers.IO) {
            val result = AccountsModel(credential).updateProfile(
                displayName.value!!,
                note.value!!,
                fields.value!!,
                isLocked.value!!,
                isBot.value!!,
                avatarByteArray.value,
                headerByteArray.value,
            )

            result.account?.let {
                setAccountValues(it)
                SettingsManageAccountsModel().updateAvatar(credential, it.avatar)
            }

            if(result.isSuccess) {
                toastMessage.postValue(getString(R.string.saved))
            } else {
                result.toastMessage?.let { toastMessage.postValue(it) }
            }

            result.isSuccess
        }
    }

    suspend fun getCurrent(): Boolean {
        return withContext(Dispatchers.IO) {
            val result = AccountsModel(credential).updateProfile()
            result.account?.let { setAccountValues(it) }
            result.toastMessage?.let {
                toastMessage.postValue(it)
            }

            result.isSuccess
        }
    }

    suspend fun importAvatar(uri: Uri) {
        withContext(Dispatchers.IO) {
            avatarUrl.postValue(uri.toString())
            val byteArray = MediaModel.importFile(uri,400) ?: return@withContext
            avatarByteArray.postValue(byteArray)
        }
    }

    suspend fun importHeader(uri: Uri) {
        withContext(Dispatchers.IO) {
            headerUrl.postValue(uri.toString())
            val byteArray = MediaModel.importFile(uri,1500) ?: return@withContext
            headerByteArray.postValue(byteArray)
        }
    }

    private fun setAccountValues(account: Account) {
        avatarUrl.postValue(account.avatar)
        headerUrl.postValue(account.header_static)
        displayName.postValue(account.display_name)
        note.postValue(account.source?.note)
        isLocked.postValue(account.locked)
        isBot.postValue(account.bot)
        val list = mutableListOf<MutableMap<String, String>>().apply {
            account.source?.fields?.forEach { field ->
                val map = mapOf(
                    Pair("name", field.name),
                    Pair("value", field.value),
                ).toMutableMap()
                add(map)
            }

            val dummySize = let { _ ->
                val size = account.source?.fields?.size ?: 0
                if (size > 4) 0 else 4-size
            }
            for(index in 1 .. dummySize) {
                add(emptyMap<String, String>().toMutableMap())
            }
        }
        fields.postValue(list)
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditProfileViewModel(credential) as T
        }
    }
}