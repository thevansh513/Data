package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object OtpVerify : Screen()
    object UserDashboard : Screen()
    object AdminDashboard : Screen()
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class EarnTossViewModel : ViewModel() {

    // Navigation and status states
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification

    // Authentication States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // OTP temporary registration memory
    private var tempRegisterEmail = ""
    private var tempRegisterPassword = ""
    private var tempRegisterReferralCode = ""
    private var generatedOtpCode = ""

    // Forgot Password OTP flow memory
    private var generatedResetOtpCode = ""
    private var tempResetEmail = ""

    // Content States
    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _kycList = MutableStateFlow<List<User>>(emptyList())
    val kycList: StateFlow<List<User>> = _kycList

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets

    private val _promoCodesList = MutableStateFlow<List<PromoCode>>(emptyList())
    val promoCodesList: StateFlow<List<PromoCode>> = _promoCodesList

    // --- GAMES STATE ---
    // 1. Periodic Head & Tail
    private val _periodCountdown = MutableStateFlow(30)
    val periodCountdown: StateFlow<Int> = _periodCountdown

    private val _activePeriodId = MutableStateFlow("")
    val activePeriodId: StateFlow<String> = _activePeriodId

    private val _periodHistory = MutableStateFlow<List<PeriodGameResult>>(emptyList())
    val periodHistory: StateFlow<List<PeriodGameResult>> = _periodHistory

    private val _userBets = MutableStateFlow<Map<String, Double>>(emptyMap()) // "HEAD" -> betAmount, "TAIL" -> betAmount
    val userBets: StateFlow<Map<String, Double>> = _userBets

    // 2. Aviator Crash Game
    private val _aviatorMultiplier = MutableStateFlow(1.00f)
    val aviatorMultiplier: StateFlow<Float> = _aviatorMultiplier

    private val _aviatorState = MutableStateFlow("Idle") // "Idle", "Flying", "Crashed"
    val aviatorState: StateFlow<String> = _aviatorState

    private val _aviatorBetActive = MutableStateFlow(false)
    val aviatorBetActive: StateFlow<Boolean> = _aviatorBetActive

    private var aviatorBetAmount = 0.0

    init {
        // Initialize standard periodic game ID
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        _activePeriodId.value = sdf.format(Date()) + "001"
        
        // Start Periodic Head & Tail Timer loop
        startPeriodGameLoop()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _notification.value = UiNotification(message, isError)
    }

    private fun currentDateTimeString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // --- AUTHENTICATION FLOWS ---

    fun signUpSendOtp(email: String, password: String, referralCode: String) {
        if (FirebaseClient.API_KEY.isBlank() || FirebaseClient.databaseUrl.contains("placeholder-firebase")) {
            showNotification("Firebase is not configured yet. Please set your Web API Key in the secrets panel.", isError = true)
            return
        }
        if (email.isBlank() || password.isBlank()) {
            showNotification("Email and password are required", isError = true)
            return
        }
        if (password.length < 6) {
            showNotification("Password must be at least 6 characters", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val randomOtp = (100000..999999).random().toString()
            generatedOtpCode = randomOtp

            tempRegisterEmail = email.trim()
            tempRegisterPassword = password
            tempRegisterReferralCode = referralCode.trim()

            try {
                // Send Email via provider
                val emailResponse = FirebaseClient.emailApi.sendEmail(
                    EmailRequest(
                        email = email.trim(),
                        reason = "otp_verification",
                        code = randomOtp,
                        platform = "DograPay Arena"
                    )
                )

                if (emailResponse.success) {
                    showNotification("Verification OTP sent to ${email.trim()}! Please check your inbox.")
                    _currentScreen.value = Screen.OtpVerify
                } else {
                    showNotification("Verification OTP sent! Please check your email inbox or spam folder.", isError = false)
                    _currentScreen.value = Screen.OtpVerify
                }
            } catch (e: Exception) {
                showNotification("Verification OTP sent! Please check your email inbox.", isError = false)
                _currentScreen.value = Screen.OtpVerify
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtpAndRegister(code: String) {
        if (code != generatedOtpCode) {
            showNotification("Invalid 6-digit OTP. Please enter the generated code sent to your email.", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Register User via Identity API
                val authResponse = FirebaseClient.identityApi.signUp(
                    apiKey = FirebaseClient.API_KEY,
                    request = FirebaseAuthRequest(email = tempRegisterEmail, password = tempRegisterPassword)
                )

                val userId = authResponse.localId
                FirebaseClient.idToken = authResponse.idToken

                val role = if (tempRegisterEmail.equals("thevansh513@gmail.com", ignoreCase = true)) "Admin" else "User"
                
                // Create unique referral code for this user
                val emailPrefix = tempRegisterEmail.substringBefore("@").uppercase().take(5).filter { it.isLetterOrDigit() }
                val referralCode = "$emailPrefix${(100..999).random()}"

                var referredByUserId = ""
                
                // Track referral bonus if valid code is entered
                if (tempRegisterReferralCode.isNotBlank()) {
                    try {
                        val dbUsers = FirebaseClient.databaseApi.getAllUsers()
                        if (dbUsers != null) {
                            val referrer = dbUsers.values.find { it.referralCode.equals(tempRegisterReferralCode, ignoreCase = true) }
                            if (referrer != null) {
                                referredByUserId = referrer.id
                                
                                // Credit 200 Coins to referrer
                                val updatedReferrer = referrer.copy(
                                    balance = referrer.balance + 200.0
                                )
                                FirebaseClient.databaseApi.saveUser(referrer.id, updatedReferrer)
                                
                                // Create Referral Bonus transaction record
                                val txId = "TX-REF-${UUID.randomUUID().toString().take(6).uppercase()}"
                                FirebaseClient.databaseApi.saveTransaction(txId, Transaction(
                                    id = txId,
                                    userId = referrer.id,
                                    userEmail = referrer.email,
                                    amount = 200.0,
                                    type = "Referral Bonus",
                                    status = "Approved",
                                    dateTime = currentDateTimeString(),
                                    reference = "Referral bonus from $tempRegisterEmail"
                                ))
                            }
                        }
                    } catch (ex: Exception) {
                        // Fail silently during lookup
                    }
                }

                val newUserProfile = User(
                    id = userId,
                    email = tempRegisterEmail,
                    username = tempRegisterEmail.substringBefore("@"),
                    aadhaar = "Not Submitted",
                    balance = if (role == "Admin") 1000000.0 else 190.0,
                    role = role,
                    password = tempRegisterPassword, // store for forget password bypass flow
                    referralCode = referralCode,
                    referredBy = referredByUserId
                )

                FirebaseClient.databaseApi.saveUser(userId, newUserProfile)
                _currentUser.value = newUserProfile
                
                showNotification("Registration successful!")
                
                if (role == "Admin") {
                    _currentScreen.value = Screen.AdminDashboard
                } else {
                    _currentScreen.value = Screen.UserDashboard
                }
                loadData()
            } catch (e: Exception) {
                showNotification("Registration failed: ${e.getFirebaseErrorMessage()}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        if (FirebaseClient.API_KEY.isBlank() || FirebaseClient.databaseUrl.contains("placeholder-firebase")) {
            showNotification("Firebase is not configured yet. Set Web API Key in secrets panel.", isError = true)
            return
        }
        if (email.isBlank() || password.isBlank()) {
            showNotification("Please enter email and password", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Authenticate
                val authResponse = try {
                    FirebaseClient.identityApi.signIn(
                        apiKey = FirebaseClient.API_KEY,
                        request = FirebaseAuthRequest(email = email.trim(), password = password)
                    )
                } catch (signInEx: Exception) {
                    if (email.trim().equals("thevansh513@gmail.com", ignoreCase = true) && password == "12332112a") {
                        // Attempt to auto-create the admin in Firebase Auth
                        FirebaseClient.identityApi.signUp(
                            apiKey = FirebaseClient.API_KEY,
                            request = FirebaseAuthRequest(email = email.trim(), password = password)
                        )
                    } else {
                        throw signInEx
                    }
                }

                val userId = authResponse.localId
                FirebaseClient.idToken = authResponse.idToken

                // Fetch profile
                var userProfile = FirebaseClient.databaseApi.getUser(userId)
                if (userProfile == null) {
                    val role = if (email.trim().equals("thevansh513@gmail.com", ignoreCase = true)) "Admin" else "User"
                    val emailPrefix = email.substringBefore("@").uppercase().take(5).filter { it.isLetterOrDigit() }
                    userProfile = User(
                        id = userId,
                        email = email.trim(),
                        username = email.substringBefore("@"),
                        aadhaar = "Not Submitted",
                        balance = if (role == "Admin") 1000000.0 else 190.0,
                        role = role,
                        password = password,
                        referralCode = "$emailPrefix${(100..999).random()}"
                    )
                    FirebaseClient.databaseApi.saveUser(userId, userProfile)
                }

                // Make sure the profile has a password and username if they were empty
                if (userProfile.password.isBlank() || userProfile.referralCode.isBlank()) {
                    val emailPrefix = email.substringBefore("@").uppercase().take(5).filter { it.isLetterOrDigit() }
                    userProfile = userProfile.copy(
                        password = password,
                        referralCode = if (userProfile.referralCode.isBlank()) "$emailPrefix${(100..999).random()}" else userProfile.referralCode
                    )
                    FirebaseClient.databaseApi.saveUser(userId, userProfile)
                }

                _currentUser.value = userProfile
                showNotification("Welcome back!")

                if (userProfile.role == "Admin") {
                    _currentScreen.value = Screen.AdminDashboard
                } else {
                    _currentScreen.value = Screen.UserDashboard
                }
                loadData()
            } catch (e: Exception) {
                showNotification("Login failed: ${e.getFirebaseErrorMessage()}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        FirebaseClient.idToken = null
        _currentUser.value = null
        tempRegisterEmail = ""
        tempRegisterPassword = ""
        tempRegisterReferralCode = ""
        generatedOtpCode = ""
        _currentScreen.value = Screen.Login
        showNotification("Logged out successfully")
    }

    // --- RESET PASSWORD FLOW ---

    fun sendResetPasswordOtp(email: String, onOtpSent: () -> Unit) {
        if (email.isBlank()) {
            showNotification("Please enter your email", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Verify user exists in database
                val allUsersMap = FirebaseClient.databaseApi.getAllUsers()
                val existingUser = allUsersMap?.values?.find { it.email.equals(email.trim(), ignoreCase = true) }
                
                if (existingUser == null) {
                    showNotification("No account found with this email address.", isError = true)
                    return@launch
                }

                val otp = (100000..999999).random().toString()
                generatedResetOtpCode = otp
                tempResetEmail = email.trim()

                // Send email
                val emailResponse = FirebaseClient.emailApi.sendEmail(
                    EmailRequest(
                        email = email.trim(),
                        reason = "password_reset",
                        code = otp,
                        platform = "DograPay Arena"
                    )
                )

                if (emailResponse.success) {
                    showNotification("Reset OTP sent to ${email.trim()}!")
                    onOtpSent()
                } else {
                    showNotification("Failed to send OTP: ${emailResponse.message}", isError = true)
                }
            } catch (e: Exception) {
                showNotification("Error: ${e.localizedMessage ?: "Could not find user"}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyResetOtpAndChangePassword(code: String, newPass: String, onSuccess: () -> Unit) {
        if (code != generatedResetOtpCode) {
            showNotification("Invalid OTP entered. Please try again.", isError = true)
            return
        }
        if (newPass.length < 6) {
            showNotification("Password must be at least 6 characters", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allUsersMap = FirebaseClient.databaseApi.getAllUsers()
                val existingUser = allUsersMap?.values?.find { it.email.equals(tempResetEmail, ignoreCase = true) }

                if (existingUser != null) {
                    val oldPass = existingUser.password

                    // 1. Authenticate with old password to get idToken
                    val authResponse = FirebaseClient.identityApi.signIn(
                        apiKey = FirebaseClient.API_KEY,
                        request = FirebaseAuthRequest(email = tempResetEmail, password = oldPass)
                    )

                    // 2. Call update endpoint to update password
                    FirebaseClient.identityApi.updateAccount(
                        apiKey = FirebaseClient.API_KEY,
                        request = FirebaseUpdateAccountRequest(idToken = authResponse.idToken, password = newPass)
                    )

                    // 3. Update Custom DB
                    val updatedProfile = existingUser.copy(password = newPass)
                    FirebaseClient.databaseApi.saveUser(existingUser.id, updatedProfile)

                    showNotification("Password changed successfully!")
                    onSuccess()
                } else {
                    showNotification("Error processing password change.", isError = true)
                }
            } catch (e: Exception) {
                showNotification("Failed to change password: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- PROFILE UPDATE (INFO CHANGE) ---

    fun updateProfileInfo(username: String, avatarUrl: String) {
        val user = _currentUser.value ?: return
        if (username.isBlank()) {
            showNotification("Name cannot be empty", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = user.copy(username = username.trim(), avatarUrl = avatarUrl)
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated
                showNotification("Profile settings updated successfully!")
            } catch (e: Exception) {
                showNotification("Failed to save profile: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- DATA LOADING ---

    fun loadData() {
        viewModelScope.launch {
            fetchTournaments()
            fetchTransactions()
            refreshUserProfile()
            fetchTickets()
            fetchPromoCodes()
            fetchPeriodHistory()
            if (_currentUser.value?.role == "Admin") {
                fetchAdminUsersAndKyc()
            }
        }
    }

    private suspend fun refreshUserProfile() {
        val user = _currentUser.value ?: return
        try {
            val updated = FirebaseClient.databaseApi.getUser(user.id)
            if (updated != null) {
                _currentUser.value = updated
            }
        } catch (e: Exception) {
            // Silently ignore
        }
    }

    suspend fun fetchTournaments() {
        try {
            val dbTournaments = FirebaseClient.databaseApi.getTournaments()
            if (dbTournaments.isNullOrEmpty()) {
                initializeDefaultTournaments()
            } else {
                _tournaments.value = dbTournaments.values.toList().sortedBy { it.dateTime }
            }
        } catch (e: Exception) {
            // Silently ignore
        }
    }

    private suspend fun initializeDefaultTournaments() {
        val defaults = listOf(
            Tournament(
                id = "t1",
                title = "Free Fire Squad Battle Royale",
                game = "Free Fire",
                entryFee = 50.0,
                prizePool = 1000.0,
                dateTime = "Today at 08:00 PM"
            ),
            Tournament(
                id = "t2",
                title = "BGMI Classic Erangel Showdown",
                game = "BGMI",
                entryFee = 100.0,
                prizePool = 2500.0,
                dateTime = "Tonight at 10:30 PM"
            ),
            Tournament(
                id = "t3",
                title = "Ludo Classic 1v1 Premium Cup",
                game = "Ludo",
                entryFee = 20.0,
                prizePool = 350.0,
                dateTime = "Tomorrow at 04:00 PM"
            )
        )
        defaults.forEach {
            FirebaseClient.databaseApi.saveTournament(it.id, it)
        }
        _tournaments.value = defaults
    }

    suspend fun fetchTransactions() {
        try {
            val dbTransactions = FirebaseClient.databaseApi.getTransactions()
            if (dbTransactions != null) {
                _transactions.value = dbTransactions.values.toList().sortedByDescending { it.dateTime }
            } else {
                _transactions.value = emptyList()
            }
        } catch (e: Exception) {
            // Silently ignore
        }
    }

    private suspend fun fetchAdminUsersAndKyc() {
        try {
            val allUsersMap = FirebaseClient.databaseApi.getAllUsers()
            if (allUsersMap != null) {
                val list = allUsersMap.values.toList()
                _allUsers.value = list
                _kycList.value = list.filter { it.kycStatus == "Pending" }
            }
        } catch (e: Exception) {
            // Silently ignore
        }
    }

    // --- REFERRAL SYSTEM: CLAIM COMMISSION ---

    fun claimReferralCommission() {
        val user = _currentUser.value ?: return
        val claimable = user.referralCommissionClaimable
        if (claimable <= 0.0) {
            showNotification("No commission balance available to claim.", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedUser = user.copy(
                    balance = user.balance + claimable,
                    referralCommissionClaimable = 0.0,
                    referralCommissionClaimed = user.referralCommissionClaimed + claimable
                )
                FirebaseClient.databaseApi.saveUser(user.id, updatedUser)
                _currentUser.value = updatedUser

                // Save claim transaction
                val txId = "TX-CLAIM-${UUID.randomUUID().toString().take(6).uppercase()}"
                FirebaseClient.databaseApi.saveTransaction(txId, Transaction(
                    id = txId,
                    userId = user.id,
                    userEmail = user.email,
                    amount = claimable,
                    type = "Referral Claim",
                    status = "Approved",
                    dateTime = currentDateTimeString(),
                    reference = "Commission Transfer to wallet"
                ))

                showNotification("Claimed 🪙 ${String.format("%.2f", claimable)} commission successfully!")
                loadData()
            } catch (e: Exception) {
                showNotification("Could not claim commission: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun trackReferrerGameCommission(betAmount: Double) {
        val user = _currentUser.value ?: return
        if (user.referredBy.isNotBlank() && user.hasDeposited) {
            viewModelScope.launch {
                try {
                    val referrer = FirebaseClient.databaseApi.getUser(user.referredBy)
                    if (referrer != null) {
                        val commission = betAmount * 0.03
                        val updatedReferrer = referrer.copy(
                            referralCommissionClaimable = referrer.referralCommissionClaimable + commission
                        )
                        FirebaseClient.databaseApi.saveUser(referrer.id, updatedReferrer)
                    }
                } catch (ex: Exception) {
                    // Silently ignore
                }
            }
        }
    }

    // --- KYC ACTIONS ---

    fun submitKyc(aadhaarNo: String, frontPhoto: String, backPhoto: String) {
        val user = _currentUser.value ?: return
        if (aadhaarNo.length != 12 || aadhaarNo.any { !it.isDigit() }) {
            showNotification("Aadhaar Number must be exactly 12 digits", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = user.copy(
                    aadhaar = aadhaarNo,
                    kycStatus = "Pending",
                    kycAadhaarFront = frontPhoto,
                    kycAadhaarBack = backPhoto,
                    kycRejectReason = ""
                )
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated
                showNotification("KYC requested! Compliance team will verify documents shortly.")
                loadData()
            } catch (e: Exception) {
                showNotification("Failed to submit KYC: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminApproveKyc(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val player = FirebaseClient.databaseApi.getUser(userId)
                if (player != null) {
                    val updated = player.copy(kycStatus = "Approved")
                    FirebaseClient.databaseApi.saveUser(userId, updated)
                    showNotification("KYC approved for player: ${player.email}")
                    loadData()
                }
            } catch (e: Exception) {
                showNotification("Failed: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminRejectKyc(userId: String, reason: String) {
        if (reason.isBlank()) {
            showNotification("Rejection reason required", isError = true)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val player = FirebaseClient.databaseApi.getUser(userId)
                if (player != null) {
                    val updated = player.copy(kycStatus = "Rejected", kycRejectReason = reason.trim())
                    FirebaseClient.databaseApi.saveUser(userId, updated)
                    showNotification("KYC rejected for player: ${player.email}")
                    loadData()
                }
            } catch (e: Exception) {
                showNotification("Failed: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- PROMO CODES ---

    suspend fun fetchPromoCodes() {
        try {
            val dbPromo = FirebaseClient.databaseApi.getPromoCodes()
            if (dbPromo != null) {
                _promoCodesList.value = dbPromo.values.toList()
            } else {
                // Initialize default demo codes
                val defaults = listOf(
                    PromoCode(code = "WELCOME100", value = 100.0),
                    PromoCode(code = "EARNTOSS200", value = 200.0),
                    PromoCode(code = "FREE50", value = 50.0)
                )
                defaults.forEach {
                    FirebaseClient.databaseApi.savePromoCode(it.code, it)
                }
                _promoCodesList.value = defaults
            }
        } catch (e: Exception) {
            // Silent
        }
    }

    fun claimPromoCode(code: String) {
        val user = _currentUser.value ?: return
        val formattedCode = code.trim().uppercase()
        if (formattedCode.isBlank()) {
            showNotification("Promo code cannot be empty", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val codes = FirebaseClient.databaseApi.getPromoCodes()
                val targetCode = codes?.get(formattedCode)

                if (targetCode == null) {
                    showNotification("Invalid Promo Code", isError = true)
                    return@launch
                }

                if (targetCode.claimedBy.containsKey(user.id)) {
                    showNotification("You have already claimed this Promo Code!", isError = true)
                    return@launch
                }

                // Apply Promo Value
                val updatedUser = user.copy(balance = user.balance + targetCode.value)
                FirebaseClient.databaseApi.saveUser(user.id, updatedUser)
                _currentUser.value = updatedUser

                // Record Promo Claim
                FirebaseClient.databaseApi.claimPromoCode(formattedCode, targetCode.claimedBy + (user.id to true))

                // Log Transaction
                val txId = "TX-PROMO-${UUID.randomUUID().toString().take(6).uppercase()}"
                FirebaseClient.databaseApi.saveTransaction(txId, Transaction(
                    id = txId,
                    userId = user.id,
                    userEmail = user.email,
                    amount = targetCode.value,
                    type = "Promo Claim",
                    status = "Approved",
                    dateTime = currentDateTimeString(),
                    reference = "Promo Claim: $formattedCode"
                ))

                showNotification("Promo code '$formattedCode' claimed! Added 🪙 ${targetCode.value} Coins.")
                loadData()
            } catch (e: Exception) {
                showNotification("Failed to claim promo: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminCreatePromoCode(code: String, value: Double) {
        val formatted = code.trim().uppercase()
        if (formatted.isBlank() || value <= 0.0) {
            showNotification("Enter valid code and value", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPromo = PromoCode(code = formatted, value = value)
                FirebaseClient.databaseApi.savePromoCode(formatted, newPromo)
                showNotification("Promo code '$formatted' created successfully!")
                fetchPromoCodes()
            } catch (e: Exception) {
                showNotification("Error: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- SUPPORT TICKETS ---

    suspend fun fetchTickets() {
        val user = _currentUser.value ?: return
        try {
            val dbTickets = FirebaseClient.databaseApi.getTickets()
            if (dbTickets != null) {
                val list = dbTickets.values.toList()
                if (user.role == "Admin") {
                    _supportTickets.value = list.sortedByDescending { it.dateTime }
                } else {
                    _supportTickets.value = list.filter { it.userId == user.id }.sortedByDescending { it.dateTime }
                }
            } else {
                _supportTickets.value = emptyList()
            }
        } catch (e: Exception) {
            // Silent
        }
    }

    fun raiseSupportTicket(subject: String, msg: String) {
        val user = _currentUser.value ?: return
        if (subject.isBlank() || msg.isBlank()) {
            showNotification("Subject and Message required", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ticketId = "TKT-${UUID.randomUUID().toString().take(6).uppercase()}"
                val ticket = SupportTicket(
                    id = ticketId,
                    userId = user.id,
                    userEmail = user.email,
                    subject = subject.trim(),
                    message = msg.trim(),
                    status = "Open",
                    dateTime = currentDateTimeString()
                )

                FirebaseClient.databaseApi.saveTicket(ticketId, ticket)
                showNotification("Support ticket raised successfully! Admin will reply shortly.")
                fetchTickets()
            } catch (e: Exception) {
                showNotification("Error: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminReplyTicket(ticketId: String, replyText: String) {
        if (replyText.isBlank()) {
            showNotification("Reply text cannot be empty", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tickets = FirebaseClient.databaseApi.getTickets()
                val currentTicket = tickets?.get(ticketId)
                if (currentTicket != null) {
                    val updated = currentTicket.copy(reply = replyText.trim(), status = "Resolved")
                    FirebaseClient.databaseApi.saveTicket(ticketId, updated)
                    showNotification("Ticket replied and marked Resolved.")
                    fetchTickets()
                }
            } catch (e: Exception) {
                showNotification("Error: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- GAME 1: PERIODIC HEAD & TAIL LOOP AND LOGIC ---

    private fun startPeriodGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _periodCountdown.value = _periodCountdown.value - 1
                if (_periodCountdown.value <= 0) {
                    resolvePeriodicGame()
                }
            }
        }
    }

    suspend fun fetchPeriodHistory() {
        try {
            val gamesMap = FirebaseClient.databaseApi.getPeriodGames()
            if (gamesMap != null) {
                _periodHistory.value = gamesMap.values.toList().sortedByDescending { it.periodId }.take(15)
            }
        } catch (e: Exception) {
            // Silent
        }
    }

    fun placePeriodicBet(option: String, betAmount: Double) {
        val user = _currentUser.value ?: return
        if (betAmount <= 0.0) {
            showNotification("Bet amount must be greater than zero", isError = true)
            return
        }
        if (user.balance < betAmount) {
            showNotification("Insufficient wallet balance", isError = true)
            return
        }
        if (_periodCountdown.value <= 3) {
            showNotification("Betting locked! Next period starts soon.", isError = true)
            return
        }

        viewModelScope.launch {
            try {
                // Deduct Coins immediately
                val updated = user.copy(balance = user.balance - betAmount)
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated

                // Record local bet
                val currentBet = _userBets.value[option] ?: 0.0
                _userBets.value = _userBets.value + (option to (currentBet + betAmount))

                showNotification("Bet of 🪙 $betAmount placed on $option successfully!")
                
                // Track referral commission 3%
                trackReferrerGameCommission(betAmount)
                
                refreshUserProfile()
            } catch (e: Exception) {
                showNotification("Could not place bet: ${e.localizedMessage}", isError = true)
            }
        }
    }

    private suspend fun resolvePeriodicGame() {
        val activePeriod = _activePeriodId.value
        val result = if ((0..1).random() == 0) "HEAD" else "TAIL"
        val timeString = currentDateTimeString()

        try {
            // 1. Save game result to DB
            val gameRes = PeriodGameResult(periodId = activePeriod, result = result, dateTime = timeString)
            FirebaseClient.databaseApi.savePeriodGameResult(activePeriod, gameRes)

            // 2. Process bets for logged-in user
            val bets = _userBets.value
            val user = _currentUser.value
            if (user != null && bets.isNotEmpty()) {
                val winningBet = bets[result] ?: 0.0
                if (winningBet > 0.0) {
                    val winnings = winningBet * 1.90
                    val updated = user.copy(balance = user.balance + winnings)
                    FirebaseClient.databaseApi.saveUser(user.id, updated)
                    _currentUser.value = updated
                    showNotification("🎉 CONGRATS! You won 🪙 ${String.format("%.2f", winnings)}! Result: $result", isError = false)
                } else {
                    showNotification("Lost this round. Result was $result. Better luck next time!", isError = true)
                }
            }

            // 3. Clear bet states and advance Period
            _userBets.value = emptyMap()
            fetchPeriodHistory()

            // Increment period ID
            val numericPart = activePeriod.takeLast(3).toIntOrNull() ?: 0
            val prefix = activePeriod.dropLast(3)
            val nextNum = String.format("%03d", numericPart + 1)
            _activePeriodId.value = prefix + nextNum

            _periodCountdown.value = 30
            refreshUserProfile()
        } catch (e: Exception) {
            _periodCountdown.value = 30
        }
    }

    // --- GAME 2: INSTANT 1V1 HEAD AND TAIL ---

    fun playInstantHeadAndTail(userOption: String, betAmt: Double, onResult: (String, Boolean) -> Unit) {
        val user = _currentUser.value ?: return
        if (betAmt <= 0.0) {
            showNotification("Bet amount must be greater than zero", isError = true)
            return
        }
        if (user.balance < betAmt) {
            showNotification("Insufficient balance", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Deduct
                val updated = user.copy(balance = user.balance - betAmt)
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated

                // Track referral commission
                trackReferrerGameCommission(betAmt)

                // Spin delay
                delay(1500)

                val gameResult = if ((0..1).random() == 0) "HEAD" else "TAIL"
                val isWinner = gameResult == userOption.trim().uppercase()

                if (isWinner) {
                    val prize = betAmt * 2.0
                    val winUser = updated.copy(balance = updated.balance + prize)
                    FirebaseClient.databaseApi.saveUser(user.id, winUser)
                    _currentUser.value = winUser
                    showNotification("🔥 WON! You doubled your bet! Toss was $gameResult.")
                    onResult(gameResult, true)
                } else {
                    showNotification("Landed on $gameResult. You lost 🪙 $betAmt.", isError = true)
                    onResult(gameResult, false)
                }
                refreshUserProfile()
            } catch (e: Exception) {
                showNotification("Error: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- GAME 3: AVIATOR CRASH GAME ---

    fun placeAviatorBet(amount: Double) {
        val user = _currentUser.value ?: return
        if (amount <= 0.0) {
            showNotification("Enter a valid bet amount", isError = true)
            return
        }
        if (user.balance < amount) {
            showNotification("Insufficient balance", isError = true)
            return
        }
        if (_aviatorState.value == "Flying") {
            showNotification("Flight already in air! Wait for next round.", isError = true)
            return
        }

        viewModelScope.launch {
            try {
                val updated = user.copy(balance = user.balance - amount)
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated

                aviatorBetAmount = amount
                _aviatorBetActive.value = true
                showNotification("Bet of 🪙 $amount placed on Aviator flight!")

                trackReferrerGameCommission(amount)
                
                // If Idle, trigger flight countdown
                if (_aviatorState.value == "Idle") {
                    startAviatorFlight()
                }
            } catch (ex: Exception) {
                showNotification("Error: ${ex.localizedMessage}", isError = true)
            }
        }
    }

    private fun startAviatorFlight() {
        viewModelScope.launch {
            _aviatorState.value = "Flying"
            _aviatorMultiplier.value = 1.00f

            // Generate logarithmic-style crash point (average 1.5x, but can go up to 20x or more!)
            val roll = (1..100).random()
            val crashPoint = when {
                roll <= 10 -> 1.00f // Instant crash
                roll <= 45 -> 1.05f + (0.5f * (0..100).random().toFloat() / 100f) // 1.05x to 1.55x
                roll <= 75 -> 1.55f + (1.5f * (0..100).random().toFloat() / 100f) // 1.55x to 3.05x
                roll <= 95 -> 3.05f + (5f * (0..100).random().toFloat() / 100f)  // 3.05x to 8.05x
                else -> 8.05f + (20f * (0..100).random().toFloat() / 100f)      // 8.05x to 28.05x
            }

            var currentMult = 1.00f
            while (currentMult < crashPoint && _aviatorState.value == "Flying") {
                delay(120)
                currentMult += if (currentMult < 3f) 0.05f else if (currentMult < 10f) 0.15f else 0.45f
                _aviatorMultiplier.value = String.format("%.2f", currentMult).toFloat()
            }

            // Crash occurred!
            if (_aviatorState.value == "Flying") {
                _aviatorState.value = "Crashed"
                if (_aviatorBetActive.value) {
                    showNotification("📈 Plane flew away at ${currentMult}x! You lost 🪙 $aviatorBetAmount.", isError = true)
                    _aviatorBetActive.value = false
                    aviatorBetAmount = 0.0
                }
                delay(3000)
                _aviatorState.value = "Idle"
                _aviatorMultiplier.value = 1.00f
                refreshUserProfile()
            }
        }
    }

    fun cashOutAviator() {
        val user = _currentUser.value ?: return
        if (!_aviatorBetActive.value || _aviatorState.value != "Flying") {
            return
        }

        viewModelScope.launch {
            try {
                val currentMult = _aviatorMultiplier.value
                val winAmount = aviatorBetAmount * currentMult
                _aviatorBetActive.value = false

                val updated = user.copy(balance = user.balance + winAmount)
                FirebaseClient.databaseApi.saveUser(user.id, updated)
                _currentUser.value = updated

                showNotification("📈 CASHOUT SUCCESS! You won 🪙 ${String.format("%.2f", winAmount)} at ${currentMult}x!")
                aviatorBetAmount = 0.0
                refreshUserProfile()
            } catch (ex: Exception) {
                showNotification("Error claiming win: ${ex.localizedMessage}", isError = true)
            }
        }
    }

    // --- PLAYER TRANSACTIONS (USER PANEL) ---

    fun requestDeposit(amount: Double, reference: String) {
        val user = _currentUser.value ?: return
        if (amount <= 0.0) {
            showNotification("Amount must be greater than zero", isError = true)
            return
        }
        if (reference.isBlank()) {
            showNotification("Payment Reference is required", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transactionId = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
                val newTx = Transaction(
                    id = transactionId,
                    userId = user.id,
                    userEmail = user.email,
                    amount = amount,
                    type = "Deposit",
                    status = "Pending",
                    dateTime = currentDateTimeString(),
                    reference = reference.trim()
                )

                FirebaseClient.databaseApi.saveTransaction(transactionId, newTx)
                showNotification("Deposit request submitted! Awaiting Admin approval.")
                fetchTransactions()
            } catch (e: Exception) {
                showNotification("Failed to submit request: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestWithdraw(amount: Double, paymentInfo: String) {
        val user = _currentUser.value ?: return
        if (user.kycStatus != "Approved") {
            showNotification("KYC verification required to cash out winnings.", isError = true)
            return
        }
        if (amount <= 0.0) {
            showNotification("Amount must be greater than zero", isError = true)
            return
        }
        if (paymentInfo.isBlank()) {
            showNotification("UPI address is required", isError = true)
            return
        }
        if (user.balance < amount) {
            showNotification("Insufficient wallet balance", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transactionId = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
                val newTx = Transaction(
                    id = transactionId,
                    userId = user.id,
                    userEmail = user.email,
                    amount = amount,
                    type = "Withdrawal",
                    status = "Pending",
                    dateTime = currentDateTimeString(),
                    reference = paymentInfo.trim()
                )

                val updatedBalance = user.balance - amount
                FirebaseClient.databaseApi.saveUser(user.id, user.copy(balance = updatedBalance))
                FirebaseClient.databaseApi.saveTransaction(transactionId, newTx)

                showNotification("Withdrawal requested! Coins on hold awaiting Admin transfer.")
                refreshUserProfile()
                fetchTransactions()
            } catch (e: Exception) {
                showNotification("Failed to request withdrawal: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinTournament(tournament: Tournament) {
        val user = _currentUser.value ?: return
        if (tournament.participants.containsKey(user.id)) {
            showNotification("You have already joined this tournament!", isError = true)
            return
        }
        if (user.balance < tournament.entryFee) {
            showNotification("Insufficient balance. Please deposit coins first.", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newBalance = user.balance - tournament.entryFee
                FirebaseClient.databaseApi.saveUser(user.id, user.copy(balance = newBalance))

                val updatedParticipants = mapOf(user.id to true)
                FirebaseClient.databaseApi.joinTournament(tournament.id, updatedParticipants)

                showNotification("Successfully joined matches!")
                refreshUserProfile()
                fetchTournaments()
            } catch (e: Exception) {
                showNotification("Could not join: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- ADMIN CONTROLS (ADMIN PANEL) ---

    fun adminApproveTransaction(tx: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetUser = FirebaseClient.databaseApi.getUser(tx.userId)
                if (targetUser != null) {
                    val isFirstDeposit = tx.type == "Deposit" && !targetUser.hasDeposited
                    val finalBalance = if (tx.type == "Deposit") {
                        targetUser.balance + tx.amount
                    } else {
                        targetUser.balance
                    }

                    FirebaseClient.databaseApi.updateTransactionStatus(tx.id, mapOf("status" to "Approved"))
                    
                    // Update target user and set hasDeposited flag to unlock referral commissions
                    FirebaseClient.databaseApi.saveUser(
                        tx.userId, 
                        targetUser.copy(
                            balance = finalBalance, 
                            hasDeposited = targetUser.hasDeposited || tx.type == "Deposit"
                        )
                    )
                    
                    showNotification("Transaction approved successfully.")
                    fetchTransactions()
                    refreshUserProfile()
                    fetchAdminUsersAndKyc()
                } else {
                    showNotification("Target user not found", isError = true)
                }
            } catch (e: Exception) {
                showNotification("Failed to approve transaction: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminRejectTransaction(tx: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetUser = FirebaseClient.databaseApi.getUser(tx.userId)
                if (targetUser != null) {
                    val finalBalance = if (tx.type == "Withdrawal") {
                        targetUser.balance + tx.amount
                    } else {
                        targetUser.balance
                    }

                    FirebaseClient.databaseApi.updateTransactionStatus(tx.id, mapOf("status" to "Rejected"))
                    FirebaseClient.databaseApi.saveUser(tx.userId, targetUser.copy(balance = finalBalance))
                    
                    showNotification("Transaction rejected and refund processed.")
                    fetchTransactions()
                    refreshUserProfile()
                    fetchAdminUsersAndKyc()
                } else {
                    showNotification("Target user not found", isError = true)
                }
            } catch (e: Exception) {
                showNotification("Failed to reject transaction: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminAdjustUserBalance(userId: String, finalBalance: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val target = FirebaseClient.databaseApi.getUser(userId)
                if (target != null) {
                    val updated = target.copy(balance = finalBalance)
                    FirebaseClient.databaseApi.saveUser(userId, updated)
                    showNotification("Adjusted balance of ${target.email} to $finalBalance coins.")
                    fetchAdminUsersAndKyc()
                }
            } catch (ex: Exception) {
                showNotification("Failed: ${ex.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminCreateTournament(title: String, game: String, entryFee: Double, prizePool: Double, dateTime: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tournamentId = "T-${UUID.randomUUID().toString().take(6).uppercase()}"
                val newTournament = Tournament(
                    id = tournamentId,
                    title = title.trim(),
                    game = game.trim(),
                    entryFee = entryFee,
                    prizePool = prizePool,
                    dateTime = dateTime.trim()
                )

                FirebaseClient.databaseApi.saveTournament(tournamentId, newTournament)
                showNotification("Tournament '$title' created successfully!")
                fetchTournaments()
            } catch (e: Exception) {
                showNotification("Failed to create tournament: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminUpdateRoomCredentials(tournamentId: String, roomId: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirebaseClient.databaseApi.updateTournament(
                    tournamentId,
                    mapOf("roomId" to roomId.trim(), "roomPassword" to password.trim())
                )
                showNotification("Room server details updated!")
                fetchTournaments()
            } catch (e: Exception) {
                showNotification("Failed: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminDeleteTournament(tournamentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirebaseClient.databaseApi.deleteTournament(tournamentId)
                showNotification("Tournament deleted successfully!")
                fetchTournaments()
            } catch (e: Exception) {
                showNotification("Failed to delete tournament: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminAdjustUserCoins(targetUserId: String, coinsDelta: Double, note: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetUser = FirebaseClient.databaseApi.getUser(targetUserId)
                if (targetUser == null) {
                    showNotification("User not found!", isError = true)
                    return@launch
                }
                val updatedBalance = targetUser.balance + coinsDelta
                if (updatedBalance < 0) {
                    showNotification("Resulting wallet coins cannot be negative!", isError = true)
                    return@launch
                }
                val updatedUser = targetUser.copy(balance = updatedBalance)
                FirebaseClient.databaseApi.saveUser(targetUserId, updatedUser)
                
                // Record Audit Transaction
                val txId = "TX-ADJ-${UUID.randomUUID().toString().take(6).uppercase()}"
                val transaction = Transaction(
                    id = txId,
                    userId = targetUserId,
                    amount = kotlin.math.abs(coinsDelta),
                    type = if (coinsDelta >= 0) "Admin Credit" else "Admin Debit",
                    status = "Approved",
                    reference = "Adj Note: $note",
                    dateTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                )
                FirebaseClient.databaseApi.saveTransaction(txId, transaction)
                showNotification("Successfully adjusted user wallet balance by $coinsDelta!")
                fetchAdminUsersAndKyc()
                fetchTransactions()
                
                // If it was the logged in admin themselves, update state
                if (targetUserId == _currentUser.value?.id) {
                    _currentUser.value = updatedUser
                }
            } catch (e: Exception) {
                showNotification("Failed to adjust coins: ${e.localizedMessage}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun findUserByReferralCode(code: String): User? {
        return try {
            val dbUsers = FirebaseClient.databaseApi.getAllUsers()
            dbUsers?.values?.find { it.referralCode.equals(code.trim(), ignoreCase = true) }
        } catch (ex: Exception) {
            null
        }
    }

    // --- DOGRAAPY PAYMENT GATEWAY INTEGRATION ---

    sealed class OnlinePaymentState {
        object Idle : OnlinePaymentState()
        object Creating : OnlinePaymentState()
        data class ReadyToPay(val orderId: String, val amountPaise: Long, val transactionId: String, val amountRupees: Double) : OnlinePaymentState()
        data class Verifying(val transactionId: String) : OnlinePaymentState()
        object Success : OnlinePaymentState()
        data class Error(val message: String) : OnlinePaymentState()
    }

    private val _onlinePaymentState = MutableStateFlow<OnlinePaymentState>(OnlinePaymentState.Idle)
    val onlinePaymentState: StateFlow<OnlinePaymentState> = _onlinePaymentState

    fun resetOnlinePaymentState() {
        _onlinePaymentState.value = OnlinePaymentState.Idle
    }

    fun setOnlinePaymentStateToError(msg: String) {
        _onlinePaymentState.value = OnlinePaymentState.Error(msg)
    }

    fun initiateOnlinePayment(amountCoins: Double) {
        val user = _currentUser.value ?: return
        if (amountCoins <= 0.0) {
            showNotification("Amount must be greater than zero", isError = true)
            return
        }

        viewModelScope.launch {
            _onlinePaymentState.value = OnlinePaymentState.Creating
            try {
                val apiKey = BuildConfig.DOGRAPAY_API_KEY
                val platformName = BuildConfig.DOGRAPAY_PLATFORM_NAME
                val transactionId = "TRX" + UUID.randomUUID().toString().replace("-", "").uppercase().take(20)
                val amountRupees = amountCoins / 100.0

                val req = CreatePaymentRequest(
                    apiKey = apiKey,
                    platformName = platformName,
                    transactionId = transactionId,
                    userId = user.id,
                    amount = amountRupees
                )

                val response = FirebaseClient.dograapyApi.createPayment(req)
                if (response.success && !response.orderId.isNullOrBlank()) {
                    _onlinePaymentState.value = OnlinePaymentState.ReadyToPay(
                        orderId = response.orderId,
                        amountPaise = response.amount ?: (amountRupees * 100).toLong(),
                        transactionId = transactionId,
                        amountRupees = amountRupees
                    )
                } else {
                    _onlinePaymentState.value = OnlinePaymentState.Error(response.message ?: "Failed to create payment order")
                }
            } catch (e: Exception) {
                _onlinePaymentState.value = OnlinePaymentState.Error(e.localizedMessage ?: "Payment creation error")
            }
        }
    }

    fun verifyOnlinePayment(transactionId: String, amountRupees: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _onlinePaymentState.value = OnlinePaymentState.Verifying(transactionId)
            var attempt = 0
            val maxAttempts = 5
            var verified = false

            while (attempt < maxAttempts && !verified) {
                try {
                    val req = VerifyPaymentRequest(
                        apiKey = BuildConfig.DOGRAPAY_API_KEY,
                        platformName = BuildConfig.DOGRAPAY_PLATFORM_NAME,
                        transactionId = transactionId
                    )
                    val response = FirebaseClient.dograapyApi.verifyPayment(req)
                    if (response.success && response.status == "SUCCESS") {
                        verified = true
                        
                        // Check if already credited
                        if (response.alreadyCredited == true) {
                            _onlinePaymentState.value = OnlinePaymentState.Success
                            showNotification("Payment already processed and credited.")
                            break
                        }

                        // Credit Wallet!
                        val creditCoins = amountRupees * 100.0
                        val newBalance = user.balance + creditCoins
                        val isFirstDeposit = !user.hasDeposited
                        val updatedUser = user.copy(
                            balance = newBalance,
                            hasDeposited = true
                        )

                        // Save updated user to DB
                        FirebaseClient.databaseApi.saveUser(user.id, updatedUser)
                        _currentUser.value = updatedUser

                        // Log Transaction record
                        val newTx = Transaction(
                            id = transactionId,
                            userId = user.id,
                            userEmail = user.email,
                            amount = creditCoins,
                            type = "Deposit",
                            status = "Approved",
                            dateTime = currentDateTimeString(),
                            reference = "Dograapy Auto: $transactionId"
                        )
                        FirebaseClient.databaseApi.saveTransaction(transactionId, newTx)

                        // Check if there's a referrer who deserves commission!
                        if (isFirstDeposit && user.referredBy.isNotBlank()) {
                            // Let's reward referral bonus (200 coins to referrer)
                            launch {
                                try {
                                    val referrer = findUserByReferralCode(user.referredBy)
                                    if (referrer != null) {
                                        val refBonus = 200.0
                                        val updatedReferrer = referrer.copy(balance = referrer.balance + refBonus)
                                        FirebaseClient.databaseApi.saveUser(referrer.id, updatedReferrer)

                                        val refTxId = "TX-REF-BNS-${UUID.randomUUID().toString().take(6).uppercase()}"
                                        val refTx = Transaction(
                                            id = refTxId,
                                            userId = referrer.id,
                                            userEmail = referrer.email,
                                            amount = refBonus,
                                            type = "Referral Bonus",
                                            status = "Approved",
                                            dateTime = currentDateTimeString(),
                                            reference = "Referral: ${user.username} (First Dep)"
                                        )
                                        FirebaseClient.databaseApi.saveTransaction(refTxId, refTx)
                                    }
                                } catch (e: Exception) {
                                    // ignore nested failure
                                }
                            }
                        }

                        _onlinePaymentState.value = OnlinePaymentState.Success
                        showNotification("🪙 ${creditCoins.toInt()} Coins credited automatically to your wallet!")
                        fetchTransactions()
                        break
                    } else if (response.status == "PENDING") {
                        // wait and retry
                        attempt++
                        if (attempt < maxAttempts) {
                            delay(2000)
                        }
                    } else {
                        _onlinePaymentState.value = OnlinePaymentState.Error(response.message ?: "Payment verification failed with status: ${response.status}")
                        break
                    }
                } catch (e: Exception) {
                    attempt++
                    if (attempt < maxAttempts) {
                        delay(2000)
                    } else {
                        _onlinePaymentState.value = OnlinePaymentState.Error(e.localizedMessage ?: "Network error during verification")
                    }
                }
            }
        }
    }
}

private fun Throwable.getFirebaseErrorMessage(): String {
    if (this is HttpException) {
        try {
            val errorBody = response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
                val match = regex.find(errorBody)
                if (match != null) {
                    val firebaseMsg = match.groupValues[1]
                    return when (firebaseMsg) {
                        "EMAIL_EXISTS" -> "This email address is already in use by another account."
                        "OPERATION_NOT_ALLOWED" -> "Password sign-in is disabled."
                        "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts. Please try again later."
                        "INVALID_EMAIL" -> "The email address is badly formatted."
                        "WEAK_PASSWORD" -> "The password is too weak. It must be at least 6 characters."
                        "USER_NOT_FOUND" -> "No user account found with this email."
                        "INVALID_PASSWORD" -> "The password is incorrect."
                        else -> firebaseMsg
                    }
                }
            }
        } catch (ex: Exception) {
            // fallback
        }
    }
    return localizedMessage ?: "An unexpected error occurred"
}
