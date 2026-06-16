package com.idealink.vinty.ui.fragments.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.databinding.ItemMissionCardBinding
import com.idealink.vinty.data.model.Mission

class MissionAdapter(
    private val onClaimClick: (Mission) -> Unit
) : ListAdapter<Mission, MissionAdapter.MissionViewHolder>(MissionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val binding = ItemMissionCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MissionViewHolder(binding, onClaimClick)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MissionViewHolder(
        private val binding: ItemMissionCardBinding,
        private val onClaimClick: (Mission) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mission: Mission) {

            // Set mission title, description
            binding.textView34.text = mission.title
            binding.textView36.text = mission.description

            // Reward Tickets
            binding.textView32.text = "+${mission.rewardTickets}"

            // Calculate progress percentage
            val progressPercentage = if (mission.target > 0) {
                ((mission.progress.toFloat() / mission.target) * 100).toInt()
            } else 0

            binding.levelProgress.max = 100
            binding.levelProgress.progress = progressPercentage

            binding.progressTxt.text = "${mission.progress}/${mission.target}"

            // Derive UI states
            val manuallyCompleted = mission.progress >= mission.target
            val isCompleted = mission.isCompleted || manuallyCompleted
            val isClaimed = mission.isClaimed

            // Reset claim button click
            binding.claimRewardTxt.setOnClickListener(null)

            // Apply UI Logic
            when {
                // CLAIMED - show completed box with text
                isClaimed -> {
                    binding.levelProgress.visibility = View.VISIBLE
                    binding.progressTxt.visibility = View.GONE
                    binding.claimBox.visibility = View.GONE
                    binding.claimRewardTxt.visibility = View.GONE

                    binding.completedBox.visibility = View.VISIBLE
                    binding.completedTxt.visibility = View.VISIBLE
                    binding.completedTxt.bringToFront()
                }

                // COMPLETED (ready to claim) - show claim box with text
                isCompleted -> {
                    binding.levelProgress.visibility = View.VISIBLE
                    binding.progressTxt.visibility = View.GONE
                    binding.completedBox.visibility = View.GONE
                    binding.completedTxt.visibility = View.GONE

                    binding.claimBox.visibility = View.VISIBLE
                    binding.claimRewardTxt.visibility = View.VISIBLE
                    binding.claimRewardTxt.setOnClickListener { onClaimClick(mission) }
                }

                // IN PROGRESS - show progress text with progress bar
                else -> {
                    binding.levelProgress.visibility = View.VISIBLE
                    binding.progressTxt.visibility = View.VISIBLE
                    binding.claimBox.visibility = View.GONE
                    binding.claimRewardTxt.visibility = View.GONE
                    binding.completedBox.visibility = View.GONE
                    binding.completedTxt.visibility = View.GONE
                }
            }
        }
    }

    class MissionDiffCallback : DiffUtil.ItemCallback<Mission>() {
        override fun areItemsTheSame(oldItem: Mission, newItem: Mission): Boolean {
            return oldItem.missionId == newItem.missionId
        }

        override fun areContentsTheSame(oldItem: Mission, newItem: Mission): Boolean {
            return oldItem == newItem
        }
    }
}