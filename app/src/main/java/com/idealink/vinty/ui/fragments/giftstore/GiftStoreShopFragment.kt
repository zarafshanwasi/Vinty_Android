package com.idealink.vinty.ui.fragments.giftstore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.idealink.vinty.App
import com.idealink.vinty.databinding.FragmentGiftStoreTabBinding
import com.idealink.vinty.ui.adapters.ShopGiftCardAdapter
import com.idealink.vinty.ui.viewmodel.FragmentGiftStoreViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.AnalyticsEvent
import com.idealink.vinty.utils.AnalyticsManager
import com.idealink.vinty.utils.FacebookAnalyticsManager
import com.idealink.vinty.utils.FacebookEvent
import kotlinx.coroutines.launch

class GiftStoreShopFragment : Fragment() {

    private var _binding: FragmentGiftStoreTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentGiftStoreViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private lateinit var adapter: ShopGiftCardAdapter

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
        
        // Initial fetch
        viewModel.fetchShopGiftCards()
    }

    private fun setupAdapter() {
        adapter = ShopGiftCardAdapter { giftCard ->
            // Track redeem attempt
            AnalyticsManager.log(AnalyticsEvent.REDEEM_ATTEMPT)
            FacebookAnalyticsManager.logEvent(FacebookEvent.REDEEM_ATTEMPT)

            viewModel.claimShopGiftCard(giftCard.id)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@GiftStoreShopFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopGiftCards.collect { cards ->
                if (cards.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.lottieAnimation.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText2.visibility = View.VISIBLE
                    binding.emptyText.text = "No gift cards yet"
                    binding.emptyText2.text = "Gift cards will appear here once they are available."
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.lottieAnimation.visibility = View.GONE
                    binding.emptyText.visibility = View.GONE
                    binding.emptyText2.visibility = View.GONE
                    adapter.submitList(cards)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadingShop.collect { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
