package com.example.saoclicker

import android.app.Application
import android.util.Log
import com.example.saoclicker.data.database.AppDatabase
import com.example.saoclicker.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SAOClickerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Инициализация в фоне, чтобы не блокировать UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.initDatabase()
                Log.d("SAOClicker", "Database initialized successfully")
            } catch (e: Exception) {
                Log.e("SAOClicker", "Database init failed", e)
            }
        }
    }
}
