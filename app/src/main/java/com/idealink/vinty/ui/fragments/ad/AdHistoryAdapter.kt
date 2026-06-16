package com.idealink.vinty.ui.fragments.ad

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.databinding.ItemAdHistoryBinding
import com.idealink.vinty.databinding.ItemAdHistoryOddBinding
import java.text.SimpleDateFormat
import java.util.*

data class AdHistory(
    val id: String,
    val userId: String,
    val watchedAt: String,
    val value: Int,
    val ticketAwarded: Boolean
)

class AdHistoryAdapter : ListAdapter<AdHistory, RecyclerView.ViewHolder>(AdHistoryDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_EVEN = 0
        private const val VIEW_TYPE_ODD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) VIEW_TYPE_EVEN else VIEW_TYPE_ODD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EVEN) {
            val binding = ItemAdHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            EvenViewHolder(binding)
        } else {
            val binding = ItemAdHistoryOddBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            OddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EvenViewHolder -> holder.bind(item)
            is OddViewHolder -> holder.bind(item)
        }
    }

    // Even position ViewHolder (no background card)
    inner class EvenViewHolder(
        private val binding: ItemAdHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adHistory: AdHistory) {
            binding.tickets.text = if (adHistory.ticketAwarded) {
                "+${adHistory.value} Tickets"
            } else {
                "No ticket"
            }
            binding.secondsAgo.text = formatTimeAgo(adHistory.watchedAt)
        }
    }

    // Odd position ViewHolder (with background card)
    inner class OddViewHolder(
        private val binding: ItemAdHistoryOddBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adHistory: AdHistory) {
            binding.tickets.text = if (adHistory.ticketAwarded) {
                "+${adHistory.value} Tickets"
            } else {
                "No ticket"
            }
            binding.secondsAgo.text = formatTimeAgo(adHistory.watchedAt)
        }
    }

    private fun formatTimeAgo(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateString)

            if (date != null) {
                val now = System.currentTimeMillis()
                val diff = now - date.time

                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                when {
                    seconds < 60 -> "$seconds seconds ago"
                    minutes < 60 -> "$minutes minutes ago"
                    hours < 24 -> "$hours hours ago"
                    days < 7 -> "$days days ago"
                    else -> {
                        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        displayFormat.format(date)
                    }
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    class AdHistoryDiffCallback : DiffUtil.ItemCallback<AdHistory>() {
        override fun areItemsTheSame(oldItem: AdHistory, newItem: AdHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdHistory, newItem: AdHistory): Boolean {
            return oldItem == newItem
        }
    }
}