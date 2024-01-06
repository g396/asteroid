package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Poll
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.SearchModel
import sns.asteroid.model.user.FedibirdActionModel
import sns.asteroid.model.user.PollModel
import sns.asteroid.model.user.StatusesModel

interface StatusViewModelInterface {
    val credential: LiveData<Credential>
    val toastMessage: MutableLiveData<String>

    suspend fun deleteStatus(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).deleteStatus(statusId) }
        if(result.isSuccess) removeStatus(statusId)
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postFavourite(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postFavourite(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postFavourite(credential: Credential, uri: String) = withContext(Dispatchers.IO) {
        val status = SearchModel(credential).findStatus(uri)
            ?: return@withContext false.also { toastMessage.postValue("Not found") }
        val result = StatusesModel(credential).postFavourite(status.id)
        toastMessage.postValue(result.toastMessage)
        return@withContext result.isSuccess
    }

    suspend fun postUnFavourite(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postUnFavourite(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBoost(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postBoost(statusId, StatusesModel.Visibility.NONE) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBoostPublic(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postBoost(statusId, StatusesModel.Visibility.PUBLIC) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBoostUnlisted(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postBoost(statusId, StatusesModel.Visibility.UNLISTED) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBoostPrivate(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postBoost(statusId, StatusesModel.Visibility.PRIVATE) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBoost(credential: Credential, uri: String) = withContext(Dispatchers.IO) {
        val status = SearchModel(credential).findStatus(uri)
            ?: return@withContext false.also { toastMessage.postValue("Not found") }
        val result = StatusesModel(credential).postBoost(status.id, StatusesModel.Visibility.NONE)
        toastMessage.postValue(result.toastMessage)
        return@withContext result.isSuccess
    }

    suspend fun postUnBoost(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postUnBoost(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBookmark(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postBookMark(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun postBookmark(credential: Credential, uri: String) = withContext(Dispatchers.IO) {
        val status = SearchModel(credential).findStatus(uri)
            ?: return@withContext false.also { toastMessage.postValue("Not found") }
        val result = StatusesModel(credential).postBookMark(status.id)
        toastMessage.postValue(result.toastMessage)
        return@withContext result.isSuccess
    }

    suspend fun postUnBookmark(statusId: String): Boolean {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postUnBookmark(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
        return result.isSuccess
    }

    suspend fun pinThePosts(statusId: String) {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postPin(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
    }

    suspend fun unPinThePosts(statusId: String) {
        val result = withContext(Dispatchers.IO) { StatusesModel(credential.value!!).postUnPin(statusId) }
        result.status?.let { replaceContent(it) }
        toastMessage.value = result.toastMessage
    }

    suspend fun vote(id: String, choices: List<Int>) {
        withContext(Dispatchers.IO) {
            val result = PollModel(credential.value!!).vote(id, choices)

            result.toastMessage?.let { toastMessage.postValue(it) }
            result.poll?.let { updatePoll(it) }
        }
    }

    suspend fun putEmojiAction(statusId: String, emoji: String): Boolean {
        val result = withContext(Dispatchers.IO) { FedibirdActionModel(credential.value!!).putEmojiReactions(statusId, emoji) }
        toastMessage.value = result.toastMessage
        result.status?.let { replaceContent(it) }
        return result.isSuccess
    }

    suspend fun deleteEmojiAction(statusId: String, emoji: String): Boolean {
        val result = withContext(Dispatchers.IO) { FedibirdActionModel(credential.value!!).deleteEmojiReactions(statusId, emoji) }
        toastMessage.value = result.toastMessage
        result.status?.let { replaceContent(it) }
        return result.isSuccess
    }

    fun replaceContent(status: Status)
    fun removeStatus(statusId: String)
    fun updatePoll(poll: Poll)
}