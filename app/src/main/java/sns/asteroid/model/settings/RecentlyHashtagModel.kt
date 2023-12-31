package sns.asteroid.model.settings

import sns.asteroid.CustomApplication
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.RecentlyHashtag

class RecentlyHashtagModel {
    companion object {
        val context get() = CustomApplication.getApplicationContext()

        fun insertOrUpdate(hashtags: List<String>, lastAt: String) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.hashtagDao()

            hashtags.forEach {
                dao.insert(RecentlyHashtag(it, lastAt))
            }.also { db.close() }
        }

        fun remove(hashtag: String) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.hashtagDao()
            dao.delete(hashtag).also { db.close() }
        }

        fun getAll(): List<String> {
            val db = AppDatabase.getDatabase(context)
            val dao = db.hashtagDao()
            return dao.getAll().map { it.hashtag }.also { db.close() }
        }
    }
}