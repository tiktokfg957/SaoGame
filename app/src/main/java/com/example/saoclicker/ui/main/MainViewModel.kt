package com.example.saoclicker.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.saoclicker.data.model.Monster
import com.example.saoclicker.data.model.Player
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Random

class MainViewModel(private val repository: GameRepository) : ViewModel() {

    val player: LiveData<Player> = repository.getPlayer().asLiveData()

    private val _currentMonster = MutableLiveData<Monster?>()
    val currentMonster: LiveData<Monster?> = _currentMonster

    private val _weaponBonus = MutableLiveData(0)
    val weaponBonus: LiveData<Int> = _weaponBonus

    init {
        viewModelScope.launch {
            repository.getAllMonsters().collect { monsters ->
                val active = monsters.firstOrNull { it.currentHp > 0 } ?: monsters.firstOrNull()
                _currentMonster.postValue(active)
            }
        }

        viewModelScope.launch {
            repository.getAllUpgrades().collect { upgrades ->
                val weapon = upgrades.find { it.effect == "weapon" }
                // Бонус = ownedCount * effectValue (каждый уровень даёт +effectValue урона)
                val bonus = (weapon?.ownedCount ?: 0) * (weapon?.effectValue ?: 0)
                _weaponBonus.postValue(bonus)
            }
        }
    }

    fun calculateDamage(): Int {
        val player = player.value ?: return 1
        val bonus = _weaponBonus.value ?: 0
        val critChance = (player.agility / 10).coerceAtMost(50)
        val isCrit = Random().nextInt(100) < critChance
        val baseDamage = player.strength + bonus
        return if (isCrit) baseDamage * 2 else baseDamage
    }

    fun performClick() {
        viewModelScope.launch {
            val monster = _currentMonster.value ?: return@launch
            val player = player.value ?: return@launch
            val damage = calculateDamage()
            val newHp = monster.currentHp - damage
            if (newHp <= 0) {
                monster.currentHp = 0
                repository.updateMonster(monster)
                player.col += monster.rewardCol
                player.experience += monster.rewardExp
                checkLevelUp(player)
                repository.updatePlayer(player)
                checkAchievements("kill", 1)
            } else {
                monster.currentHp = newHp
                repository.updateMonster(monster)
            }
        }
    }

    fun performAutoClick() {
        viewModelScope.launch {
            val player = player.value ?: return@launch
            // Автоурон от скорости атаки (каждые 10 очков = 10 урона)
            val autoDamage = (player.attackSpeed / 10) * 10
            if (autoDamage > 0) {
                val monster = _currentMonster.value ?: return@launch
                val newHp = monster.currentHp - autoDamage
                if (newHp <= 0) {
                    monster.currentHp = 0
                    repository.updateMonster(monster)
                    player.col += monster.rewardCol
                    player.experience += monster.rewardExp
                    checkLevelUp(player)
                    repository.updatePlayer(player)
                    checkAchievements("kill", 1)
                } else {
                    monster.currentHp = newHp
                    repository.updateMonster(monster)
                }
            }
        }
    }

    private suspend fun checkLevelUp(player: Player) {
        var level = player.level
        var exp = player.experience
        while (exp >= 100 * level) {
            exp -= 100 * level
            level++
            player.skillPoints += 1
        }
        if (level != player.level) {
            player.level = level
            player.experience = exp
            repository.updatePlayer(player)
        }
    }

    private suspend fun checkAchievements(type: String, value: Int) {
        val achievements = repository.getAllAchievements().first()
        achievements.filter { !it.isCompleted && it.requirementType == type }.forEach { ach ->
            val newProgress = ach.currentProgress + value
            if (newProgress >= ach.requirementValue) {
                ach.isCompleted = true
                ach.currentProgress = ach.requirementValue
                val player = player.value ?: return@forEach
                player.col += ach.rewardCol
                player.experience += ach.rewardExp
                repository.updatePlayer(player)
            } else {
                ach.currentProgress = newProgress
            }
            repository.updateAchievement(ach)
        }
    }

    class MainViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
