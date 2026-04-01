package com.example.saoclicker.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.launch

class StatsViewModel(private val repository: GameRepository) : ViewModel() {

    val player = repository.getPlayer().asLiveData()
    private val _toastMessage = MutableLiveData("")
    val toastMessage: LiveData<String> = _toastMessage

    fun increaseStat(stat: String) {
        viewModelScope.launch {
            val currentPlayer = player.value ?: return@launch
            if (currentPlayer.skillPoints > 0) {
                when (stat) {
                    "strength" -> currentPlayer.strength += 1
                    "agility" -> currentPlayer.agility += 1
                    "vitality" -> currentPlayer.vitality += 1
                    "attackSpeed" -> currentPlayer.attackSpeed += 1
                }
                currentPlayer.skillPoints -= 1
                repository.updatePlayer(currentPlayer)
                // также обновляем достижения (stat_strength и т.п.)
                checkStatAchievements(stat, currentPlayer)
            } else {
                _toastMessage.value = "Недостаточно очков навыков"
            }
        }
    }

    private fun checkStatAchievements(stat: String, player: com.example.saoclicker.data.model.Player) {
        viewModelScope.launch {
            val achievements = repository.getAllAchievements().asLiveData().value ?: return@launch
            val value = when (stat) {
                "strength" -> player.strength
                "agility" -> player.agility
                "vitality" -> player.vitality
                "attackSpeed" -> player.attackSpeed
                else -> 0
            }
            achievements.filter { !it.isCompleted && it.requirementType == "stat_$stat" && it.requirementValue <= value }.forEach { ach ->
                ach.isCompleted = true
                ach.currentProgress = ach.requirementValue
                // награда
                player.col += ach.rewardCol
                player.experience += ach.rewardExp
                repository.updatePlayer(player)
                repository.updateAchievement(ach)
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = ""
    }

    class StatsViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
