package com.idealink.vinty.ui.fragments.badge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.model.Badge
import com.idealink.vinty.databinding.FragmentBadgeBinding
import com.idealink.vinty.ui.viewmodel.BadgeViewModel
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class BadgeFragment : Fragment() {

    private var _binding: FragmentBadgeBinding? = null
    private val binding get() = _binding!!

    private val badgeViewModel: BadgeViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private lateinit var badgeAdapter: BadgeAdapter

    // ----------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBadgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
        observeViewModels()

        // Load badges ONLY once
        badgeViewModel.loadBadges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ----------------------------------------------------
    // Setup UI (NO collectors here)
    // ----------------------------------------------------

    private fun setupUI() {
        binding.backBtn4.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_badgeFragment_to_profileFragment)
        }
    }

    private fun setupRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            showBadgeBottomSheet(badge)
        }

        binding.badgeRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = badgeAdapter
            setHasFixedSize(true)
        }
    }

    // ----------------------------------------------------
    // Observers (ALL collectors live here)
    // ----------------------------------------------------

    private fun observeViewModels() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // -----------------------------
                // Profile User (StateFlow replay)
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
                // Loading
                // -----------------------------
                launch {
                    badgeViewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility =
                            if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                // -----------------------------
                // Error
                // -----------------------------
                launch {
                    badgeViewModel.error.collect { error ->
                        error?.let {
                            showError(it)
                            badgeViewModel.clearError()
                        }
                    }
                }

                // -----------------------------
                // Badges list
                // -----------------------------
                launch {
                    badgeViewModel.badges.collect { badges ->
                        if (badges.isEmpty() && !badgeViewModel.isLoading.value) {
                            binding.emptyText.visibility = View.VISIBLE
                            binding.badgeRecyclerView.visibility = View.GONE
                        } else {
                            binding.emptyText.visibility = View.GONE
                            binding.badgeRecyclerView.visibility = View.VISIBLE
                            badgeAdapter.submitList(badges)
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Helpers
    // ----------------------------------------------------

    private fun showBadgeBottomSheet(badge: Badge) {
        BadgeDetailBottomSheet(badge)
            .show(parentFragmentManager, "BadgeDetail")
    }

    private fun showError(message: String) {
        binding.emptyText.visibility = View.VISIBLE
        binding.emptyText.text = message
        binding.badgeRecyclerView.visibility = View.GONE
    }
}
