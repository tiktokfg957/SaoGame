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
import kotlinx.coroutines.launch
import java.util.Random

class MainViewModel(private val repository: GameRepository) : ViewModel() {

    val player: LiveData<Player> = repository.getPlayer().asLiveData()
    val allMonsters = repository.getAllMonsters().asLiveData()

    private val _currentMonster = MutableLiveData<Monster?>()
    val currentMonster: LiveData<Monster?> = _currentMonster

    private val _weaponBonus = MutableLiveData(0)
    val weaponBonus: LiveData<Int> = _weaponBonus

    init {
        viewModelScope.launch {
            val monsters = repository.getAllMonsters().asLiveData()
            // обновление текущего монстра (берём первого с hp>0)
            repository.getAllMonsters().asLiveData().observeForever { monsterList ->
                val active = monsterList.firstOrNull { it.currentHp > 0 } ?: monsterList.firstOrNull()
                _currentMonster.postValue(active)
            }
            // подписываемся на апгрейды для оружия
            repository.getAllUpgrades().asLiveData().observeForever { upgrades ->
                val weapon = upgrades.find { it.effect == "weapon" }
                _weaponBonus.postValue(weapon?.effectValue ?: 0)
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
                // убийство монстра
                monster.currentHp = 0
                repository.updateMonster(monster)
                // награда
                val newCol = player.col + monster.rewardCol
                val newExp = player.experience + monster.rewardExp
                // обновляем игрока
                player.col = newCol
                player.experience = newExp
                // проверка уровня
                checkLevelUp(player)
                repository.updatePlayer(player)

                // переключение на следующего монстра
                val monsters = repository.getAllMonsters().asLiveData().value ?: emptyList()
                val next = monsters.firstOrNull { it.currentHp > 0 } ?: monsters.firstOrNull()
                if (next != null) {
                    // сброс HP, если это первый монстр (новый цикл)
                    if (next.currentHp <= 0) {
                        next.currentHp = next.maxHp
                        repository.updateMonster(next)
                    }
                    _currentMonster.postValue(next)
                }
                // проверка достижений
                checkAchievements("kill", 1)
            } else {
                monster.currentHp = newHp
                repository.updateMonster(monster)
                _currentMonster.postValue(monster)
            }
        }
    }

    fun performAutoClick() {
        viewModelScope.launch {
            val player = player.value ?: return@launch
            val autoDamage = (player.attackSpeed / 10) * 10 // каждые 10 очков дают 10 урона
            if (autoDamage > 0) {
                val monster = _currentMonster.value ?: return@launch
                val newHp = monster.currentHp - autoDamage
                if (newHp <= 0) {
                    // аналогично kill, но без дополнительных вызовов
                    monster.currentHp = 0
                    repository.updateMonster(monster)
                    player.col += monster.rewardCol
                    player.experience += monster.rewardExp
                    checkLevelUp(player)
                    repository.updatePlayer(player)

                    val monsters = repository.getAllMonsters().asLiveData().value ?: emptyList()
                    val next = monsters.firstOrNull { it.currentHp > 0 } ?: monsters.firstOrNull()
                    if (next != null) {
                        if (next.currentHp <= 0) {
                            next.currentHp = next.maxHp
                            repository.updateMonster(next)
                        }
                        _currentMonster.postValue(next)
                    }
                    checkAchievements("kill", 1)
                } else {
                    monster.currentHp = newHp
                    repository.updateMonster(monster)
                    _currentMonster.postValue(monster)
                }
            }
        }
    }

    private fun checkLevelUp(player: Player) {
        var level = player.level
        var exp = player.experience
        while (exp >= 100 * level) { // формула опыта для уровня
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

    private fun checkAchievements(type: String, value: Int) {
        viewModelScope.launch {
            val achievements = repository.getAllAchievements().asLiveData().value ?: return@launch
            achievements.filter { !it.isCompleted && it.requirementType == type }.forEach { ach ->
                val newProgress = ach.currentProgress + value
                if (newProgress >= ach.requirementValue) {
                    ach.isCompleted = true
                    ach.currentProgress = ach.requirementValue
                    // даём награду
                    val player = player.value ?: return@launch
                    player.col += ach.rewardCol
                    player.experience += ach.rewardExp
                    repository.updatePlayer(player)
                } else {
                    ach.currentProgress = newProgress
                }
                repository.updateAchievement(ach)
            }
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
