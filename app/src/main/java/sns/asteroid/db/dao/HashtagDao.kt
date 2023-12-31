package sns.asteroid.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sns.asteroid.db.entities.RecentlyHashtag

@Dao
interface HashtagDao {
    @Query("SELECT * FROM RecentlyHashtag ORDER BY RecentlyHashtag.last_at DESC")
    fun getAll(): List<RecentlyHashtag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recentlyHashtag: RecentlyHashtag): Long

    @Query("DELETE FROM RecentlyHashtag WHERE hashtag = :hashtag")
    fun delete(hashtag: String)
}