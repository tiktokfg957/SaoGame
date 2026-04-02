package com.example.saoclicker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var description: String,
    var requirementType: String, // "kill", "stat_strength" и т.д.
    var requirementValue: Int,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
    var rewardCol: Int,
    var rewardExp: Int
)
