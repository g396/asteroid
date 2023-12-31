package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.StatusesTrendsModel

class StatusesTrendsViewModel(
    columnInfo: ColumnInfo, credential: Credential
): TimelineViewModel(columnInfo, credential) {
    override val timelineModel = StatusesTrendsModel(credential)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val columnInfo: ColumnInfo, private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatusesTrendsViewModel(columnInfo, credential) as T
        }
    }
}