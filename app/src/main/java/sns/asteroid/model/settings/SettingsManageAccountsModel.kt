package sns.asteroid.model.settings

import sns.asteroid.CustomApplication
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.Credential

class SettingsManageAccountsModel {
    fun getCredentials(): MutableList<Credential> {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        val list = db.credentialDao().getAll()
        return list.toMutableList()
    }

    fun getCredential(acct: String): Credential? {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
       return db.credentialDao().get(acct)
    }

    fun removeCredential(credential: Credential) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)

        // 削除したアカウントのカラムも消す
        val columns = db.columnInfoDao().getAll().filter { it.acct == credential.acct }
        db.columnInfoDao().deleteAll(columns)

        db.credentialDao().delete(credential)
    }

    fun changeAccentColor(credential: Credential, color: Int) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        db.credentialDao().updateAccentColor(credential.acct, color)
    }

    fun updateAvatar(credential: Credential, avatarUrl: String) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        db.credentialDao().updateAvatar(credential.acct, avatarUrl)
    }

    fun updateAll(list: List<Credential>) {
        val context = CustomApplication.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        db.credentialDao().updateAll(list)
    }
}