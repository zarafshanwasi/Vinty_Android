package com.idealink.vinty.ui.fragments.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idealink.vinty.databinding.OnboardingPageBinding

class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: OnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        with(holder.binding) {
            topImage.setImageResource(item.topImage)
            imageicon.setImageResource(item.iconImage)

            // 🔥 APPLY PAGE-SPECIFIC GLOW
            imageicon.setBackgroundResource(item.glowBg)

            title.text = item.title
            description.text = item.description
        }
    }


    override fun getItemCount(): Int = items.size
}
