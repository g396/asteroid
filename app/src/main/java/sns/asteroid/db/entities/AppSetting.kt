package sns.asteroid.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * acct format
 * myusername@example.com
 */
@Entity
data class AppSetting(
    @PrimaryKey val acct: String,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "show_dialog_all") val showDialog: Boolean,
    @ColumnInfo(name = "show_dialog_toot") val showDialogToot: Boolean,
    @ColumnInfo(name = "show_dialog_boost") val showDialogBoost: Boolean,
    @ColumnInfo(name = "show_dialog_favourite") val showDialogFavourite: Boolean,
)