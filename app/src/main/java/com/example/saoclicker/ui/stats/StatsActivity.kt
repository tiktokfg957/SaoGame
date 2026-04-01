package com.example.saoclicker.ui.stats

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saoclicker.SAOClickerApplication
import com.example.saoclicker.databinding.ActivityStatsBinding

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val viewModel: StatsViewModel by viewModels {
        StatsViewModel.StatsViewModelFactory((application as SAOClickerApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.stats)

        viewModel.player.observe(this) { player ->
            binding.tvStrength.text = player.strength.toString()
            binding.tvAgility.text = player.agility.toString()
            binding.tvVitality.text = player.vitality.toString()
            binding.tvAttackSpeed.text = player.attackSpeed.toString()
            binding.tvSkillPoints.text = player.skillPoints.toString()
        }

        binding.btnStrengthPlus.setOnClickListener {
            viewModel.increaseStat("strength")
        }
        binding.btnAgilityPlus.setOnClickListener {
            viewModel.increaseStat("agility")
        }
        binding.btnVitalityPlus.setOnClickListener {
            viewModel.increaseStat("vitality")
        }
        binding.btnAttackSpeedPlus.setOnClickListener {
            viewModel.increaseStat("attackSpeed")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
