package com.idealink.vinty.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.idealink.vinty.R
import com.idealink.vinty.data.model.User
import com.idealink.vinty.databinding.ViewProfileHeaderBinding

class ProfileHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewProfileHeaderBinding =
        ViewProfileHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    // =========================
    // USER DATA
    // =========================

    fun updateProfile(user: User?) {
        user?.let {
            binding.usernameText.text = it.username
            val levelStr = "Lvl. ${it.level}"
            binding.levelText.text = levelStr
            binding.ticketText.text = it.tickets.toString()

            it.avatarUrl?.let { url ->
                Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profileImage)
            } ?: run {
                binding.profileImage.setImageResource(R.drawable.profile_icon)
            }
        }
    }

    // =========================
    // BADGE STATS (NEW API)
    // =========================

    fun updateBadgeStats(unlocked: Int, locked: Int) {
        binding.badgeText.text = "$unlocked/$locked"
    }

    // =========================
    // CLICK LISTENER
    // =========================

    fun setOnProfileClickListener(listener: OnClickListener) {
        binding.profileImage.setOnClickListener(listener)
        binding.usernameText.setOnClickListener(listener)
    }
}
