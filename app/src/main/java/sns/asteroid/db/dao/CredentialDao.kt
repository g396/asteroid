package sns.asteroid.db.dao

import androidx.room.*
import sns.asteroid.db.entities.Credential

@Dao
interface CredentialDao {
    @Query("SELECT * FROM Credential ORDER BY Credential.priority ASC")
    fun getAll(): List<Credential>

    @Query("SELECT * FROM Credential WHERE acct= :acct")
    fun get(acct: String): Credential?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(credential: Credential)

    @Update
    fun updateAll(credential: List<Credential>)

    @Delete
    fun delete(credential: Credential)

    @Query("UPDATE Credential SET accent_color = :color WHERE acct = :acct")
    fun updateAccentColor(acct: String, color: Int)


    @Query("UPDATE Credential SET avatar_static= :avatarUrl WHERE acct = :acct")
    fun updateAvatar(acct: String, avatarUrl: String)

    @Query("UPDATE Credential SET access_token= :accessToken WHERE acct = :acct")
    fun updateAccessToken(acct: String, accessToken: String)
}