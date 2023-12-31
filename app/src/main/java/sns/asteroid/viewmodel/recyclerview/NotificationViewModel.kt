package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Notification
import sns.asteroid.api.entities.Poll
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.*
import sns.asteroid.model.streaming.NotificationStreamingClient
import sns.asteroid.model.streaming.AbstractStreamingClient
import sns.asteroid.model.timeline.NotificationTimelineModel
import sns.asteroid.model.user.UserActionModel

/**
 * 通知カラムのViewModel
 */
class NotificationViewModel(
    columnInfo: ColumnInfo,
    credential: Credential,
) : RecyclerViewViewModel<Notification>(columnInfo, credential), Streaming,
    AbstractStreamingClient.OnReceiveListener<Notification>, StatusViewModelInterface {
    override val timelineModel =
        if(columnInfo.subject == "mention") NotificationTimelineModel(credential, onlyMention = true)
        else NotificationTimelineModel(credential)

    override val streamingClient = NotificationStreamingClient(this, credential)

    /**
     * WebSocketで新着投稿を受信した際に呼び出される
     */
    override fun onUpdated(notification: Notification) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) { updateContent(notification) }
        }
    }

    /**
     * WebSocketでエラーが発生した際に呼び出される
     */
    override fun onMessage(message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) { toastMessage.value = message }
        }
    }

    /**
     * 新しくデータをリストに追加・同じIDで既に存在する場合は置換え
     */
    private fun updateContent(content: Notification) {
        val current = _contents.value?.find { it.id == content.id }

        if (current != null) {
            _contents.value?.toMutableList()?.apply {
                val index = indexOf(current)
                removeAt(index)
                add(index, content.copy().apply { status?.isSelected = current.status?.isSelected ?: false })
            }.let { _contents.postValue(it) }
        } else {
            _contents.value?.toMutableList()?.apply { add(0, content) }
                .let { _contents.postValue(it) }
        }
    }

    override fun updatePoll(poll: Poll) {
        val mutableList = contents.value!!.toMutableList()

        val statuses = contents.value!!.filter {
            it.status?.poll?.id == poll.id
        }
        statuses.forEach { s ->
            val new = s.copy().also {
                it.status = it.status?.copy()?.also { c ->
                    c.poll = poll
                }
            }
            val index = mutableList.indexOf(s)
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        _contents.postValue(mutableList)
    }

    override fun replaceContent(status: Status) {
        val mutableList = contents.value!!.toMutableList()

        val notifications = mutableList.filter {
            it.status?.id == status.id
        }
        notifications.forEach { n ->
            val new = n.copy().also {
                it.status = status
            }
            val index = mutableList.indexOf(n)
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        _contents.postValue(mutableList)
    }

    override fun removeStatus(statusId: String) {
    }

    suspend fun acceptFollowRequest(account: Account) = withContext(Dispatchers.IO) {
        val result = UserActionModel(credential.value!!).acceptFollowRequest(account)
        toastMessage.postValue(result.toastMessage)
        if (result.isSuccess) removeRequest(account)

    }

    suspend fun rejectFollowRequest(account: Account) = withContext(Dispatchers.IO) {
        val result = UserActionModel(credential.value!!).rejectFollowRequest(account)
        toastMessage.postValue(result.toastMessage)
        if (result.isSuccess) removeRequest(account)
    }

    /**
     * フォロリクを許可or拒否した後に通知欄から削除する
     */
    private fun removeRequest(account: Account) {
        val mutableList = contents.value!!.toMutableList()
        mutableList.removeIf { (it.account == account) and (it.type == "follow_request") }
        _contents.postValue(mutableList)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val column: ColumnInfo, private val credential: Credential): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(column, credential) as T
        }
    }
}