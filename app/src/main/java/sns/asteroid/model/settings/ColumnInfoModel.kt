package sns.asteroid.model.settings

import sns.asteroid.CustomApplication
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential

class ColumnInfoModel {
    companion object {
        fun getAll(): List<Pair<ColumnInfo, Credential>> {
            val context = CustomApplication.getApplicationContext()

            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()

            return dao.getAllJoinMyAccount().toList().also { db.close() }
        }

        fun insert(column: ColumnInfo): Long {
            val context = CustomApplication.getApplicationContext()

            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()

            return dao.insert(column).also { db.close() }
        }

        fun delete(column: ColumnInfo) {
            val context = CustomApplication.getApplicationContext()

            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()

            dao.deleteAll(column)
        }

        fun update(list: List<ColumnInfo>) {
            val context = CustomApplication.getApplicationContext()

            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()

            dao.updateAll(list)
        }

        fun updateListTitle(title: String, hash: String) {
            val context = CustomApplication.getApplicationContext()
            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()
            dao.updateListTitle(title, hash)
        }

        fun setIsEnableStreaming(enabled: Boolean, hash: String) {
            val context = CustomApplication.getApplicationContext()
            val db = AppDatabase.getDatabase(context)
            val dao = db.columnInfoDao()
            dao.updateStreaming(enabled, hash)
        }
    }
}