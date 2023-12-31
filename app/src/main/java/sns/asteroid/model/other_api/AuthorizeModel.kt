package sns.asteroid.model.other_api

import android.net.Uri
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.OAuth
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Application
import sns.asteroid.api.entities.Token
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.ColorPreset

class AuthorizeModel(private val server: String) {
    data class Result(
        val isSuccess: Boolean,
        val toastMessage: String?,
    )

    fun generateUri(application: Application): Uri? {
        val client = OAuth(server, application)
        val uri = client.generateUrlToAuthorize()
        return Uri.parse(uri.toString())
    }

    fun obtainToken(code: String, application: Application): Result {
        val client = OAuth(server, application)
        val response = client.obtainToken(code)
            ?: return Result(isSuccess = false, toastMessage = null)

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val token = json.decodeFromString(Token.serializer(), response.body!!.string())

        return verifyAccessToken(token.access_token)
    }

    /**
     * acctのドメイン部分は自分自身のサーバーだと省略されてしまうので
     * api/v1/instanceから別途取得するようにしてます
     */
    fun verifyAccessToken(accessToken: String): Result {
        val client = OAuth(server)
        val response = client.verifyCredentials(accessToken)
            ?: return Result(isSuccess = false, toastMessage = null)

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val account = json.decodeFromString(Account.serializer(), response.body!!.string())

        val domain = let {
            val result = InstanceModel(server).getInstanceV1()
            result.instance?.uri
                ?: return Result(isSuccess = false, toastMessage = result.toastMessage)
        }

        addColumns(domain, account)
        saveCredential(domain, account, accessToken)

        return Result(isSuccess = true, toastMessage = null)
            .also { response.close() }
    }




    /**
     * アカウントが認証できたらカラムを追加する
     */
    private fun addColumns(domain:String, account: Account) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        val dao = db.columnInfoDao()

        val isFirst = let {
            val accounts = db.credentialDao().getAll()
            accounts.isEmpty()
        }

        if(isFirst) dao.insertAll(
            ColumnInfo("${account.username}@${domain}", "local", 0),
            ColumnInfo("${account.username}@${domain}", "home", 1),
            ColumnInfo("${account.username}@${domain}", "public", 2),
            ColumnInfo("${account.username}@${domain}", "notification", 3),)
        else {
            val rows = dao.getAll().size
            dao.insertAll(ColumnInfo("${account.username}@${domain}", "local", rows))
        }

        db.close()
    }

    /**
     * データベースに保存
     *
     */
    private fun saveCredential(domain: String, account: Account, accessToken: String) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        val dao = db.credentialDao()

        val acct = "${account.username}@${domain}"

        // 再認証時はアクセストークンのみ更新
        val current = dao.get(acct)
        if (current != null) {
            dao.updateAccessToken(acct, accessToken)
            dao.updateAvatar(acct, account.avatar_static)
            db.close()
            return
        }


        // 1つ目のアカウントには青、それ以降は被らない色をランダムに割り当てる
        val accentColor = let {
            val credentials = dao.getAll()
            if (credentials.isEmpty())
                return@let context.resources.getColor(R.color.blue)

            val usedColorSet = credentials.associateBy { it.accentColor } .keys
            val preset = ColorPreset.getAll()
            val notUsed = preset.filterNot { usedColorSet.contains(it) }
            if (notUsed.isEmpty())
                preset.shuffled().first()
            else
                notUsed.shuffled().first()
        }

        val credential = Credential(
            acct = acct,
            account_id = account.id,
            accessToken = accessToken,
            instance = server,
            screenName = account.username,
            avatarStatic = account.avatar_static,
            accentColor = accentColor
        )

        dao.insertOrReplace(credential)
        db.close()
    }
}
