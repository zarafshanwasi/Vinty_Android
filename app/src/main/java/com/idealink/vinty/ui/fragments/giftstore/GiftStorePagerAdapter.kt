package com.idealink.vinty.ui.fragments.giftstore

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class GiftStorePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GiftStoreShopFragment()
            1 -> GiftStoreHistoryFragment.newInstance(isRedeemed = false)
            2 -> GiftStoreHistoryFragment.newInstance(isRedeemed = true)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
