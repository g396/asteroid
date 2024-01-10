package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.db.entities.Draft
import sns.asteroid.model.DraftModel

class DraftViewModel: ViewModel() {
    private val _drafts = MutableLiveData<List<Draft>>()
    val drafts: LiveData<List<Draft>> get() = _drafts

    suspend fun getAll() = withContext(Dispatchers.IO) {
        val list = DraftModel.getAll()
        _drafts.postValue(list)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        DraftModel.deleteAll()
        getAll()
    }

    suspend fun delete(position: Int) = withContext(Dispatchers.IO) {
        val item = drafts.value?.getOrNull(position) ?: return@withContext
        DraftModel.delete(item)
        getAll()
    }
}