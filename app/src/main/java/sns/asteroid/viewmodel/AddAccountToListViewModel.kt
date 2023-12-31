package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.user.ListsModel

class AddAccountToListViewModel(credential: Credential, private val userId: String): ViewModel() {
    private val _list = MutableLiveData<List<Pair<ListTimeline, Boolean>>>()
    val list: LiveData<List<Pair<ListTimeline, Boolean>>> get() = _list

    val model = ListsModel(credential)

    suspend fun getInAccount() = withContext(Dispatchers.IO) {
        val all = model.getAll().lists ?: return@withContext
        val inAccount = model.getInAccount(userId).lists ?: return@withContext

        val pair = all.associateWith { inAccount.contains(it) }
            .toList()

        _list.postValue(pair)
    }

    suspend fun addAccount(listTimeline: ListTimeline) = withContext(Dispatchers.IO) {
        val result = model.addAccountToList(userId, listTimeline.id)
        if (result) {
            val current = list.value!!.toMutableList()
            val pair = current.find { it.first.id == listTimeline.id } ?: return@withContext
            val replaced = Pair(pair.first, true)
            val index = current.indexOf(pair)
            current.removeAt(index)
            current.add(index, replaced)
            _list.postValue(current)
        }
    }

    suspend fun removeAccount(listTimeline: ListTimeline) = withContext(Dispatchers.IO) {
        val result = model.removeAccountFromList(userId, listTimeline.id)
        if (result) {
            val current = list.value!!.toMutableList()
            val pair = current.find { it.first.id == listTimeline.id } ?: return@withContext
            val replaced = Pair(pair.first, false)
            val index = current.indexOf(pair)
            current.removeAt(index)
            current.add(index, replaced)
            _list.postValue(current)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential, private val userId: String) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddAccountToListViewModel(credential, userId) as T
        }
    }
}