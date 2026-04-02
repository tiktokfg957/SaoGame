package com.example.saoclicker.ui.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.saoclicker.data.model.Upgrade
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: GameRepository) : ViewModel() {

    val upgrades = repository.getAllUpgrades().asLiveData()
    val player = repository.getPlayer().asLiveData()

    private val _toastMessage = MutableLiveData("")
    val toastMessage: LiveData<String> = _toastMessage

    fun buyUpgrade(upgrade: Upgrade) {
        viewModelScope.launch {
            val currentPlayer = player.value ?: return@launch
            if (currentPlayer.col >= upgrade.currentPrice) {
                currentPlayer.col -= upgrade.currentPrice
                repository.updatePlayer(currentPlayer)

                upgrade.ownedCount += 1
                upgrade.currentPrice = (upgrade.basePrice * (1 + 0.2 * upgrade.ownedCount)).toInt()
                repository.updateUpgrade(upgrade)

                // Применяем эффект (для автокликера увеличиваем скорость атаки)
                when (upgrade.effect) {
                    "autoClicker" -> {
                        currentPlayer.attackSpeed += upgrade.effectValue
                        repository.updatePlayer(currentPlayer)
                    }
                    // weapon bonus обрабатывается автоматически через Flow в MainViewModel
                }
                _toastMessage.value = "Куплено: ${upgrade.name}"
            } else {
                _toastMessage.value = "Недостаточно кол"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = ""
    }

    class ShopViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShopViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
