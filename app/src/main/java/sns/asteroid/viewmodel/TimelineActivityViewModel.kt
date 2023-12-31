package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.RecentlyHashtagModel

class TimelineActivityViewModel : ViewModel() {

    private var _credentials = MutableLiveData<List<Credential>>()
    val credentials: LiveData<List<Credential>>
        get() = _credentials

    private var _columns = MutableLiveData<List<Pair<ColumnInfo, Credential>>>()
    val columns: LiveData<List<Pair<ColumnInfo, Credential>>>
        get() = _columns

    private val _hashtags = MutableLiveData<List<String>>()
    val hashtags: LiveData<List<String>>
        get() = _hashtags

    private val _selectedHashtag = MutableLiveData<String>()
    val selectedHashtag: LiveData<String>
        get() = _selectedHashtag

    var isStandbyClose = false

    suspend fun loadCredentials() = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(CustomApplication.getApplicationContext())
        val result = db.credentialDao().getAll()
        if(credentials.value != result)
            _credentials.postValue(result)
    }

    suspend fun loadColumns() = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(CustomApplication.getApplicationContext())
        val result = db.columnInfoDao().getAllJoinMyAccount().toList()
        _columns.postValue(result)
    }

    suspend fun loadHashtags() = withContext(Dispatchers.IO) {
        val hashtags = RecentlyHashtagModel.getAll()
        _hashtags.postValue(hashtags)
    }

    fun setSelectedHashtag(hashtag: String) {
        _selectedHashtag.value = hashtag
    }

    /**
     * バックキー押す
     * ↓
     * 3秒以内にもう一度押されたらアプリ終了
     * 用のフラグ
     */
    suspend fun standbyClose() {
        withContext(Dispatchers.IO) {
            isStandbyClose = true
            Thread.sleep(3000)
            isStandbyClose = false
        }
    }
}