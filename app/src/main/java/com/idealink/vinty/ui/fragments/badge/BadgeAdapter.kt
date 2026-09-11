package com.idealink.vinty.ui.fragments.badge

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.idealink.vinty.BuildConfig
import com.idealink.vinty.databinding.ItemBadgeBinding
import com.idealink.vinty.data.model.Badge

class BadgeAdapter(
    private val onBadgeClick: (Badge) -> Unit
) : ListAdapter<Badge, BadgeAdapter.BadgeViewHolder>(BadgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BadgeViewHolder(
        private val binding: ItemBadgeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: Badge) {
            binding.apply {

                badgeLoader.visibility = View.VISIBLE
                badgeIcon.setImageDrawable(null)

                val baseUrl = BuildConfig.BASE_IMAGE_URL
                val rawIconUrl = badge.iconUrl.replace(".svg", ".png", ignoreCase = true)
                val iconUrl = if (rawIconUrl.startsWith("http")) {
                    rawIconUrl
                } else {
                    "$baseUrl$rawIconUrl"
                }

                Glide.with(root.context)
                    .load(iconUrl)
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.badgeLoader.visibility = View.GONE
                            binding.badgeIcon.setImageDrawable(null)
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.badgeLoader.visibility = View.GONE
                            return false
                        }
                    })


                    .into(badgeIcon)

                if (!badge.isUnlocked) {
                    lockedOverlay.visibility = View.VISIBLE
                    badgeIcon.alpha = 0.3f
                } else {
                    lockedOverlay.visibility = View.GONE
                    badgeIcon.alpha = 1.0f
                }

                // Click listener (UNCHANGED)
                root.setOnClickListener {
                    onBadgeClick(badge)
                }
            }
        }
    }

    class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Badge, newItem: Badge): Boolean {
            return oldItem == newItem
        }
    }
}
