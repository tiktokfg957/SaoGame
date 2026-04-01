package com.example.saoclicker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.saoclicker.data.repository.GameRepository

class AchievementsViewModel(private val repository: GameRepository) : ViewModel() {

    val achievements = repository.getAllAchievements().asLiveData()

    class AchievementsViewModelFactory(private val repository: GameRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AchievementsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
