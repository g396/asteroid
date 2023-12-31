package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.hashtag.FollowedTagsModel
import sns.asteroid.model.settings.RecentlyHashtagModel
import java.time.LocalDateTime
import java.time.ZoneId

class RecentlyHashtagsViewModel: ViewModel() {
    private val _hashtags = MutableLiveData<Set<String>>()
    val hashtags: LiveData<Set<String>> get() = _hashtags

    init {
        viewModelScope.launch { loadHashtags() }
    }

    suspend fun loadHashtags() = withContext(Dispatchers.IO) {
        val hashtags = RecentlyHashtagModel.getAll()
        _hashtags.postValue(hashtags.toSet())
    }

    suspend fun remove(hashtag: String) = withContext(Dispatchers.IO) {
        RecentlyHashtagModel.remove(hashtag)
        _hashtags.postValue(hashtags.value!!.filterNot { it == hashtag }.toSet())
    }

    suspend fun add(hashtag: String) = withContext(Dispatchers.IO) {
        //鯖が返してくる時刻がUTCなのでそれに合わせる
        val now = LocalDateTime.now(ZoneId.of("UTC"))
        RecentlyHashtagModel.insertOrUpdate(listOf(hashtag), now.toString())
        _hashtags.postValue(setOf(hashtag).plus(hashtags.value!!))
    }

    suspend fun importHashtags(credential: Credential) = withContext(Dispatchers.IO) {
        val model = FollowedTagsModel(credential)
        val set = mutableSetOf<String>()

        // 最大40*10=400個までフォローしているタグを取得
        for(i in 1..10) {
            val result = model.getOlder()
            if(!result.isSuccess) break

            val tag = result.contents?.map { it.name } ?: break
            if(tag.isEmpty()) break

            set.addAll(tag)
        }
        val now = LocalDateTime.now(ZoneId.of("UTC"))
        RecentlyHashtagModel.insertOrUpdate(set.toList(), now.toString())
        _hashtags.postValue(set.plus(hashtags.value!!))
    }
}