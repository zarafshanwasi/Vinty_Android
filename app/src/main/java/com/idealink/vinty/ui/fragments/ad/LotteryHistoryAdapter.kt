package com.idealink.vinty.ui.fragments.ad

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.data.model.LotteryHistory
import com.idealink.vinty.databinding.ItemAdHistoryBinding
import com.idealink.vinty.databinding.ItemAdHistoryOddBinding
import java.text.SimpleDateFormat
import java.util.*

class LotteryHistoryAdapter : ListAdapter<LotteryHistory, RecyclerView.ViewHolder>(LotteryHistoryDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_EVEN = 0
        private const val VIEW_TYPE_ODD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) VIEW_TYPE_EVEN else VIEW_TYPE_ODD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EVEN) {
            val binding = ItemAdHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            EvenViewHolder(binding)
        } else {
            val binding = ItemAdHistoryOddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class EvenViewHolder(private val binding: ItemAdHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LotteryHistory) {
            binding.tickets.text = if (item.prizeWon != null) {
                "Prize: ${item.prizeWon}"
            } else {
                "${item.ticketsUsed ?: 0} Tickets Used"
            }
            binding.secondsAgo.text = formatTimeAgo(item.createdAt)
        }
    }

    inner class OddViewHolder(private val binding: ItemAdHistoryOddBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LotteryHistory) {
            binding.tickets.text = if (item.prizeWon != null) {
                "Prize: ${item.prizeWon}"
            } else {
                "${item.ticketsUsed ?: 0} Tickets Used"
            }
            binding.secondsAgo.text = formatTimeAgo(item.createdAt)
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
                    else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }
            } else dateString
        } catch (_: Exception) { dateString }
    }

    class LotteryHistoryDiffCallback : DiffUtil.ItemCallback<LotteryHistory>() {
        override fun areItemsTheSame(oldItem: LotteryHistory, newItem: LotteryHistory) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LotteryHistory, newItem: LotteryHistory) = oldItem == newItem
    }
}
