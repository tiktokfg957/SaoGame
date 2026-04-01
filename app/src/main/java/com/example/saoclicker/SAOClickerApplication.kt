package com.example.saoclicker

import android.app.Application
import android.util.Log
import com.example.saoclicker.data.database.AppDatabase
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.runBlocking

class SAOClickerApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: GameRepository

    override fun onCreate() {
        super.onCreate()
        Log.d("SAOClicker", "Application onCreate")
        try {
            database = AppDatabase.getDatabase(this)
            repository = GameRepository(database)
            // Инициализируем базу данных в фоне
            Thread {
                runBlocking {
                    try {
                        repository.initDatabase()
                        Log.d("SAOClicker", "Database initialized successfully")
                    } catch (e: Exception) {
                        Log.e("SAOClicker", "Error initializing database", e)
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e("SAOClicker", "Application creation failed", e)
        }
    }
}
