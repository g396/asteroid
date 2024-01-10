package sns.asteroid.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Draft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val language: String,
    val visibility: String,
    @ColumnInfo(name = "spoiler_text") val spoilerText: String,
    @ColumnInfo(name = "poll1") val pollValue1: String,
    @ColumnInfo(name = "poll2") val pollValue2: String,
    @ColumnInfo(name = "poll3") val pollValue3: String,
    @ColumnInfo(name = "poll4") val pollValue4: String,
    @ColumnInfo(name = "poll_multiple") val pollMultiple: Boolean,
    @ColumnInfo(name = "expire_day") val expireDay: Int,
    @ColumnInfo(name = "expire_hour") val expireHour: Int,
    @ColumnInfo(name = "expire_min") val expireMin: Int,
)
