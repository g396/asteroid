package sns.asteroid.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sns.asteroid.db.entities.Draft

@Dao
interface DraftDao {
    @Query("SELECT * FROM Draft ORDER BY Draft.id DESC")
    fun getAll(): List<Draft>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(draft: Draft): Long

    @Delete
    fun delete(draft: Draft)

    @Query("DELETE FROM Draft WHERE :id = id")
    fun delete(id: Int)

    @Query("DELETE FROM Draft")
    fun deleteAll()

}