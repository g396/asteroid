package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.HashtagsSearchModel

class HashtagSearchViewModel(
    columnInfo: ColumnInfo,credential: Credential, query: String, tags: List<Tag>
): RecyclerViewViewModel<Tag>(columnInfo, credential) {
    override val timelineModel = HashtagsSearchModel(credential, query, offset = tags.size)

    init {
        _contents.value = tags
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val credential: Credential,
        private val query: String,
        private val tags: List<Tag>,
        ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val columnInfo = ColumnInfo("dummy", "search", -1)
            return HashtagSearchViewModel(columnInfo, credential, query, tags) as T
        }
    }
}