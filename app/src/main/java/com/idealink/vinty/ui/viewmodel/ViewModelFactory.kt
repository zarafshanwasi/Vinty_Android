package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.ui.fragments.jackpot.JackpotViewModel
import com.idealink.vinty.ui.fragments.leaderboard.LeaderboardViewModel
import com.idealink.vinty.ui.fragments.mission.MissionViewModel

class ViewModelFactory(
    private val repository: VintyRepository
) : ViewModelProvider.Factory {

    private val creators: Map<Class<out ViewModel>, () -> ViewModel> = mapOf(
        LeaderboardViewModel::class.java to { LeaderboardViewModel(repository) },
        MissionViewModel::class.java to { MissionViewModel(repository) },
        HomeViewModel::class.java to { HomeViewModel(repository) },
        AuthViewModel::class.java to { AuthViewModel(repository) },
        UserSharedViewModel::class.java to { UserSharedViewModel(repository) },
        BadgeViewModel::class.java to { BadgeViewModel(repository) },
        AdHistoryViewModel::class.java to { AdHistoryViewModel(repository) },
        JackpotViewModel::class.java to { JackpotViewModel(repository) },
        ChangePasswordViewModel::class.java to { ChangePasswordViewModel(repository) },
        FragmentGiftStoreViewModel::class.java to { FragmentGiftStoreViewModel(repository) }
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: creators.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
            ?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")

        return creator.invoke() as T
    }
}