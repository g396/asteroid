package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.ContextModel

class StatusDetailViewModel(columnInfo: ColumnInfo, credential: Credential, val status: Status):
    TimelineViewModel(columnInfo, credential) {

    override val timelineModel = ContextModel(credential, status)
    init {
        _contents.value = listOf(status)
    }

    suspend fun getParentAndChildStatuses() {
        val result = withContext(Dispatchers.IO) { timelineModel.getLatest() }
        if (result.isSuccess) {
            result.contents?.let { _contents.value = it }
        } else {
            result.toastMessage?.let { toastMessage.value = it }
        }

    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val credential: Credential,
        private val status: Status
        ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val columnInfo = ColumnInfo("dummy", "dummy", -1)
            return StatusDetailViewModel(columnInfo, credential, status) as T
        }
    }
}