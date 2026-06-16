package com.idealink.vinty.ui.fragments.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idealink.vinty.ui.fragments.ad.AdHistoryFragment
import com.idealink.vinty.ui.fragments.ad.LotteryHistoryFragment
import com.idealink.vinty.ui.fragments.ad.RewardHistoryFragment

class HistoryViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LotteryHistoryFragment()
            1 -> RewardHistoryFragment()
            2 -> AdHistoryFragment()
            else -> AdHistoryFragment()
        }
    }
}