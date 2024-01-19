package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.ContentInterface
import sns.asteroid.api.entities.Status
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.RecentlyHashtagModel
import sns.asteroid.model.settings.SettingsManageAccountsModel
import sns.asteroid.model.timeline.GettingContentsModel
import sns.asteroid.model.user.StatusesModel

abstract class RecyclerViewViewModel<T: ContentInterface>(
    protected val columnInfo: ColumnInfo,
    credential: Credential
): ViewModel() {
    abstract val timelineModel: GettingContentsModel<T>

    protected val _credential = MutableLiveData<Credential>()
    val credential: LiveData<Credential> get() = _credential

    protected val _contents = MutableLiveData<List<T>>()
    val contents: LiveData<List<T>> get() = _contents

    private var _isLoaded = false //初回読み込み用フラグ(画面表示時にfalseにする)
    val isLoaded: Boolean get() = _isLoaded

    val toastMessage = MutableLiveData<String>()

    init {
        _credential.value = credential
        _contents.value = mutableListOf()
    }

    suspend fun reloadContents() {
        _contents.value = mutableListOf()
        val result = withContext(Dispatchers.IO) { timelineModel.reload() }
        result.contents?.let { addLatestContents(it) }
        result.toastMessage?.let { toastMessage.value = it }
    }

    suspend fun getLatestContents() {
        _isLoaded = true
        withContext(Dispatchers.IO) {
            val result = timelineModel.getLatest()
            result.contents?.let {
                addLatestContents(it)
            }
            result.toastMessage?.let {
                toastMessage.postValue(it)
            }
        }
    }

    suspend fun getOlderContents() {
        _isLoaded = true
        withContext(Dispatchers.IO) {
            val result = timelineModel.getOlder()
            result.contents?.let { addOlderContents(it) }
            result.toastMessage?.let { toastMessage.postValue(it) }
        }
    }

    suspend fun reloadCredential() {
        withContext(Dispatchers.IO) {
            val credential = SettingsManageAccountsModel().getCredential(credential.value!!.acct) ?: return@withContext
            if (_credential.value != credential)
                _credential.postValue(credential)
        }
    }

    suspend fun addColumn() {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        val dao = db.columnInfoDao()

        val result =  withContext(Dispatchers.IO) {
            val size = dao.getAll().size
            val add = when(columnInfo.subject) {
                "list" -> ColumnInfo(columnInfo.acct, columnInfo.subject, columnInfo.option_id, columnInfo.option_title, size)
                "hashtag" -> ColumnInfo(columnInfo.acct, columnInfo.subject, columnInfo.option_id, columnInfo.option_title, size)
                else -> ColumnInfo(columnInfo.acct, columnInfo.subject, size)
            }
            dao.insert(add)
        }

        val msg =
            if(result == -1L) getString(R.string.already_exist)
            else getString(R.string.added_column)

        toastMessage.postValue(msg)
    }

    suspend fun saveHashtag(status: Status?) = withContext(Dispatchers.IO) {
        if (status == null) return@withContext
        RecentlyHashtagModel.insertOrUpdate(status.tags.map { it.name }, status.created_at)
    }

    /**
     * 取得したコンテンツ(新しい投稿・フォロワー一覧等)をデータのリストに加える
     *
     * 重複チェックは
     * ストリーミングで先に取得してからRestAPIで値が返ってきた時に重複するケースがあるのでその対策
     */
    protected open suspend fun addLatestContents(list: List<T>) {
        val contents = contents.value!!.filter { item ->
            list.find { it.id == item.id } == null
        }
        val newList = list.plus(contents)
        _contents.postValue(newList)
    }

    /**
     * 取得したコンテンツ(古い投稿・フォロワー一覧等)をデータのリストに加える
     *
     * 重複チェックは
     * トレンドの取得等でoffsetがズレた際に重複するケースがあるのでその対策
     */
    private fun addOlderContents(list: List<T>) {
        val items = list.filter { item ->
            contents.value!!.find { it.id == item.id } == null
        }
        _contents.postValue(contents.value!!.plus(items))
    }

    open suspend fun postStatus(text: String, visibility: String) = withContext(Dispatchers.IO){
        val result = StatusesModel(credential.value!!).postStatuses(text, visibility).also {
            toastMessage.postValue(it.toastMessage)
            saveHashtag(it.status)
        }
        return@withContext result.isSuccess
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}