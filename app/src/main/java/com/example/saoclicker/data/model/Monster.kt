package com.example.saoclicker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monsters")
data class Monster(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var emoji: String,
    var level: Int,
    var maxHp: Int,
    var currentHp: Int,
    var rewardCol: Int,
    var rewardExp: Int
)
