package my.storyapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeysEntity: List<RemoteKeyEntity>)

    @Query("select * from remote_keys where id = :id")
    suspend fun getRemoteKeysId(id: String): RemoteKeyEntity?

    @Query("delete from remote_keys")
    suspend fun deleteRemoteKeys()
}
