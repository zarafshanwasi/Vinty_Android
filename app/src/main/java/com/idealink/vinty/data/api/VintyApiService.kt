package com.idealink.vinty.data.api

import com.idealink.vinty.data.UserHistoryResponse
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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VintyApiService {

    // =========================================================
    // AUTHENTICATION APIs
    // =========================================================

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @POST("auth/verify-email-otp")
    suspend fun verifyEmailOtp(
        @Body request: VerifyEmailOtpRequest
    ): Response<GeneralResponse>

    @POST("auth/resend-verification-email")
    suspend fun resendVerificationEmail(
        @Body request: ResendVerificationEmailRequest
    ): Response<OtpResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

    @POST("auth/forgot-password/verify-otp")
    suspend fun forgotPasswordVerifyOtp(
        @Body request: ForgotPasswordVerifyOtpRequest
    ): Response<GeneralResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body body: Map<String, String>
    ): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @HTTP(method = "DELETE", path = "user/account", hasBody = true)
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): Response<DeleteAccountResponse>


    // =========================================================
    // USER PROFILE
    // =========================================================

    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @Multipart
    @POST("user/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<AvatarUploadResponse>

    @POST("user/ad-view")
    suspend fun recordAdView(
        @Body request: AdViewRequest
    ): Response<AdViewResponse>

    @PATCH("user/gain-xp")
    suspend fun gainXp(
        @Body request: GainXpRequest
    ): Response<GainXpResponse>

    @GET("user/ad-history")
    suspend fun getAdHistory(): Response<List<AdHistory>>

    @GET("user/lottery-history")
    suspend fun getLotteryHistory(): Response<List<LotteryHistory>>

    @GET("user/reward-history")
    suspend fun getRewardHistory(): Response<List<RewardHistory>>

    @GET("user/history")
    suspend fun getUserHistory(): Response<UserHistoryResponse>

    @POST("user/fcm-token")
    suspend fun registerFcmToken(
        @Body request: Map<String, String>
    ): Response<GeneralResponse>

    // =========================================================
    // NOTIFICATIONS APIs
    // =========================================================

    @POST("notifications/register-device")
    suspend fun registerDevice(
        @Body request: Map<String, String>
    ): Response<GeneralResponse>

    @POST("notifications/unregister-device")
    suspend fun unregisterDevice(
        @Body request: Map<String, String>
    ): Response<GeneralResponse>

    @POST("notifications/{notificationId}/clicked")
    suspend fun markNotificationAsClicked(
        @Path("notificationId") notificationId: String
    ): Response<GeneralResponse>

    @POST("notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String
    ): Response<GeneralResponse>

    // =========================================================
    // MISSIONS APIs
    // =========================================================

    @GET("missions")
    suspend fun getMissions(): Response<MissionsResponse>

    @POST("missions/claim")
    suspend fun claimMission(
        @Body request: Map<String, String>
    ): Response<MissionClaimResponse>



    // =========================================================
    // LEADERBOARD APIs
    // =========================================================

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("period") period: String = "all-time",
        @Query("limit") limit: Int = 50
    ): Response<LeaderboardResponse>


    // =========================================================
    // BADGES APIs
    // =========================================================

    @GET("badges/gallery")
    suspend fun getBadgeGallery(): Response<BadgeGalleryResponse>

    @GET("badges/stats")
    suspend fun getBadgeStats(): Response<BadgeStatsResponse>

    @GET("missions/badge-progress")
    suspend fun getBadgeProgress(): Response<BadgeProgressResponse>

    @POST("badges/{badgeId}/claim")
    suspend fun claimBadgeReward(
        @Path("badgeId") badgeId: String
    ): Response<ClaimBadgeResponse>


    @GET("invites/my-code")
    suspend fun getMyInviteCode(): Response<InviteCodeResponse>

    @POST("invites/apply-code")
    suspend fun applyInviteCode(
        @Body request: ApplyInviteCodeRequest
    ): Response<ApplyInviteCodeResponse>

    // =========================================================
    // Games
    // =========================================================

    @GET("games/spin-status")
    suspend fun getSpinStatus(): Response<SpinStatusResponse>

    @POST("games/spin-result")
    suspend fun saveSpinResult(
        @Body request: Map<String, String>
    ): Response<SpinResult>

    // =========================================================
    // GIFT CARDS APIs
    // =========================================================

    @GET("packages")
    suspend fun getShopGiftCards(): Response<ShopGiftCardsResponse>

    @GET("gift-cards/claimed")
    suspend fun getAvailableGiftCards(): Response<GiftCardsResponse>

    @GET("gift-cards/history")
    suspend fun getGiftCardHistory(): Response<RedemptionResponse>

    @POST("gift-cards/{giftId}/redeem")
    suspend fun claimGiftCard(
        @Path("giftId") giftId: String,
        @Body request: Map<String, String>
    ): Response<NewRedemptionResponse>

    @POST("packages/{packageId}/claim")
    suspend fun claimShopGiftCard(
        @Path("packageId") packageId: String
    ): Response<GiftClaimResponse>
}