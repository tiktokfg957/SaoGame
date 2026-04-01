package com.example.saoclicker.data.repository

import com.example.saoclicker.data.database.AppDatabase
import com.example.saoclicker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepository(private val db: AppDatabase) {

    fun getPlayer(): Flow<Player> = db.playerDao().getPlayer()
    suspend fun updatePlayer(player: Player) = db.playerDao().update(player)

    fun getAllMonsters(): Flow<List<Monster>> = db.monsterDao().getAllMonsters()
    suspend fun getCurrentMonster(): Monster? {
        val monsters = db.monsterDao().getAllMonsters().first()
        // Берём первого, у которого HP > 0, или первого в списке (если все мертвы)
        return monsters.firstOrNull { it.currentHp > 0 } ?: monsters.firstOrNull()
    }
    suspend fun updateMonster(monster: Monster) = db.monsterDao().update(monster)

    fun getAllUpgrades(): Flow<List<Upgrade>> = db.upgradeDao().getAllUpgrades()
    suspend fun updateUpgrade(upgrade: Upgrade) = db.upgradeDao().update(upgrade)

    fun getAllAchievements(): Flow<List<Achievement>> = db.achievementDao().getAllAchievements()
    suspend fun updateAchievement(achievement: Achievement) = db.achievementDao().update(achievement)

    suspend fun initDatabase() {
        // Игрок
        val player = db.playerDao().getPlayer().first()
        if (player.id == 0) {
            db.playerDao().insert(Player())
        }

        // Монстры
        val monsters = db.monsterDao().getAllMonsters().first()
        if (monsters.isEmpty()) {
            val monsterList = listOf(
                Monster(name = "Слайм", emoji = "🐌", level = 1, maxHp = 50, currentHp = 50, rewardCol = 10, rewardExp = 5),
                Monster(name = "Кабан", emoji = "🐗", level = 2, maxHp = 100, currentHp = 100, rewardCol = 20, rewardExp = 10),
                Monster(name = "Волк", emoji = "🐺", level = 3, maxHp = 150, currentHp = 150, rewardCol = 30, rewardExp = 15),
                Monster(name = "Гоблин", emoji = "👺", level = 4, maxHp = 200, currentHp = 200, rewardCol = 50, rewardExp = 25),
                Monster(name = "Скелет", emoji = "💀", level = 5, maxHp = 250, currentHp = 250, rewardCol = 70, rewardExp = 35),
                Monster(name = "Минотавр", emoji = "🐮", level = 6, maxHp = 350, currentHp = 350, rewardCol = 100, rewardExp = 50),
                Monster(name = "Босс: Илфанг", emoji = "👑", level = 7, maxHp = 500, currentHp = 500, rewardCol = 200, rewardExp = 100)
            )
            monsterList.forEach { db.monsterDao().insert(it) }
        }

        // Улучшения
        val upgrades = db.upgradeDao().getAllUpgrades().first()
        if (upgrades.isEmpty()) {
            val upgradeList = listOf(
                Upgrade(name = "Улучшение меча", description = "Увеличивает урон на +5", basePrice = 100, currentPrice = 100, effect = "weapon", effectValue = 5),
                Upgrade(name = "Автокликер", description = "Наносит 10 урона каждую секунду", basePrice = 500, currentPrice = 500, effect = "autoClicker", effectValue = 10)
            )
            upgradeList.forEach { db.upgradeDao().insert(it) }
        }

        // Достижения
        val achievements = db.achievementDao().getAllAchievements().first()
        if (achievements.isEmpty()) {
            val achievementList = listOf(
                Achievement(title = "Первый шаг", description = "Убить 1 монстра", requirementType = "kill", requirementValue = 1, rewardCol = 50, rewardExp = 10),
                Achievement(title = "Охотник", description = "Убить 10 монстров", requirementType = "kill", requirementValue = 10, rewardCol = 200, rewardExp = 50),
                Achievement(title = "Силач", description = "Достичь 10 силы", requirementType = "stat_strength", requirementValue = 10, rewardCol = 150, rewardExp = 30)
            )
            achievementList.forEach { db.achievementDao().insert(it) }
        }
    }
}
