package com.idealink.vinty.ui.fragments.giftstore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.idealink.vinty.App
import com.idealink.vinty.databinding.FragmentGiftStoreTabBinding
import com.idealink.vinty.ui.adapters.AvailableGiftCardAdapter
import com.idealink.vinty.ui.viewmodel.FragmentGiftStoreViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class GiftStoreHistoryFragment : Fragment() {

    private var _binding: FragmentGiftStoreTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentGiftStoreViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private lateinit var adapter: AvailableGiftCardAdapter
    private var isRedeemedArg: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isRedeemedArg = it.getBoolean(ARG_IS_REDEEMED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftStoreTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        observeViewModel()
        
        // Initial fetch based on type
        if (isRedeemedArg) {
            viewModel.fetchGiftCardHistory()
        } else {
            viewModel.fetchAvailableGiftCards()
        }

    }

    private fun setupAdapter() {
        adapter = AvailableGiftCardAdapter(isRedeemed = isRedeemedArg) { giftCard ->
            if (!isRedeemedArg) {
                viewModel.claimAvailableGiftCard(giftCard)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GiftStoreHistoryFragment.adapter
        }
    }

    private fun observeViewModel() {
        if (isRedeemedArg) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.redeemedGiftCards.collect { cards ->
                    showEmptyOrList(cards.isEmpty(), "No redeemed gift cards", "Your redeemed gift cards will appear here.")
                    adapter.submitList(cards)
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isLoadingRedeemed.collect { loading ->
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.availableGiftCards.collect { cards ->
                    showEmptyOrList(cards.isEmpty(), "No gift cards available", "Once you earn gift cards, they will appear here.")
                    adapter.submitList(cards)
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isLoadingAvailable.collect { loading ->
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showEmptyOrList(isEmpty: Boolean, title: String, subtitle: String) {
        if (isEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.lottieAnimation.visibility = View.VISIBLE
            binding.emptyText.visibility = View.VISIBLE
            binding.emptyText2.visibility = View.VISIBLE
            binding.emptyText.text = title
            binding.emptyText2.text = subtitle
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.lottieAnimation.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
            binding.emptyText2.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IS_REDEEMED = "is_redeemed"

        @JvmStatic
        fun newInstance(isRedeemed: Boolean) =
            GiftStoreHistoryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_REDEEMED, isRedeemed)
                }
            }
    }
}
