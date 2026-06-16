package com.idealink.vinty.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import com.idealink.vinty.databinding.FragmentUserHistoryBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class UserHistoryFragment : Fragment() {

    private var _binding: FragmentUserHistoryBinding? = null
    private val binding get() = _binding!!

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    // =====================================================
    // Lifecycle
    // =====================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupViewPager()
        observeProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // =====================================================
    // Observers (StateFlow handles initial value automatically)
    // =====================================================

    private fun observeProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userSharedViewModel.user.collect { user ->
                        user?.let { binding.profileHeader.updateProfile(it) }
                    }
                }

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
            }
        }
    }

    // =====================================================
    // Clicks
    // =====================================================

    private fun setupClickListeners() {
        binding.backBtn4.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_userHistoryFragment_to_profileFragment)
        }
    }

    // =====================================================
    // ViewPager + Tabs
    // =====================================================

    private fun setupViewPager() {

        val adapter = HistoryViewPagerAdapter(this)
        binding.viewPager2.adapter = adapter

        val tabTitles = listOf("Lotteries", "Rewards", "Ads")

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            val customBinding = CustomTabBinding.inflate(layoutInflater)
            customBinding.tabText.text = tabTitles[position]
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

    // =====================================================
    // Tab helpers
    // =====================================================

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
}
