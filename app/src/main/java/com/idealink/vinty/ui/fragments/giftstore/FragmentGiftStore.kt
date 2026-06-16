package com.idealink.vinty.ui.fragments.giftstore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.databinding.CustomTabBinding
import com.idealink.vinty.databinding.FragmentFragmentGiftStoreBinding
import com.idealink.vinty.ui.viewmodel.FragmentGiftStoreViewModel
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class FragmentGiftStore : Fragment() {

    private var _binding: FragmentFragmentGiftStoreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentGiftStoreViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }
    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentGiftStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupClicks()
        observeViewModels()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewPager() {
        val pagerAdapter = GiftStorePagerAdapter(this)
        binding.giftStoreViewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.giftStoreViewPager) { tab, position ->
            val customBinding = CustomTabBinding.inflate(layoutInflater)
            customBinding.tabText.text = when (position) {
                0 -> "Shop"
                1 -> "Available"
                2 -> "Redeemed"
                else -> ""
            }
            tab.customView = customBinding.root
        }.attach()

        binding.tabLayout.post {
            styleSelectedTab(binding.tabLayout.getTabAt(0))
            moveIndicator(0)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                styleSelectedTab(tab)
                moveIndicator(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                styleUnselectedTab(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun moveIndicator(position: Int) {
        val tab = binding.tabLayout.getTabAt(position) ?: return
        val tabView = tab.view

        binding.tabIndicatorLine.animate()
            .x(tabView.x)
            .setDuration(200)
            .start()

        binding.tabIndicatorLine.layoutParams.width = tabView.width
        binding.tabIndicatorLine.requestLayout()
    }

    private fun styleSelectedTab(tab: TabLayout.Tab?) {
        tab?.customView?.let { view ->
            val customBinding = CustomTabBinding.bind(view)
            customBinding.tabText.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
            customBinding.tabText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun styleUnselectedTab(tab: TabLayout.Tab?) {
        tab?.customView?.let { view ->
            val customBinding = CustomTabBinding.bind(view)
            customBinding.tabText.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.poppins_light)
            customBinding.tabText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.txt_color)
            )
        }
    }

    // ----------------------------------------------------
    // Setup
    // ----------------------------------------------------

    private fun setupClicks() {
        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_fragmentGiftStore_to_profileFragment)
        }
    }

    // ----------------------------------------------------
    // Observers (ALL collectors here)
    // ----------------------------------------------------

    private fun observeViewModels() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // -----------------------------
                // User (StateFlow replay)
                // -----------------------------
                launch {
                    userSharedViewModel.user.collect { user ->
                        user?.let(binding.profileHeader::updateProfile)
                    }
                }

                // -----------------------------
                // Badge stats
                // -----------------------------
                launch {
                    userSharedViewModel.badgeStats.collect { response ->
                        response?.data?.overall?.let {
                            binding.profileHeader.updateBadgeStats(
                                unlocked = it.unlocked,
                                locked = it.locked
                            )
                        }
                    }
                }

                // -----------------------------
                // Events (navigation/toasts)
                // -----------------------------
                launch {
                    viewModel.events.collect { event ->
                        when (event) {

                            is FragmentGiftStoreViewModel.GiftStoreEvent.ShowError ->
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()

                            is FragmentGiftStoreViewModel.GiftStoreEvent.OpenUrl ->
                                openUrlInCustomTab(event.url)

                            is FragmentGiftStoreViewModel.GiftStoreEvent.ClaimSuccess ->
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()

                            is FragmentGiftStoreViewModel.GiftStoreEvent.RedeemSuccess ->
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()

                            is FragmentGiftStoreViewModel.GiftStoreEvent.RefreshUserData ->
                                userSharedViewModel.triggerDataRefresh()
                        }
                    }
                }
            }
        }
    }

    private fun openUrlInCustomTab(url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), url.toUri())
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
    }
}
