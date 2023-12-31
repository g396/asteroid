package sns.asteroid.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Application
import sns.asteroid.model.other_api.AppsModel
import sns.asteroid.model.other_api.AuthorizeModel

class AuthorizeViewModel: ViewModel() {
    val serverDomain = MutableLiveData<String>()
    val accessToken = MutableLiveData<String>()
    val appName = MutableLiveData<String>()

    private val _authorizeUri = MutableLiveData<Uri>()
    val authorizeUri: MutableLiveData<Uri> get() = _authorizeUri

    private val _application = MutableLiveData<Application>()
    val application: MutableLiveData<Application> get() = _application

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    init {
        serverDomain.value = ""
        accessToken.value = ""
        appName.value = ""
    }

    /**
     * クライアントのIDとシークレットの取得
     */
    suspend fun createApps(isCustomName: Boolean) {
        val client = if (isCustomName) AppsModel(serverDomain.value!!, appName.value!!)
        else AppsModel(serverDomain.value!!, "")

        val result = withContext(Dispatchers.IO) { client.createApps() }
        result.toastMessage?.let { _toastMessage.value = it }

        if (result.application == null) return

        application.value = result.application
        authorizeUri.value = AuthorizeModel(serverDomain.value!!).generateUri(result.application)
    }

    /**
     * ブラウザでアカウントを認証すると、アクセストークンを得るためのコードが貰えるので
     * それを鯖に投げつけてアクセストークンを得る
     */

    suspend fun obtainToken(code: String): Boolean {
        val client = AuthorizeModel(serverDomain.value!!)
        val result = withContext(Dispatchers.IO) {client.obtainToken(code, application.value!!) }

        return if(!result.isSuccess) {
            result.toastMessage?.let { _toastMessage.value = it }
            false
        } else true
    }

    /**
     * EditTextに入力したアクセストークンを使用して直接認証する
     */
    suspend fun verifyAccessToken(): Boolean {
        val client = AuthorizeModel(serverDomain.value!!)
        val result = withContext(Dispatchers.IO) { client.verifyAccessToken(accessToken.value!!) }

        return if(!result.isSuccess) {
            result.toastMessage?.let { _toastMessage.value = it }
            false
        } else true
    }
}