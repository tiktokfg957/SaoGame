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
                _weaponBonus.postValue(weapon?.effectValue ?: 0)
            }
        }
    }

    fun calculateDamage(): Int {
        val player = player.value ?: return 1
        val base = player.strength
        val agilityBonus = (player.agility * 0.1).toInt()   // +10% за ед. ловкости
        val vitalityBonus = (player.vitality * 0.05).toInt() // +5% за ед. выносливости
        val weapon = _weaponBonus.value ?: 0
        val rawDamage = base + agilityBonus + vitalityBonus + weapon
        val critChance = (player.agility / 10).coerceAtMost(50)
        val isCrit = Random().nextInt(100) < critChance
        return if (isCrit) rawDamage * 2 else rawDamage
    }

    fun performClick() {
        viewModelScope.launch {
            val monster = _currentMonster.value ?: return@launch
            val player = player.value ?: return@launch
            val damage = calculateDamage()
            val newHp = monster.currentHp - damage
            if (newHp <= 0) {
                killMonster(monster, player)
            } else {
                monster.currentHp = newHp
                repository.updateMonster(monster)
            }
        }
    }

    fun performAutoClick() {
        viewModelScope.launch {
            val player = player.value ?: return@launch
            val autoDamage = (player.attackSpeed / 10) * 10
            if (autoDamage <= 0) return@launch
            val monster = _currentMonster.value ?: return@launch
            val newHp = monster.currentHp - autoDamage
            if (newHp <= 0) {
                killMonster(monster, player)
            } else {
                monster.currentHp = newHp
                repository.updateMonster(monster)
            }
        }
    }

    private suspend fun killMonster(monster: Monster, player: Player) {
        monster.currentHp = 0
        repository.updateMonster(monster)
        player.col += monster.rewardCol
        player.experience += monster.rewardExp
        checkLevelUp(player)
        repository.updatePlayer(player)
        checkAchievements("kill", 1)
        spawnNextMonster()
    }

    private suspend fun spawnNextMonster() {
        val currentPlayer = player.value ?: return
        val allMonsters = repository.getAllMonsters().first()
        // Монстры отсортированы по уровню, выбираем следующего
        val currentMonster = _currentMonster.value
        val nextMonster = if (currentMonster == null) allMonsters.firstOrNull()
        else {
            val index = allMonsters.indexOfFirst { it.id == currentMonster.id }
            if (index >= 0 && index + 1 < allMonsters.size) allMonsters[index + 1]
            else {
                // Сброс первого монстра с увеличением сложности (повышаем HP и награду)
                val first = allMonsters.firstOrNull()
                first?.let {
                    it.maxHp = (it.maxHp * 1.2).toInt()
                    it.currentHp = it.maxHp
                    it.rewardCol = (it.rewardCol * 1.2).toInt()
                    it.rewardExp = (it.rewardExp * 1.2).toInt()
                    repository.updateMonster(it)
                }
                first
            }
        }
        _currentMonster.postValue(nextMonster)
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
