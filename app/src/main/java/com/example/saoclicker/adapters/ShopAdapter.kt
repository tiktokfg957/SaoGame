package com.example.saoclicker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saoclicker.data.model.Upgrade
import com.example.saoclicker.databinding.ItemShopBinding

class ShopAdapter(private val onBuyClick: (Upgrade) -> Unit) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    private var items = listOf<Upgrade>()

    fun submitList(list: List<Upgrade>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ShopViewHolder(private val binding: ItemShopBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(upgrade: Upgrade) {
            binding.tvName.text = upgrade.name
            binding.tvDescription.text = upgrade.description
            binding.tvPrice.text = "${upgrade.currentPrice} кол"
            binding.tvOwned.text = "Влад.: ${upgrade.ownedCount}"
            binding.btnBuy.setOnClickListener {
                onBuyClick(upgrade)
            }
        }
    }
}
