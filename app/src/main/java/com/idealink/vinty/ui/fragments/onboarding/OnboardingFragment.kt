package com.idealink.vinty.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var items: List<OnboardingItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        items = listOf(
            OnboardingItem(
                topImage = R.drawable.watch_ad_logo,
                iconImage = R.drawable.watch_icon,
                title = "Watch Ads & Discover",
                glowBg = R.drawable.bg_glow_30,
                description = "Spend a few spare moments watching sponsored videos. It’s the easiest way to start your journey with Vinty"
            ),
            OnboardingItem(
                topImage = R.drawable.earn_ticket_logo,
                iconImage = R.drawable.ticket_ic,
                title = "Earn Tickets",
                glowBg = R.drawable.bg_glow_ticket,
                description = "Every video you complete adds,\n" +
                        "tickets directly to your wallet. The more you watch, the faster your balance grows."
            ),
            OnboardingItem(
                topImage = R.drawable.redeem_gift_card_logo,
                iconImage = R.drawable.ic_gift,
                title = "Redeem Gift Cards",
                glowBg = R.drawable.bg_glow_redeem,
                description = "Exchange your Tickets for gift cards from your favorite brands. Shopping at top retailers has never been this free!"
            )
        )

        binding.viewPager.adapter = OnboardingAdapter(items)
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        // 🔥 Change button text on swipe
        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.btnnext.text =
                        if (position == items.lastIndex) "Get Started" else "Next"
                }
            }
        )
    }

    private fun setupButtons() {

        // Skip → Login (always visible)
        binding.skipbtn.setOnClickListener {
            goToLogin()
        }

        // Next / Get Started
        binding.btnnext.setOnClickListener {
            val currentPage = binding.viewPager.currentItem
            if (currentPage == items.lastIndex) {
                goToLogin()
            } else {
                binding.viewPager.currentItem = currentPage + 1
            }
        }
    }

    private fun goToLogin() {
        val dataManager = DataManager.getInstance()
        dataManager.setOnboardingCompleted()

        findNavController().navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build()
        )
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
