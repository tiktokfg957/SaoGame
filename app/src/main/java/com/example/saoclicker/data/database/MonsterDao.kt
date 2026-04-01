package com.example.saoclicker.data.database

import androidx.room.*
import com.example.saoclicker.data.model.Monster
import kotlinx.coroutines.flow.Flow

@Dao
interface MonsterDao {
    @Query("SELECT * FROM monsters ORDER BY level ASC")
    fun getAllMonsters(): Flow<List<Monster>>

    @Query("SELECT * FROM monsters WHERE id = :id")
    suspend fun getMonsterById(id: Int): Monster?

    @Insert
    suspend fun insert(monster: Monster)

    @Update
    suspend fun update(monster: Monster)
}
