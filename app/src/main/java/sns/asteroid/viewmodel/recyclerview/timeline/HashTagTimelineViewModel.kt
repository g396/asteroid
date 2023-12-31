package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.streaming.HashtagStreamingClient
import sns.asteroid.model.streaming.StreamingClient
import sns.asteroid.model.user.TagsModel

class HashTagTimelineViewModel(columnInfo: ColumnInfo, credential: Credential):
    TimelineStreamingViewModel(columnInfo, credential) {
    override val streamingClient =
        HashtagStreamingClient(this, credential.instance, credential.accessToken, columnInfo.option_id)

    private val _tag = MutableLiveData<Tag>()
    val tag: LiveData<Tag> get() = _tag

    val model = TagsModel(credential)

    suspend fun getTag() = withContext(Dispatchers.IO) {
        val id = columnInfo.option_id
        val result = model.getTag(id)
        result.tag?.let { _tag.postValue(it) }
    }

    suspend fun followTag() = withContext(Dispatchers.IO) {
        val id = columnInfo.option_id
        val result = model.followTag(id)
        result.tag?.let { _tag.postValue(it) }
        result.message.let { toastMessage.postValue(it) }
    }

    suspend fun unfollowTag() = withContext(Dispatchers.IO) {
        val id = columnInfo.option_id
        val result = model.unfollowTag(id)
        result.tag?.let { _tag.postValue(it) }
        result.message.let { toastMessage.postValue(it) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val columnInfo: ColumnInfo, private val credential: Credential): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HashTagTimelineViewModel(columnInfo, credential) as T
        }
    }
}