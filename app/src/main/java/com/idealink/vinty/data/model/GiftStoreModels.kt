package com.idealink.vinty.data.model

import com.google.gson.annotations.SerializedName

// MARK: - Shop Gift Cards (CollectionView in iOS)
data class ShopGiftCardsResponse(
    val success: Boolean,
    val packages: List<ShopGiftCard>
)

data class ShopGiftCard(
    val id: String,
    val packageName: String,
    val value: Int,
    val minTickets: Int,
    val isActive: Boolean,
    val description: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val availableCount: Int,
    val isAvailable: Boolean
)

// MARK: - Available Gift Cards (TableView in iOS)
data class GiftCardsResponse(
    val success: Boolean,
    val cards: List<AvailableGiftCard>
)

data class AvailableGiftCard(
    val id: String,
    @SerializedName("package") val giftPackage: AvailableGiftPackage,
    val giftCard: AvailableGiftDetails,
    val claimedAt: String,
    val expiresAt: String
)

data class AvailableGiftPackage(
    val id: String,
    val packageName: String,
    val value: Int,
    val minTickets: Int,
    val isActive: Boolean,
    val description: String,
    val imageUrl: String,
    val createdAt: String,
    val updatedAt: String
)

data class AvailableGiftDetails(
    val id: String?,
    val code: String,
    val pin: String?,
    val url: String?
)

// MARK: - Redemption History (TableView in iOS)
data class RedemptionResponse(
    val success: Boolean,
    val history: List<Redemption>
)

data class Redemption(
    val id: String,
    @SerializedName("package") val giftPackage: Package,
    val giftCard: AvailableGiftDetails,
    val redeemedAt: String
)

data class Package(
    val id: String,
    val packageName: String,
    val value: Int,
    val minTickets: Int,
    val isActive: Boolean,
    val description: String,
    val imageUrl: String,
    val createdAt: String,
    val updatedAt: String
)

// MARK: - Claim Shop Gift Card Response
data class GiftClaimResponse(
    val success: Boolean,
    val claimedPackageId: String,
    @SerializedName("package") val giftPackage: ShopClaimPackage,
    val giftCard: ShopClaimGiftCard,
    val claimedAt: String,
    val expiresAt: String
)

data class ShopClaimPackage(
    val id: String,
    val packageName: String,
    val value: Int,
    val minTickets: Int,
    val isActive: Boolean,
    val description: String,
    val imageUrl: String,
    val createdAt: String,
    val updatedAt: String
)

data class ShopClaimGiftCard(
    val code: String,
    val pin: String,
    val url: String
)

// MARK: - Redeem Available Gift Card Response
data class NewRedemptionResponse(
    val success: Boolean?,
    val redemptionId: String?,
    val redeemedAt: String?
)
