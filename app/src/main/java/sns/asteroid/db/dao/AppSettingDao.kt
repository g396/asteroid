package sns.asteroid.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import sns.asteroid.db.entities.AppSetting

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM AppSetting WHERE acct = :acct LIMIT 1")
    fun loadAppSetting(acct: String): AppSetting

    @Insert
    fun insertAll(vararg appSettings: AppSetting)

    @Update
    fun update(appSetting: AppSetting)

    @Delete
    fun delete(appSetting: AppSetting)
}