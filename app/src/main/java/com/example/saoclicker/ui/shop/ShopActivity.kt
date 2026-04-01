package com.example.saoclicker.ui.shop

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saoclicker.SAOClickerApplication
import com.example.saoclicker.adapters.ShopAdapter
import com.example.saoclicker.databinding.ActivityShopBinding
import kotlinx.coroutines.launch

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private val viewModel: ShopViewModel by viewModels {
        ShopViewModel.ShopViewModelFactory((application as SAOClickerApplication).repository)
    }
    private lateinit var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.shop)

        adapter = ShopAdapter { upgrade ->
            viewModel.buyUpgrade(upgrade)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.upgrades.observe(this) { upgrades ->
            adapter.submitList(upgrades)
        }

        viewModel.player.observe(this) { player ->
            binding.tvCol.text = player.col.toString()
        }

        // показываем тосты об успехе/ошибке
        viewModel.toastMessage.observe(this) { message ->
            if (message.isNotBlank()) {
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
