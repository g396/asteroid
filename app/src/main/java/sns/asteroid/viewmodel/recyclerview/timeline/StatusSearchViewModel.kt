package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.StatusesSearchModel

class StatusSearchViewModel(
    columnInfo: ColumnInfo, credential: Credential, query: String, statuses: List<Status>
): TimelineViewModel(columnInfo, credential) {
    override val timelineModel = StatusesSearchModel(credential, query, offset = statuses.size)

    init {
        _contents.value = statuses
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val credential: Credential,
        private val query: String,
        private val statuses: List<Status>,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val columnInfo = ColumnInfo("dummy", "search", -1)
            return StatusSearchViewModel(columnInfo, credential, query, statuses) as T
        }
    }
}