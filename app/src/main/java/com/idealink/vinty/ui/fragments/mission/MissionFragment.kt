package com.idealink.vinty.ui.fragments.mission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.idealink.vinty.data.model.Mission
import com.idealink.vinty.databinding.FragmentMissionBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.AnalyticsEvent
import com.idealink.vinty.utils.AnalyticsManager
import com.idealink.vinty.utils.FacebookAnalyticsManager
import com.idealink.vinty.utils.FacebookEvent
import kotlinx.coroutines.launch

class MissionFragment : Fragment() {

    private var _binding: FragmentMissionBinding? = null
    private val binding get() = _binding!!

    private lateinit var missionAdapter: MissionAdapter
    private val TAG = "MissionFragment"

    private val missionViewModel: MissionViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    // ----------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupUI()
        observeViewModels()

        // Load once only
        missionViewModel.loadMissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ----------------------------------------------------
    // Setup (NO collectors)
    // ----------------------------------------------------

    private fun setupRecycler() {
        missionAdapter = MissionAdapter { mission ->
            claimMission(mission)
        }

        binding.recyclerViewMission.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = missionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupUI() {
        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_missionFragment_to_profileFragment)
        }
    }

    // ----------------------------------------------------
    // Observers (ALL flows here)
    // ----------------------------------------------------

    private fun observeViewModels() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Missions list
                launch {
                    missionViewModel.missions.collect { missions ->
                        missionAdapter.submitList(missions)
                    }
                }

                // Loading
                launch {
                    missionViewModel.isLoading.collect { isLoading ->
                        showLoading(isLoading)
                    }
                }

                // Error
                launch {
                    missionViewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Mission updates → refresh list
                launch {
//                    missionViewModel.refreshDataTrigger.collect { timestamp ->
//                        if (timestamp > 0) {
//                            Log.d(TAG, "Mission update triggered")
//                            missionViewModel.loadMissionsInBackground()
//                        }
//                    }
                }

                // User (StateFlow replay)
                launch {
                    userSharedViewModel.user.collect { user ->
                        user?.let(binding.profileHeader::updateProfile)
                    }
                }

                // Badge stats
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

    // ----------------------------------------------------

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerViewMission.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun claimMission(mission: Mission) {
        missionViewModel.claimMission(mission.id) { success, errorMessage, _, _ ->
            if (success) {
                // Track mission progress
                AnalyticsManager.log(AnalyticsEvent.MISSION_PROGRESS_UP)
                FacebookAnalyticsManager.logEvent(FacebookEvent.MISSION_PROGRESS_UP)

                Toast.makeText(requireContext(), "Reward claimed!", Toast.LENGTH_SHORT).show()
                // Local mission state is already updated optimistically in MissionViewModel.
                // Refresh user balance (header) so the new ticket count appears.
                userSharedViewModel.triggerDataRefresh()
            } else {
                Toast.makeText(
                    requireContext(),
                    errorMessage ?: "Failed to claim reward",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
