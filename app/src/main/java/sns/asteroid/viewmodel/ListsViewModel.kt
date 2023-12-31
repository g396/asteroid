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

class ListsViewModel(val credential: Credential): ViewModel() {
    private val _lists = MutableLiveData<List<ListTimeline>>()
    val lists: LiveData<List<ListTimeline>>
        get() = _lists

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val model = ListsModel(credential)

    init {
        _lists.value = emptyList()
    }
    suspend fun getAll() {
        withContext(Dispatchers.IO) {
            val result = model.getAll()
            result.lists?.let { _lists.postValue(it.toMutableList()) }
        }
    }

    suspend fun createList(title: String) = withContext(Dispatchers.IO) {
        val result = model.createList(title)

        result?.let {
            val update = lists.value!!.toMutableList().apply {
                add(lists.value!!.size, it)
            }
            _lists.postValue(update)
        }

        if(result != null)
            _toastMessage.postValue("Created")
        else
            _toastMessage.postValue("An error occurred")
    }

    suspend fun updateList(id: String, title: String) = withContext(Dispatchers.IO) {
        val repliesPolicy = lists.value?.find { it.id == id }?.replies_policy ?: return@withContext
        val exclusive = lists.value?.find { it.id == id }?.exclusive

        val result = model.updateList(id, title, repliesPolicy, exclusive)
        val newItem = result.lists?.firstOrNull()

        if (newItem == null) {
            _toastMessage.postValue(result.message ?: "null")
            return@withContext
        }

        val update = lists.value!!.toMutableList().apply {
            val index = indexOfFirst { it.id == newItem.id }
            if(index == -1) return@withContext
            removeAt(index)
            add(index, newItem)
        }
        _lists.postValue(update)
        _toastMessage.postValue("Updated")
    }

    suspend fun deleteList(id: String) = withContext(Dispatchers.IO) {
        val result = model.deleteList(id)
        if (result) {
            val update = lists.value!!.toMutableList().apply {
                removeIf { it.id == id }
            }
            _lists.postValue(update)
            _toastMessage.postValue("Deleted")
        } else {
            _toastMessage.postValue("An error occurred")
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListsViewModel(credential) as T
        }
    }
}