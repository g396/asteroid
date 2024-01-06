package sns.asteroid.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import java.io.Serializable
import java.security.MessageDigest

/**
 * TimelineActivityで表示する要素(LTL,Home等)のリスト
 *
 * acct format
 * myusername@example.com
 */
@Entity(primaryKeys = ["acct", "subject", "option_id"])
data class ColumnInfo (
    val acct: String,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "option_id") val option_id: String,
    @ColumnInfo(name = "option_title") val option_title: String,
    @ColumnInfo(name = "priority") var priority: Int,
    @ColumnInfo(name = "streaming") val streaming: Boolean = true,
    @ColumnInfo(name = "hash") val hash: String = getHash(),
): Serializable {
    @Ignore constructor(acct:String, subject: String, priority: Int):
            this(acct, subject, "", "", priority)

    companion object {
        private fun getHash(): String {
            val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

            val random = (1..24)
                .map { charset.random() }
                .joinToString("")

            return MessageDigest.getInstance("MD5")
                .digest(random.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
    }
}
