package com.idealink.vinty.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.R
import android.view.View
import com.idealink.vinty.data.model.ShopGiftCard
import com.idealink.vinty.databinding.GiftstoreItemBinding

class ShopGiftCardAdapter(
    private val onClaimClick: (ShopGiftCard) -> Unit
) : ListAdapter<ShopGiftCard, ShopGiftCardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GiftstoreItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: GiftstoreItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(giftCard: ShopGiftCard) {

            // Description
            binding.descriptionGiftcard.text =
                if (giftCard.description.isNullOrBlank()) {
                    "$${giftCard.value} Gift Card"
                } else {
                    giftCard.description
                }

            // Value text
            binding.valueTxtview.text =
                "Use this $${giftCard.value} card across\n multiple platforms"

            binding.dollarImage.setImageResource(
                getDollarImage(giftCard.value)
            )

            // Claim button logic (UNCHANGED)
            if (giftCard.availableCount > 0 && giftCard.isAvailable) {
                binding.textView39.text = "Claim Gift for"
                binding.amountTxtView.text = giftCard.minTickets.toString()
                binding.btnContinue.isEnabled = true
                setDisabledAlpha(
                    binding.btnContinue,
                    binding.textView39,
                    binding.amountTxtView,
                    binding.imageView28
                )
                binding.btnContinue.setOnClickListener {
                    onClaimClick(giftCard)
                }
            } else {
                binding.textView39.text = "Need"
                binding.amountTxtView.text = "${giftCard.minTickets} to claim"
                binding.btnContinue.isEnabled = false
                setDisabledAlpha(
                    binding.btnContinue,
                    binding.textView39,
                    binding.amountTxtView,
                    binding.imageView28
                )
            }
        }
    }
    private fun getDollarImage(value: Int): Int {
        return when (value) {
            5 -> R.drawable.dollar_icon5
            10 -> R.drawable.dollar_icon10
            15 -> R.drawable.dollar_icon15
            20 -> R.drawable.dollar_icon20
            50 -> R.drawable.dollar_icon50
            100 -> R.drawable.dollar_icon100
            else -> R.drawable.dollar_icon10
        }
    }
    private fun setDisabledAlpha(vararg views: View) {
        views.forEach { it.alpha = 0.5f }
    }

    private fun setEnabledAlpha(vararg views: View) {
        views.forEach { it.alpha = 1f }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShopGiftCard>() {
        override fun areItemsTheSame(oldItem: ShopGiftCard, newItem: ShopGiftCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShopGiftCard, newItem: ShopGiftCard): Boolean {
            return oldItem == newItem
        }
    }
}
