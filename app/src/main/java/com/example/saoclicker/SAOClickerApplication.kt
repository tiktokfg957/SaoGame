package com.example.saoclicker

import android.app.Application
import com.example.saoclicker.data.database.AppDatabase
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.runBlocking

class SAOClickerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Блокируемся до завершения инициализации, чтобы данные были готовы к моменту открытия активити
        runBlocking {
            repository.initDatabase()
        }
    }
}
