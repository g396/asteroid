package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.ColumnInfoModel
import sns.asteroid.model.streaming.ListStreamingClient
import sns.asteroid.model.streaming.StreamingClient
import sns.asteroid.model.user.ListsModel

class ListTimelineViewModel(
    columnInfo: ColumnInfo,
    credential: Credential,
): TimelineStreamingViewModel(columnInfo, credential) {
    override val streamingClient: StreamingClient =
        ListStreamingClient(this, credential.instance, credential.accessToken, columnInfo.option_id)

    private val listModel = ListsModel(credential)

    private val _list = MutableLiveData<ListTimeline>()
    val list: LiveData<ListTimeline> get() = _list

    suspend fun getInfo() = withContext(Dispatchers.IO) {
        val result = listModel.get(columnInfo.option_id)
        val info = result.lists?.firstOrNull() ?: return@withContext

        _list.postValue(info)
        ColumnInfoModel.updateListTitle(info.title, columnInfo.hash)
    }

    suspend fun setExclusive(exclusive: Boolean) = withContext(Dispatchers.IO) {
        val current = list.value ?: return@withContext
        val result = listModel.updateList(current.id, current.title, current.replies_policy, exclusive)

        val info = result.lists?.firstOrNull() ?: return@withContext
        _list.postValue(info)

    }

    suspend fun setRepliesPolicy(policy: String) = withContext(Dispatchers.IO) {
        val current = list.value ?: return@withContext
        val result = listModel.updateList(current.id, current.title, policy, current.exclusive)

        val info = result.lists?.firstOrNull() ?: return@withContext
        _list.postValue(info)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val columnInfo: ColumnInfo, private val credential: Credential): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListTimelineViewModel(columnInfo, credential) as T
        }
    }
}