package sns.asteroid.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.other_api.AnnouncementsModel
import sns.asteroid.model.util.TimeFormatter

class AnnouncementsViewModel(credential: Credential): ViewModel() {
    private val model = AnnouncementsModel(credential)

    private val _announcements = MutableLiveData<String>()
    val announcements: LiveData<String> get() = _announcements

    suspend fun getAll() = withContext(Dispatchers.IO) {
        val result = model.getAll()

        val string = StringBuilder().apply {
            result.announcements?.forEach {
                append(TimeFormatter.formatAbsolute(it.published_at))
                append(it.content)
                append("\n")
            }
        }.toString()

        _announcements.postValue(string)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnnouncementsViewModel(credential) as T
        }
    }
}