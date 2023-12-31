package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.ColumnInfoModel

class ManageColumnsViewModel: ViewModel() {
    private val _columns = MutableLiveData<List<Pair<ColumnInfo, Credential>>>()
    val columns: LiveData<List<Pair<ColumnInfo, Credential>>> get() = _columns

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    /**
     * カラムの一覧を取得する
     */
     suspend fun getAll() = withContext(Dispatchers.IO) {
         val columns = ColumnInfoModel.getAll()
         _columns.postValue(columns)
     }

    /**
     * カラムを追加する
     * DBが-1を返す(既に同じカラムが存在する)場合は追加しない
     */
    suspend fun addItem(item: Pair<ColumnInfo, Credential>) = withContext(Dispatchers.IO) {
        val result = ColumnInfoModel.insert(item.first)

        if(result == -1L)
            _toastMessage.postValue(getString(R.string.already_exist))
                .also { return@withContext }

        val columns = _columns.value!!.toMutableList().apply {
            add(item)
        }
        _columns.postValue(columns)
    }

    /**
     * カラムを削除する
     */
    suspend fun deleteItem(item: Pair<ColumnInfo, Credential>) = withContext(Dispatchers.IO) {
        val columns = _columns.value!!.toMutableList().apply {
            remove(item)
        }

        for((index, pair) in columns.withIndex()) {
            pair.first.priority = index
        }
        _columns.postValue(columns)

        ColumnInfoModel.delete(item.first)
        ColumnInfoModel.update(columns.unzip().first)
    }

    /**
     * カラムの並び替えを検知し、その通りに並び替える
     *
     * 全体を非同期処理にすると、高速で移動を繰り返した際に不整合が生じるので
     * DB書き込みだけ非同期にする
     */
    suspend fun move(from: Int, to: Int) {
        if(from == to) return

        val columns = _columns.value!!.toMutableList().apply {
            val moving = get(from)
            remove(moving)
            add(to, moving)
        }
        for((index, item) in columns.withIndex()) {
            item.first.priority = index
        }
        _columns.value = columns

        withContext(Dispatchers.IO) { ColumnInfoModel.update(columns.unzip().first) }
    }
    
    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}