package com.example.saoclicker.ui.main

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.saoclicker.R
import com.example.saoclicker.SAOClickerApplication
import com.example.saoclicker.databinding.ActivityMainBinding
import com.example.saoclicker.ui.achievements.AchievementsActivity
import com.example.saoclicker.ui.shop.ShopActivity
import com.example.saoclicker.ui.stats.StatsActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((application as SAOClickerApplication).repository)
    }
    private var autoClickJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        viewModel.player.observe(this) { player ->
            binding.tvLevel.text = "${player.level}"
            binding.tvExp.text = "${player.experience}"
            binding.tvCol.text = "${player.col}"
            binding.tvSkillPoints.text = "${player.skillPoints}"
        }

        viewModel.currentMonster.observe(this) { monster ->
            monster?.let {
                binding.tvMonsterName.text = "${it.emoji} ${it.name} (Ур. ${it.level})"
                binding.progressBarHp.max = it.maxHp
                binding.progressBarHp.progress = it.currentHp
                binding.tvHp.text = "${it.currentHp}/${it.maxHp}"
            }
        }

        viewModel.weaponBonus.observe(this) {
            binding.tvDamage.text = getString(R.string.damage, viewModel.calculateDamage())
        }

        binding.root.setOnClickListener { event ->
            viewModel.performClick()
            showClickEffect(event.x, event.y)
        }

        binding.btnShop.setOnClickListener {
            startActivity(android.content.Intent(this, ShopActivity::class.java))
        }
        binding.btnStats.setOnClickListener {
            startActivity(android.content.Intent(this, StatsActivity::class.java))
        }
        binding.btnAchievements.setOnClickListener {
            startActivity(android.content.Intent(this, AchievementsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        startAutoClicker()
    }

    override fun onPause() {
        super.onPause()
        stopAutoClicker()
    }

    private fun startAutoClicker() {
        autoClickJob?.cancel()
        autoClickJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000L)
                viewModel.performAutoClick()
            }
        }
    }

    private fun stopAutoClicker() {
        autoClickJob?.cancel()
        autoClickJob = null
    }

    private fun showClickEffect(x: Float, y: Float) {
        val damageText = TextView(this).apply {
            text = "+${viewModel.calculateDamage()}"
            setTextColor(Color.YELLOW)
            textSize = 20f
            setPadding(16, 8, 16, 8)
            setBackgroundResource(R.drawable.bg_click_effect)
        }
        (window.decorView as ViewGroup).addView(damageText, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            this.x = x - 50
            this.y = y - 100
        })

        damageText.animate()
            .translationY(-100f)
            .alpha(0f)
            .setDuration(800)
            .withEndAction {
                (damageText.parent as? ViewGroup)?.removeView(damageText)
            }
            .start()
    }
}
