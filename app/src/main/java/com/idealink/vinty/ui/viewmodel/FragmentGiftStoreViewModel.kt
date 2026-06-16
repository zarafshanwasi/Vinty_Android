package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.model.AvailableGiftCard
import com.idealink.vinty.data.model.Redemption
import com.idealink.vinty.data.model.ShopGiftCard
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Shared by Activity
class FragmentGiftStoreViewModel(
    private val repository: VintyRepository
) : ViewModel() {

    // Loading states
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _isLoadingShop = MutableStateFlow(false)
    val isLoadingShop: StateFlow<Boolean> = _isLoadingShop

    private val _isLoadingAvailable = MutableStateFlow(false)
    val isLoadingAvailable: StateFlow<Boolean> = _isLoadingAvailable

    private val _isLoadingRedeemed = MutableStateFlow(false)
    val isLoadingRedeemed: StateFlow<Boolean> = _isLoadingRedeemed

    // Gift card data
    private val _shopGiftCards = MutableStateFlow<List<ShopGiftCard>>(emptyList())
    val shopGiftCards: StateFlow<List<ShopGiftCard>> = _shopGiftCards

    private val _availableGiftCards = MutableStateFlow<List<AvailableGiftCard>>(emptyList())
    val availableGiftCards: StateFlow<List<AvailableGiftCard>> = _availableGiftCards

    private val _redeemedGiftCards = MutableStateFlow<List<Redemption>>(emptyList())
    val redeemedGiftCards: StateFlow<List<Redemption>> = _redeemedGiftCards

    // Events
    private val _events = Channel<GiftStoreEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    sealed class GiftStoreEvent {
        data class ShowError(val message: String) : GiftStoreEvent()
        data class OpenUrl(val url: String) : GiftStoreEvent()
        data class ClaimSuccess(val message: String) : GiftStoreEvent()
        data class RedeemSuccess(val message: String) : GiftStoreEvent()
        object RefreshUserData : GiftStoreEvent()
    }



    fun fetchShopGiftCards() {
        viewModelScope.launch {
            _isLoadingShop.value = true
            try {
                val response = repository.getShopGiftCards()
                if (response.success) {
                    _shopGiftCards.value = response.packages
                } else {
                    _events.send(GiftStoreEvent.ShowError("Failed to load shop gift cards"))
                }
            } catch (e: Exception) {
                _events.send(GiftStoreEvent.ShowError(e.message ?: "Unknown error occurred"))
            } finally {
                _isLoadingShop.value = false
            }
        }
    }

    fun fetchAvailableGiftCards() {
        viewModelScope.launch {
            _isLoadingAvailable.value = true
            try {
                val response = repository.getAvailableGiftCards()
                if (response.success) {
                    _availableGiftCards.value = response.cards
                } else {
                    _events.send(GiftStoreEvent.ShowError("Failed to load available gift cards"))
                }
            } catch (e: Exception) {
                _events.send(GiftStoreEvent.ShowError(e.message ?: "Unknown error occurred"))
            } finally {
                _isLoadingAvailable.value = false
            }
        }
    }

    fun fetchGiftCardHistory() {
        viewModelScope.launch {
            _isLoadingRedeemed.value = true
            try {
                val response = repository.getGiftCardHistory()
                if (response.success) {
                    _redeemedGiftCards.value = response.history
                } else {
                    _events.send(GiftStoreEvent.ShowError("Failed to load redemption history"))
                }
            } catch (e: Exception) {
                _events.send(GiftStoreEvent.ShowError(e.message ?: "Unknown error occurred"))
            } finally {
                _isLoadingRedeemed.value = false
            }
        }
    }

    fun claimShopGiftCard(packageId: String) {
        viewModelScope.launch {
            try {
                val response = repository.claimShopGiftCard(packageId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val claimResponse = response.body()!!
                    _events.send(GiftStoreEvent.ClaimSuccess("Gift card claimed successfully!"))
                    
                    // Open URL if available
                    if (claimResponse.giftCard.url.isNotEmpty()) {
                        _events.send(GiftStoreEvent.OpenUrl(claimResponse.giftCard.url))
                    }
                    
                    // Refresh data
                    _events.send(GiftStoreEvent.RefreshUserData)
                    fetchShopGiftCards()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _events.send(GiftStoreEvent.ShowError(errorBody ?: "Failed to claim gift card"))
                }
            } catch (e: Exception) {
                _events.send(GiftStoreEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    fun claimAvailableGiftCard(giftCard: AvailableGiftCard) {
        viewModelScope.launch {
            try {
                val giftCardId = giftCard.giftCard.id ?: return@launch
                val response = repository.claimGiftCard(giftCardId, giftCard.giftCard.code)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _events.send(GiftStoreEvent.RedeemSuccess("Gift card redeemed successfully!"))
                    
                    // Open URL if available
                    giftCard.giftCard.url?.let { url ->
                        _events.send(GiftStoreEvent.OpenUrl(url))
                    }
                    
                    // Refresh available cards
                    fetchAvailableGiftCards()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _events.send(GiftStoreEvent.ShowError(errorBody ?: "Failed to redeem gift card"))
                }
            } catch (e: Exception) {
                _events.send(GiftStoreEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }
}
