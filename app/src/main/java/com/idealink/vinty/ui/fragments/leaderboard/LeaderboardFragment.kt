package com.idealink.vinty.ui.fragments.leaderboard

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.databinding.FragmentLeaderboardBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeaderboardViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private lateinit var adapter: LeaderboardAdapter

    // ----------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecycler()
        observeViewModels()

        // Load only once
        viewModel.loadLeaderboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ----------------------------------------------------
    // Setup (NO collectors here)
    // ----------------------------------------------------

    private fun setupUI() {
        binding.backBtn4.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_leaderboardFragment_to_profileFragment)
        }
    }

    private fun setupRecycler() {
        adapter = LeaderboardAdapter()
        binding.recyclerViewLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLeaderboard.adapter = adapter
    }

    // ----------------------------------------------------
    // Observers (ALL flows here)
    // ----------------------------------------------------

    private fun observeViewModels() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // -----------------------------
                // Loading
                // -----------------------------
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.leaderboardLoader.visibility =
                            if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                // -----------------------------
                // Leaderboard list
                // -----------------------------
                launch {
                    viewModel.leaderboard.collect { response ->
                        response?.leaderboard?.let {
                            adapter.submitList(it)
                        }
                    }
                }

                // -----------------------------
                // Mission updates → reload
                // -----------------------------
//                launch {
//                    missionViewModel.missionUpdateTrigger.collect { timestamp ->
//                        if (timestamp > 0) {
//                            viewModel.loadLeaderboard()
//                        }
//                    }
//                }

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
            }
        }
    }
}
