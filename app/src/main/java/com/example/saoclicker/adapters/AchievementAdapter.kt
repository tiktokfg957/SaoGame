package com.example.saoclicker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saoclicker.data.model.Achievement
import com.example.saoclicker.databinding.ItemAchievementBinding

class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private var items = listOf<Achievement>()

    fun submitList(list: List<Achievement>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class AchievementViewHolder(private val binding: ItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            binding.tvTitle.text = achievement.title
            binding.tvDescription.text = achievement.description
            if (achievement.isCompleted) {
                binding.tvProgress.text = getString(com.example.saoclicker.R.string.completed)
                binding.tvProgress.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                binding.tvProgress.text = getString(
                    com.example.saoclicker.R.string.achievement_progress,
                    achievement.currentProgress,
                    achievement.requirementValue
                )
                binding.tvProgress.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            }
            binding.tvRewardCol.text = getString(
                com.example.saoclicker.R.string.reward_col,
                achievement.rewardCol
            )
            binding.tvRewardExp.text = getString(
                com.example.saoclicker.R.string.reward_exp,
                achievement.rewardExp
            )
        }

        private fun getString(id: Int): String {
            return binding.root.context.getString(id)
        }
    }
}
