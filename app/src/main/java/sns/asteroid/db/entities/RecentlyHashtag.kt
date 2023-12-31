package sns.asteroid.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentlyHashtag(
    @PrimaryKey val hashtag: String,
    @ColumnInfo(name = "last_at") val lastAt: String,
)