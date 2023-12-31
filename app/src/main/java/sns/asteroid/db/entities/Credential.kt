package sns.asteroid.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * このアプリで使用するアカウントの認証情報
 * (インスタンスのURLやアクセストークンなど)
 *
 * uid format
 * myusername@example.com
 */
@Entity
data class Credential(
    @PrimaryKey val acct: String,
    @ColumnInfo(name = "account_id") val account_id: String,
    @ColumnInfo(name = "access_token") val accessToken: String,
    @ColumnInfo(name = "instance") val instance: String,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "avatar_static") val avatarStatic: String,
    @ColumnInfo(name = "accent_color") val accentColor: Int,
    @ColumnInfo(name = "priority") var priority: Int = 0,
): Serializable
