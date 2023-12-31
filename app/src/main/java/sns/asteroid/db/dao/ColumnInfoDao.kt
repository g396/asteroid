package sns.asteroid.db.dao

import androidx.room.*
import sns.asteroid.db.entities.Credential
import sns.asteroid.db.entities.ColumnInfo

@Dao
interface ColumnInfoDao {
    @Query("SELECT * FROM ColumnInfo ORDER BY ColumnInfo.priority ASC")
    fun getAll(): List<ColumnInfo>

    @Query(
        "SELECT * FROM ColumnInfo JOIN Credential ON ColumnInfo.acct = Credential.acct " +
                "ORDER BY ColumnInfo.priority ASC"
    )
    fun getAllJoinMyAccount(): Map<ColumnInfo, Credential>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg columnInfos: ColumnInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(columnInfo: ColumnInfo): Long

    @Update
    fun updateAll(vararg columnInfo: ColumnInfo)

    @Update
    fun updateAll(columnInfos: List<ColumnInfo>)

    @Query(
        "UPDATE ColumnInfo SET option_title = :title WHERE hash = :hash ")
    fun updateListTitle(title: String, hash: String)

    @Query("DELETE FROM ColumnInfo")
    fun deleteAll()

    @Delete
    fun deleteAll(vararg columnInfos: ColumnInfo)

    @Delete
    fun deleteAll(columnInfo: List<ColumnInfo>)
}

