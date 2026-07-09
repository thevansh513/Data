package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val avatarUrl: String = "gamer", // "gamer", "champion", "master", "legend"
    val aadhaar: String = "",
    val balance: Double = 1000.0,
    val role: String = "User",
    
    // Referral System
    val referredBy: String = "",
    val referralCode: String = "",
    val referralCommissionClaimable: Double = 0.0,
    val referralCommissionClaimed: Double = 0.0,
    val hasDeposited: Boolean = false,
    
    // KYC Information
    val kycStatus: String = "Not Submitted", // "Not Submitted", "Pending", "Approved", "Rejected"
    val kycAadhaarFront: String = "", // status info or mock file name
    val kycAadhaarBack: String = "", // status info or mock file name
    val kycRejectReason: String = "",
    
    // Saved password (plain text inside their private profile node for background password update bypass)
    val password: String = ""
)

@JsonClass(generateAdapter = true)
data class Tournament(
    val id: String = "",
    val title: String = "",
    val game: String = "",
    val entryFee: Double = 0.0,
    val prizePool: Double = 0.0,
    val dateTime: String = "",
    val roomId: String = "",
    val roomPassword: String = "",
    val participants: Map<String, Boolean> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "Deposit", "Withdrawal", "Referral Claim", "Promo Claim"
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val dateTime: String = "",
    val reference: String = ""
)

@JsonClass(generateAdapter = true)
data class FirebaseAuthRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@JsonClass(generateAdapter = true)
data class FirebaseAuthResponse(
    val localId: String,
    val email: String,
    val idToken: String,
    val expiresIn: String
)

@JsonClass(generateAdapter = true)
data class FirebaseUpdateAccountRequest(
    val idToken: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@JsonClass(generateAdapter = true)
data class FirebaseUpdateAccountResponse(
    val localId: String,
    val email: String,
    val idToken: String
)

@JsonClass(generateAdapter = true)
data class EmailRequest(
    val email: String,
    val reason: String,
    val code: String,
    val platform: String = "DograPay Arena"
)

@JsonClass(generateAdapter = true)
data class EmailResponse(
    val success: Boolean,
    val message: String
)

@JsonClass(generateAdapter = true)
data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val subject: String = "",
    val message: String = "",
    val reply: String = "",
    val status: String = "Open", // "Open", "Resolved"
    val dateTime: String = ""
)

@JsonClass(generateAdapter = true)
data class PromoCode(
    val code: String = "",
    val value: Double = 0.0,
    val claimedBy: Map<String, Boolean> = emptyMap() // userId -> true
)

@JsonClass(generateAdapter = true)
data class PeriodGameResult(
    val periodId: String = "",
    val result: String = "HEAD", // "HEAD" or "TAIL"
    val dateTime: String = ""
)

@JsonClass(generateAdapter = true)
data class CreatePaymentRequest(
    val apiKey: String,
    val platformName: String,
    val transactionId: String,
    val userId: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentRequest(
    val apiKey: String,
    val platformName: String,
    val transactionId: String
)

@JsonClass(generateAdapter = true)
data class CreatePaymentResponse(
    val success: Boolean,
    val orderId: String? = null,
    val amount: Long? = null,           // paise
    val currency: String? = null,
    val transactionId: String? = null,
    val error: String? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentResponse(
    val success: Boolean,
    val status: String,          // SUCCESS, PENDING, FAILED, CANCELLED, NOT_FOUND
    val amount: Double? = null,
    val transactionId: String? = null,
    val message: String? = null,
    val alreadyCredited: Boolean? = null
)
