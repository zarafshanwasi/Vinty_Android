package com.idealink.vinty.data.repository

import com.idealink.vinty.data.UserHistoryResponse
import com.idealink.vinty.data.api.ApiException
import com.idealink.vinty.data.api.VintyApiService
import com.idealink.vinty.data.model.AdViewRequest
import com.idealink.vinty.data.model.AdViewResponse
import com.idealink.vinty.data.model.ApplyInviteCodeRequest
import com.idealink.vinty.data.model.ApplyInviteCodeResponse
import com.idealink.vinty.data.model.AvatarUploadResponse
import com.idealink.vinty.data.model.BadgeGalleryResponse
import com.idealink.vinty.data.model.BadgeProgressResponse
import com.idealink.vinty.data.model.BadgeStatsResponse
import com.idealink.vinty.data.model.ChangePasswordRequest
import com.idealink.vinty.data.model.ChangePasswordResponse
import com.idealink.vinty.data.model.ClaimBadgeResponse
import com.idealink.vinty.data.model.DeleteAccountRequest
import com.idealink.vinty.data.model.DeleteAccountResponse
import com.idealink.vinty.data.model.ForgotPasswordRequest
import com.idealink.vinty.data.model.ForgotPasswordResponse
import com.idealink.vinty.data.model.ForgotPasswordVerifyOtpRequest
import com.idealink.vinty.data.model.GainXpRequest
import com.idealink.vinty.data.model.GainXpResponse
import com.idealink.vinty.data.model.GeneralResponse
import com.idealink.vinty.data.model.GiftCardsResponse
import com.idealink.vinty.data.model.GiftClaimResponse
import com.idealink.vinty.data.model.GoogleLoginRequest
import com.idealink.vinty.data.model.InviteCodeResponse
import com.idealink.vinty.data.model.LeaderboardResponse
import com.idealink.vinty.data.model.LoginRequest
import com.idealink.vinty.data.model.LoginResponse
import com.idealink.vinty.data.model.LogoutResponse
import com.idealink.vinty.data.model.LotteryHistory
import com.idealink.vinty.data.model.MissionClaimResponse
import com.idealink.vinty.data.model.MissionsResponse
import com.idealink.vinty.data.model.NewRedemptionResponse
import com.idealink.vinty.data.model.OtpResponse
import com.idealink.vinty.data.model.RedemptionResponse
import com.idealink.vinty.data.model.RegisterRequest
import com.idealink.vinty.data.model.RegisterResponse
import com.idealink.vinty.data.model.ResendVerificationEmailRequest
import com.idealink.vinty.data.model.RewardHistory
import com.idealink.vinty.data.model.ShopGiftCardsResponse
import com.idealink.vinty.data.model.SpinResult
import com.idealink.vinty.data.model.SpinStatusResponse
import com.idealink.vinty.data.model.TokenResponse
import com.idealink.vinty.data.model.UserProfileResponse
import com.idealink.vinty.data.model.VerifyEmailOtpRequest
import com.idealink.vinty.ui.fragments.ad.AdHistory
import okhttp3.MultipartBody
import retrofit2.Response

interface VintyRepository {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun googleLogin(request: GoogleLoginRequest): LoginResponse
    suspend fun verifyEmailOtp(request: VerifyEmailOtpRequest): GeneralResponse
    suspend fun resendVerificationEmail(request: ResendVerificationEmailRequest): OtpResponse
    suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse
    suspend fun forgotPasswordVerifyOtp(request: ForgotPasswordVerifyOtpRequest): GeneralResponse
    suspend fun getLeaderboard(period: String = "all-time", limit: Int = 50): LeaderboardResponse
    suspend fun refreshToken(body: Map<String, String>): TokenResponse
    suspend fun logout(): Response<LogoutResponse>
    suspend fun deleteAccount(request: DeleteAccountRequest): Response<DeleteAccountResponse>
    suspend fun getMissions(): Response<MissionsResponse>
    suspend fun recordAdView(request: AdViewRequest): Response<AdViewResponse>
    suspend fun gainXp(request: GainXpRequest): Response<GainXpResponse>
    suspend fun claimMission(request: Map<String, String>): Response<MissionClaimResponse>
    suspend fun getAdHistory(): Response<List<AdHistory>>
    suspend fun getLotteryHistory(): Response<List<LotteryHistory>>
    suspend fun getRewardHistory(): Response<List<RewardHistory>>
    suspend fun getBadgeGallery(): Response<BadgeGalleryResponse>
    suspend fun getMyInviteCode(): Response<InviteCodeResponse>
    suspend fun applyInviteCode(request: ApplyInviteCodeRequest): ApplyInviteCodeResponse
    suspend fun getUserProfile(): UserProfileResponse
    suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Response<AvatarUploadResponse>
    suspend fun getSpinStatus(): SpinStatusResponse
    suspend fun saveSpinResult(request: Map<String, String>): Response<SpinResult>
    suspend fun changePassword(request: ChangePasswordRequest): ChangePasswordResponse
    suspend fun getUserHistory(): UserHistoryResponse
    suspend fun getBadgeProgress(): BadgeProgressResponse
    suspend fun getShopGiftCards(): ShopGiftCardsResponse
    suspend fun getAvailableGiftCards(): GiftCardsResponse
    suspend fun getGiftCardHistory(): RedemptionResponse
    suspend fun claimGiftCard(giftId: String, code: String): Response<NewRedemptionResponse>
    suspend fun claimShopGiftCard(packageId: String): Response<GiftClaimResponse>
    suspend fun getBadgeStats(): BadgeStatsResponse

