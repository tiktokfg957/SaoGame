package com.example.saoclicker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upgrades")
data class Upgrade(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var description: String,
    var basePrice: Int,
    var currentPrice: Int,
    var effect: String, // "weapon", "autoClicker", "potion" и т.д.
    var effectValue: Int, // например, +5 к урону
    var ownedCount: Int = 0
)
