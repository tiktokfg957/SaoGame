package com.example.saoclicker.data.database

import androidx.room.*
import com.example.saoclicker.data.model.Upgrade
import kotlinx.coroutines.flow.Flow

@Dao
interface UpgradeDao {
    @Query("SELECT * FROM upgrades")
    fun getAllUpgrades(): Flow<List<Upgrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upgrade: Upgrade)

    @Update
    suspend fun update(upgrade: Upgrade)
}
