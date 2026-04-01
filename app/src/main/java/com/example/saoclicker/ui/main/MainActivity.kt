package com.example.saoclicker.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saoclicker.R
import com.example.saoclicker.SAOClickerApplication
import com.example.saoclicker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SAOClicker", "MainActivity onCreate")
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("SAOClicker", "Layout inflated")

            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = getString(R.string.app_name)

            binding.root.setOnClickListener {
                Toast.makeText(this, "Click!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SAOClicker", "Error in onCreate", e)
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
