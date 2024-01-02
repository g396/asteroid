package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.*
import kotlinx.coroutines.*
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.*
import sns.asteroid.model.streaming.MentionStreamingClient
import sns.asteroid.model.streaming.MixTimelineStreamingClient
import sns.asteroid.model.streaming.TimelineStreamingClient
import sns.asteroid.viewmodel.recyclerview.Streaming

/**
 * ストリーミング可能なタイムラインのViewModel
 */
open class TimelineStreamingViewModel(
    columnInfo: ColumnInfo,
    credential: Credential,
): TimelineViewModel(columnInfo, credential), Streaming, TimelineStreamingClient.OnReceiveListener {
    override val streamingClient = when(columnInfo.subject) {
        "mention" -> MentionStreamingClient(this, credential.instance, credential.accessToken)
        "mix"   -> MixTimelineStreamingClient(this, credential)
        else    -> TimelineStreamingClient(this, columnInfo, credential)
    }

    /**
     * 同じIDの投稿がすでにある場合は新しいデータで置換
     * 同じIDの投稿がない場合は新しいデータとして追加
     */
    private fun updateContent(content: Status) {
        val current = _contents.value?.find { it.id == content.id }

        if (current != null) {
            contents.value?.toMutableList()?.apply {
                val index = indexOf(current)
                removeAt(index)
                add(index, content)
            }.let { _contents.postValue(it) }
        } else {
            contents.value?.toMutableList()?.apply { add(0, content) }
                .let { _contents.postValue(it) }
        }
    }

    /**
     * WebSocketで新着投稿を受信した際に呼び出される
     */
    override fun onUpdated(status: Status) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                updateContent(status)
                checkUpdateMyAvatar(listOf(status))
            }
        }
    }

    /**
     * WebSocketで編集された投稿を受信した際に呼び出される
     */
    override fun onEdited(status: Status) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                replaceContent(status)
                checkUpdateMyAvatar(listOf(status))
            }
        }
    }

    /**
     * WebSocketで削除された投稿のIDを受信した際に呼び出される
     */
    override fun onDeleted(statusId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
               removeStatus(statusId)
            }
        }
    }

    /**
     * WebSocketでエラーが発生した際に呼び出される
     */
    override fun onMessage(message: String) {
        toastMessage.postValue(message)
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val columnInfo: ColumnInfo, private val credential: Credential): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineStreamingViewModel(columnInfo, credential) as T
        }
    }
}