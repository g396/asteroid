package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Search
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.SearchModel

class SearchViewModel(val credential: Credential): ViewModel() {
    val query = MutableLiveData<String>()

    private val _search = MutableLiveData<Search>()
    val search: LiveData<Search> get() = _search

    private val searchModel = SearchModel(credential)

    init {
        query.value = ""
    }

    suspend fun getAll() {
        withContext(Dispatchers.IO) {
            val result = searchModel.searchAll(query.value!!)
            result.content?.let {
                _search.postValue(it)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(credential) as T
        }
    }
}