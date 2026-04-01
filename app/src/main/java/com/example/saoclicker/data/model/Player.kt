package com.example.saoclicker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,  // одна запись
    var level: Int = 1,
    var experience: Int = 0,
    var col: Int = 0,  // валюта
    var skillPoints: Int = 0,
    var strength: Int = 1,    // урон за клик
    var agility: Int = 0,     // шанс крита (каждые 10 = +1% шанса, максимум 50%)
    var vitality: Int = 0,    // максимальное здоровье игрока (если введём) – для простоты пока не используем
    var attackSpeed: Int = 0, // авто-удары: каждые 10 очков дают +1 удар в секунду
    var weaponLevel: Int = 1   // улучшение оружия (дополнительный урон)
)