    suspend fun claimBadgeReward(badgeId: String): Response<ClaimBadgeResponse>

    suspend fun markNotificationAsClicked(notificationId: String): Response<GeneralResponse>
    suspend fun markNotificationAsRead(notificationId: String): Response<GeneralResponse>
    suspend fun registerDevice(request: Map<String, String>): Response<GeneralResponse>
    suspend fun unregisterDevice(request: Map<String, String>): Response<GeneralResponse>
}

class VintyRepositoryImpl(private val apiService: VintyApiService) : VintyRepository {
    
    private suspend fun <T> apiCall(call: suspend () -> Response<T>): T {
        val response = call()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response body is null")
        } else {
            val body = response.errorBody()?.string()
            throw ApiException(response.code(), body, "API Error: ${response.code()}")
        }
    }

    override suspend fun register(request: RegisterRequest) = apiCall { apiService.register(request) }
    override suspend fun login(request: LoginRequest) = apiCall { apiService.login(request) }
    override suspend fun googleLogin(request: GoogleLoginRequest) = apiCall { apiService.googleLogin(request) }
    override suspend fun verifyEmailOtp(request: VerifyEmailOtpRequest) = apiCall { apiService.verifyEmailOtp(request) }
    override suspend fun resendVerificationEmail(request: ResendVerificationEmailRequest) = apiCall { apiService.resendVerificationEmail(request) }
    override suspend fun forgotPassword(request: ForgotPasswordRequest) = apiCall { apiService.forgotPassword(request) }
    override suspend fun forgotPasswordVerifyOtp(request: ForgotPasswordVerifyOtpRequest) = apiCall { apiService.forgotPasswordVerifyOtp(request) }
    override suspend fun getLeaderboard(period: String, limit: Int) = apiCall { apiService.getLeaderboard(period, limit) }
    override suspend fun refreshToken(body: Map<String, String>) = apiCall { apiService.refreshToken(body) }
    override suspend fun logout() = apiService.logout()
    override suspend fun deleteAccount(request: DeleteAccountRequest) = apiService.deleteAccount(request)
    override suspend fun getMissions() = apiService.getMissions()
    override suspend fun recordAdView(request: AdViewRequest) = apiService.recordAdView(request)
    override suspend fun gainXp(request: GainXpRequest) = apiService.gainXp(request)
    override suspend fun claimMission(request: Map<String, String>) = apiService.claimMission(request)
    override suspend fun getAdHistory() = apiService.getAdHistory()
    override suspend fun getLotteryHistory() = apiService.getLotteryHistory()
    override suspend fun getRewardHistory() = apiService.getRewardHistory()
    override suspend fun getBadgeGallery() = apiService.getBadgeGallery()
    override suspend fun getMyInviteCode() = apiService.getMyInviteCode()
    override suspend fun applyInviteCode(request: ApplyInviteCodeRequest) = apiCall { apiService.applyInviteCode(request) }
    override suspend fun getUserProfile() = apiCall { apiService.getUserProfile() }
    override suspend fun uploadAvatar(avatarPart: MultipartBody.Part) = apiService.uploadAvatar(avatarPart)
    override suspend fun getSpinStatus() = apiCall { apiService.getSpinStatus() }
    override suspend fun saveSpinResult(request: Map<String, String>) = apiService.saveSpinResult(request)
    override suspend fun changePassword(request: ChangePasswordRequest) = apiCall { apiService.changePassword(request) }
    override suspend fun getUserHistory() = apiCall { apiService.getUserHistory() }
    override suspend fun getBadgeProgress() = apiCall { apiService.getBadgeProgress() }
    override suspend fun getShopGiftCards() = apiCall { apiService.getShopGiftCards() }
    override suspend fun getAvailableGiftCards() = apiCall { apiService.getAvailableGiftCards() }
    override suspend fun getGiftCardHistory() = apiCall { apiService.getGiftCardHistory() }
    
    override suspend fun claimGiftCard(giftId: String, code: String): Response<NewRedemptionResponse> = 
        apiService.claimGiftCard(giftId, mapOf("code" to code))
        
    override suspend fun claimShopGiftCard(packageId: String): Response<GiftClaimResponse> = 
        apiService.claimShopGiftCard(packageId)
        
    override suspend fun getBadgeStats() = apiCall { apiService.getBadgeStats() }

    override suspend fun claimBadgeReward(badgeId: String) = apiService.claimBadgeReward(badgeId)

    override suspend fun markNotificationAsClicked(notificationId: String) = apiService.markNotificationAsClicked(notificationId)
    override suspend fun markNotificationAsRead(notificationId: String) = apiService.markNotificationAsRead(notificationId)
    override suspend fun registerDevice(request: Map<String, String>) = apiService.registerDevice(request)
    override suspend fun unregisterDevice(request: Map<String, String>) = apiService.unregisterDevice(request)
}
