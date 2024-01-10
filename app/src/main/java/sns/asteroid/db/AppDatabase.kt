package sns.asteroid.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import sns.asteroid.db.dao.*
import sns.asteroid.db.entities.*
import java.io.File

@Database(
    entities = [Credential::class, AppSetting::class, ColumnInfo::class, RecentlyHashtag::class, Draft::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun credentialDao(): CredentialDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun columnInfoDao(): ColumnInfoDao
    abstract fun hashtagDao(): HashtagDao
    abstract fun draftDao(): DraftDao

    companion object {
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val password = generatePassword(context)
                Room.databaseBuilder(context, AppDatabase::class.java, "db-primary")
                    .addMigrations(Migration1to2())
                    .addMigrations(Migration2to3())
                    .addMigrations(Migration3to4())
                    .openHelperFactory(SupportFactory(password.toByteArray()))
                    .build()
            }
        }

        /**
         * 暗号化されたSharedPreferencesからDBのパスワードを取得
         * パスワードが見つからない場合は
         * 新たに生成&合わせてDBを暗号化
         */
        private fun generatePassword(context: Context): String {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val fileName = "secret"
            val sp = EncryptedSharedPreferences(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            val password = sp.getString("database", null)

            return if (password.isNullOrBlank()) {
                val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

                val random = (1..32)
                    .map { charset.random() }
                    .joinToString("")

                with(sp.edit()) {
                    putString("database", random)
                    apply()
                }

                encrypt(context, password = random)

                random
            } else
                password
        }

        /**
         * 引数のパスワードでDBを暗号化する
         */
        private fun encrypt(context: Context, password: String) {
            SQLiteDatabase.loadLibs(context)

            val oldFile = context.getDatabasePath("db-primary").also {
                if (!it.exists()) return
            }

            val database = SQLiteDatabase.openDatabase(oldFile.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE)
            val version = database.version.also { database.close() }

            val newFile = File.createTempFile("sqltmp", "tmp", context.cacheDir)
            val newDatabase = SQLiteDatabase.openDatabase(newFile.absolutePath, password, null, SQLiteDatabase.OPEN_READWRITE, null, null)

            newDatabase.compileStatement("ATTACH DATABASE ? AS plaintext KEY ''").also {
                it.bindString(1, oldFile.absolutePath)
                it.execute()
            }
            newDatabase.also {
                it.rawExecSQL("SELECT sqlcipher_export('main', 'plaintext')")
                it.rawExecSQL("DETACH DATABASE plaintext")
                it.version = version
                it.close()
            }

            oldFile.delete()
            newFile.renameTo(oldFile)
        }
    }

    class Migration1to2: Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // 何故かmigrate()が2回呼ばれてエラるときあるので仕方なく例外キャッチしてる
                database.execSQL("ALTER TABLE credential ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
            }
        }
    }

    class Migration2to3: Migration(2,3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE RecentlyHashtag(hashtag TEXT NOT NULL, last_at TEXT NOT NULL, PRIMARY KEY(hashtag))")
        }
    }

    class Migration3to4: Migration(3,4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // SQLiteだとBooleanが無いのでIntegerを使用する
            database.execSQL("CREATE TABLE Draft(" +
                    "id INTEGER NOT NULL," +
                    "content TEXT NOT NULL," +
                    "language TEXT NOT NULL," +
                    "visibility TEXT NOT NULL," +
                    "spoiler_text TEXT NOT NULL," +
                    "poll1 TEXT NOT NULL," +
                    "poll2 TEXT NOT NULL," +
                    "poll3 TEXT NOT NULL," +
                    "poll4 TEXT NOT NULL," +
                    "poll_multiple INTEGER NOT NULL," +
                    "expire_day INTEGER NOT NULL," +
                    "expire_hour INTEGER NOT NULL," +
                    "expire_min INTEGER NOT NULL," +
                    "PRIMARY KEY(id))"
            )
        }
    }
}
