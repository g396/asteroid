package sns.asteroid.model

import sns.asteroid.CustomApplication
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.Draft

class DraftModel {
    companion object {
        private val context get() = CustomApplication.getApplicationContext()

        fun getAll(): List<Draft> {
            val db = AppDatabase.getDatabase(context)
            val dao = db.draftDao()
            return dao.getAll().also { db.close() }
        }

        fun deleteAll() {
            val db = AppDatabase.getDatabase(context)
            val dao = db.draftDao()
            dao.deleteAll().also { db.close() }
        }

        fun delete(draft: Draft) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.draftDao()
            dao.delete(draft).also { db.close() }
        }

        fun insert(draft: Draft) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.draftDao()
            dao.insert(draft).also { db.close() }
        }

        fun insert(
            id: Int,
            content: String?,
            languageCode: String?,
            visibility: String?,
            spoilerText: String?,
            pollValue1: String?,
            pollValue2: String?,
            pollValue3: String?,
            pollValue4: String?,
            pollMultiple: Boolean?,
            expireDay: Int,
            expireHour: Int,
            expireMin: Int,
        ) {
            val draft = Draft(
                id = id,
                content = content ?: "",
                language = languageCode ?: "",
                visibility = visibility ?: "",
                spoilerText = spoilerText ?: "",
                pollValue1 = pollValue1 ?: "",
                pollValue2 = pollValue2 ?: "",
                pollValue3 = pollValue3 ?: "",
                pollValue4 = pollValue4 ?: "",
                pollMultiple = pollMultiple ?: false,
                expireDay = expireDay,
                expireHour = expireHour,
                expireMin = expireMin,
            )
            insert(draft)
        }
    }
}