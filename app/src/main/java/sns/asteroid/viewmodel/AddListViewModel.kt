package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.user.ListsModel

class AddListViewModel(val credential: Credential): ViewModel() {
    private val _lists = MutableLiveData<List<ListTimeline>>()
    val lists: LiveData<List<ListTimeline>>
        get() = _lists

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    suspend fun getLists() {
        val result = withContext(Dispatchers.IO) { ListsModel(credential).getAll() }
        if(result.isSuccess) {
            _lists.value = let {
                if (result.lists.isNullOrEmpty()) _message.value = "List not found"
                result.lists?: emptyList()
            }

        } else {
            result.message?. let { _message.value = it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddListViewModel(credential) as T
        }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}