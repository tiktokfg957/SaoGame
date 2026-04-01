package com.example.saoclicker.data.database

import androidx.room.*
import com.example.saoclicker.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement)

    @Update
    suspend fun update(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE isCompleted = 0")
    fun getIncompleteAchievements(): Flow<List<Achievement>>
}
