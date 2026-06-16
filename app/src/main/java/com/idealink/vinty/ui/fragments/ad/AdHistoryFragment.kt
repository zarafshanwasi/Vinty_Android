package com.idealink.vinty.ui.fragments.ad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.idealink.vinty.App
import com.idealink.vinty.databinding.FragmentHistoryTabBinding
import com.idealink.vinty.ui.viewmodel.AdHistoryViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

// ===== AD HISTORY FRAGMENT =====
class AdHistoryFragment : Fragment() {
    private var _binding: FragmentHistoryTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdHistoryAdapter

    private val adHistoryViewModel: AdHistoryViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdHistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdHistoryFragment.adapter
        }

        observeViewModel()

        adHistoryViewModel.loadAdHistory()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.adHistory.collect { data ->
                if (data.isEmpty()) {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText2.visibility = View.VISIBLE
                    binding.lottieAnimation.visibility = View.VISIBLE
                    binding.emptyText.text = "No ads yet"
                    binding.emptyText2.text = "Once you earn rewards, your history will shown here."
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.GONE
                    binding.emptyText2.visibility = View.GONE
                    binding.lottieAnimation.visibility = View.GONE
                    adapter.submitList(data as List<AdHistory?>?)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.error.collect { error ->
                error?.let {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = it
                    adHistoryViewModel.clearError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ===== LOTTERY HISTORY FRAGMENT =====
class LotteryHistoryFragment : Fragment() {
    private var _binding: FragmentHistoryTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var lotteryAdapter: LotteryHistoryAdapter

    private val adHistoryViewModel: AdHistoryViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lotteryAdapter = LotteryHistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lotteryAdapter
        }
        observeViewModel()
        adHistoryViewModel.loadLotteryHistory()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.lotteryHistory.collect { data ->
                if (data.isEmpty()) {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText2.visibility = View.VISIBLE
                    binding.lottieAnimation.visibility = View.VISIBLE
                    binding.emptyText.text = "No lotteries yet"
                    binding.emptyText2.text = "Once you earn lotteries, your history will shown here."
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.GONE
                    binding.emptyText2.visibility = View.GONE
                    binding.lottieAnimation.visibility = View.GONE
                    lotteryAdapter.submitList(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.error.collect { error ->
                error?.let {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = it
                    adHistoryViewModel.clearError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ===== REWARD HISTORY FRAGMENT =====
class RewardHistoryFragment : Fragment() {
    private var _binding: FragmentHistoryTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var rewardAdapter: RewardHistoryAdapter

    private val adHistoryViewModel: AdHistoryViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rewardAdapter = RewardHistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rewardAdapter
        }
        observeViewModel()
        adHistoryViewModel.loadRewardHistory()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.rewardHistory.collect { data ->
                if (data.isEmpty()) {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText2.visibility = View.VISIBLE
                    binding.lottieAnimation.visibility = View.VISIBLE
                    binding.emptyText.text = "No rewards yet"
                    binding.emptyText2.text = "Once you earn rewards, your history will shown here."
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.GONE
                    binding.emptyText2.visibility = View.GONE
                    binding.lottieAnimation.visibility = View.GONE
                    rewardAdapter.submitList(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adHistoryViewModel.error.collect { error ->
                error?.let {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = it
                    adHistoryViewModel.clearError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}