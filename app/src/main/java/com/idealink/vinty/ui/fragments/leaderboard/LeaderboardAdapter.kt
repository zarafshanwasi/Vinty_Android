package com.idealink.vinty.ui.fragments.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.idealink.vinty.R
import com.idealink.vinty.data.model.LeaderboardUser

class LeaderboardAdapter :
    ListAdapter<LeaderboardUser, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_ONE = 1
        private const val TYPE_TWO = 2
        private const val TYPE_THREE = 3
        private const val TYPE_FOUR = 4
        private const val TYPE_FIVE = 5

        private val DIFF = object : DiffUtil.ItemCallback<LeaderboardUser>() {
            override fun areItemsTheSame(oldItem: LeaderboardUser, newItem: LeaderboardUser): Boolean {
                return oldItem.rank == newItem.rank // or unique id if you have one
            }

            override fun areContentsTheSame(oldItem: LeaderboardUser, newItem: LeaderboardUser): Boolean {
                return oldItem == newItem
            }
        }
    }

    // ---------------------------------------------------

    override fun getItemViewType(position: Int): Int {
        val rank = getItem(position).rank
        return when (rank) {
            1 -> TYPE_ONE
            2 -> TYPE_TWO
            3 -> TYPE_THREE
            4 -> TYPE_FOUR
            5 -> TYPE_FIVE
            else -> if (rank % 2 == 0) TYPE_FOUR else TYPE_FIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            TYPE_ONE -> R.layout.leaderboard_card_one
            TYPE_TWO -> R.layout.leaderboard_card_two
            TYPE_THREE -> R.layout.leaderboard_card_three
            TYPE_FOUR -> R.layout.leaderboard_four_card_xml
            TYPE_FIVE -> R.layout.leaderboard_five
            else -> R.layout.leaderboard_five
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        (holder as LeaderboardViewHolder).bind(user)
    }

    // ---------------------------------------------------

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val username: TextView = itemView.findViewById(R.id.username_txt_view)
        private val tickets: TextView = itemView.findViewById(R.id.tickets_view)
        private val userNumber: TextView? = itemView.findViewById(R.id.user_number)
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImageView)

        fun bind(user: LeaderboardUser) {
            username.text = user.username
            tickets.text = user.stats.totalScore.toString()
            userNumber?.text = String.format("%02d", user.rank)

            Glide.with(itemView.context)
                .load(user.avatarUrl ?: R.drawable.profile_icon)
                .into(profileImage)
        }
    }
}
