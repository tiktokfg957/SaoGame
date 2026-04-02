package com.example.saoclicker.ui.main

import android.animation.ValueAnimator
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((application as SAOClickerApplication).repository)
    }

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
            } ?: run {
                binding.tvMonsterName.text = "Загрузка монстра..."
            }
        }

        viewModel.weaponBonus.observe(this) {
            binding.tvDamage.text = getString(R.string.damage, viewModel.calculateDamage())
        }

        // Атака по нажатию на круглую кнопку
        binding.attackButton.setOnClickListener {
            viewModel.performClick()
            showClickEffect(binding.attackButton.x + binding.attackButton.width/2,
                binding.attackButton.y + binding.attackButton.height/2)
        }

        // Также можно оставить атаку по всему экрану (опционально)
        // binding.root.setOnClickListener { ... } – закомментируем, чтобы не мешать кнопкам

        binding.btnShop.setOnClickListener {
            startActivity(android.content.Intent(this, ShopActivity::class.java))
        }
        binding.btnStats.setOnClickListener {
            startActivity(android.content.Intent(this, StatsActivity::class.java))
        }
        binding.btnAchievements.setOnClickListener {
            startActivity(android.content.Intent(this, AchievementsActivity::class.java))
        }

        lifecycleScope.launch {
            while (true) {
                delay(1000)
                viewModel.performAutoClick()
            }
        }
    }

    private fun showClickEffect(x: Float, y: Float) {
        val damageText = TextView(this)
        damageText.text = "+${viewModel.calculateDamage()}"
        damageText.setTextColor(resources.getColor(R.color.white, null))
        damageText.textSize = 24f
        damageText.x = x
        damageText.y = y
        (window.decorView as ViewGroup).addView(damageText)

        val animator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 800
            addUpdateListener {
                damageText.alpha = it.animatedValue as Float
                damageText.translationY -= 50 * it.animatedFraction
            }
        }
        animator.start()
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                (damageText.parent as? ViewGroup)?.removeView(damageText)
            }
        })
    }
}
