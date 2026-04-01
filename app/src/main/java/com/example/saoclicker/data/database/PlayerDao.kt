package com.example.saoclicker.data.database

import androidx.room.*
import com.example.saoclicker.data.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player LIMIT 1")
    fun getPlayer(): Flow<Player>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player)

    @Update
    suspend fun update(player: Player)
}
