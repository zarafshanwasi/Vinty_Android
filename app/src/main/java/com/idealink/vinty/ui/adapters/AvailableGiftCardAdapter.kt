package com.idealink.vinty.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.data.model.AvailableGiftCard
import com.idealink.vinty.data.model.Redemption
import com.idealink.vinty.databinding.GiftstoreItem2Binding

class AvailableGiftCardAdapter(
    private val isRedeemed: Boolean = false,
    private val onRedeemClick: (AvailableGiftCard) -> Unit = {}
) : ListAdapter<Any, AvailableGiftCardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GiftstoreItem2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AvailableGiftCard -> holder.bindAvailable(item)
            is Redemption -> holder.bindRedeemed(item)
        }
    }

    inner class ViewHolder(
        private val binding: GiftstoreItem2Binding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindAvailable(giftCard: AvailableGiftCard) {
            // Set value
            binding.value.text = "$${giftCard.giftPackage.value}"
            
            // Set PIN code
            binding.pinCode.text = giftCard.giftCard.pin ?: "N/A"
            binding.pinCode.setOnClickListener {
                copyToClipboard(itemView.context, giftCard.giftCard.pin ?: "")
            }
            
            // Set card code (shortened)
            binding.cardCode.text = shortenCode(giftCard.giftCard.code)
            binding.cardCode.setOnClickListener {
                copyToClipboard(itemView.context, giftCard.giftCard.code)
            }
            
            // Set redeem button
            binding.redeemBtn.text = "Redeem Now"
            binding.redeemBtn.isEnabled = true
            binding.redeemBtn.alpha = 1.0f
            binding.redeemBtn.setOnClickListener {
                onRedeemClick(giftCard)
            }
        }

        fun bindRedeemed(redemption: Redemption) {
            // Set value
            binding.value.text = "$${redemption.giftPackage.value}"
            
            // Set PIN code
            binding.pinCode.text = redemption.giftCard.pin ?: "N/A"
            binding.pinCode.setOnClickListener {
                copyToClipboard(itemView.context, redemption.giftCard.pin ?: "")
            }
            
            // Set card code (shortened)
            binding.cardCode.text = shortenCode(redemption.giftCard.code)
            binding.cardCode.setOnClickListener {
                copyToClipboard(itemView.context, redemption.giftCard.code)
            }
            
            // Set redeemed button
            binding.redeemBtn.text = "Already Redeemed"
            binding.redeemBtn.isEnabled = false
            binding.redeemBtn.alpha = 0.5f
        }

        private fun shortenCode(code: String, prefixCount: Int = 4, suffixCount: Int = 3): String {
            return if (code.length > prefixCount + suffixCount) {
                "${code.take(prefixCount)}...${code.takeLast(suffixCount)}"
            } else {
                code
            }
        }

        private fun copyToClipboard(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Gift Card Code", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is AvailableGiftCard && newItem is AvailableGiftCard -> 
                    oldItem.id == newItem.id
                oldItem is Redemption && newItem is Redemption -> 
                    oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
