# Android Integration Guide — Dograapy Payment Gateway

> Complete Kotlin integration using **Retrofit**, **OkHttp**, and the **Razorpay Checkout SDK**.

---

## Table of Contents

1. [Register Your Application](#1-register-your-application)
2. [Store the API Key Securely](#2-store-the-api-key-securely)
3. [Generate a Unique transactionId](#3-generate-a-unique-transactionid)
4. [Setup Retrofit + OkHttp](#4-setup-retrofit--okhttp)
5. [Call Create Payment API](#5-call-create-payment-api)
6. [Open Razorpay Checkout](#6-open-razorpay-checkout)
7. [Call Verify Payment API](#7-call-verify-payment-api)
8. [Credit User's Wallet](#8-credit-users-wallet)

---

## 1. Register Your Application

Call this **once** from Postman or cURL — not from your Android app.

```bash
curl -X POST https://dograpay.online/api/application \
  -H "Content-Type: application/json" \
  -d '{
    "platformName": "MyEarnApp",
    "ownerName": "Your Name",
    "ownerEmail": "you@example.com"
  }'
```

Save the returned `apiKey` — it is shown only **once**.

---

## 2. Store the API Key Securely

**Never hardcode your API key in source code!**

### Option A — BuildConfig (Recommended for release builds)

In your `build.gradle` (app module):

```groovy
android {
    defaultConfig {
        buildConfigField "String", "PAYMENT_API_KEY", '"dgpy_your_actual_key_here"'
        buildConfigField "String", "PLATFORM_NAME", '"MyEarnApp"'
        buildConfigField "String", "PAYMENT_BASE_URL", '"https://dograpay.online/"'
    }
}
```

Access in Kotlin:

```kotlin
val apiKey = BuildConfig.PAYMENT_API_KEY
val platformName = BuildConfig.PLATFORM_NAME
```

### Option B — EncryptedSharedPreferences

```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val sharedPreferences = EncryptedSharedPreferences.create(
    context,
    "payment_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// Save
sharedPreferences.edit().putString("api_key", "dgpy_your_key").apply()

// Read
val apiKey = sharedPreferences.getString("api_key", "")
```

---

## 3. Generate a Unique transactionId

```kotlin
import java.util.UUID

fun generateTransactionId(): String {
    // Format: TRX + UUID without dashes (uppercase)
    return "TRX" + UUID.randomUUID().toString().replace("-", "").uppercase()
    // Example: TRXA1B2C3D4E5F6A1B2C3D4E5F6A1B2C3
}
```

> **Rules:**
> - Must be unique per payment
> - Store it locally before calling Create Payment (in case the network fails)
> - Never reuse a transactionId

---

## 4. Setup Retrofit + OkHttp

### Add dependencies to `build.gradle` (app):

```groovy
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.11.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    implementation 'com.razorpay:checkout:1.6.41'
}
```

### Data classes

```kotlin
// Request models
data class CreatePaymentRequest(
    val apiKey: String,
    val platformName: String,
    val transactionId: String,
    val userId: String,
    val amount: Double
)

data class VerifyPaymentRequest(
    val apiKey: String,
    val platformName: String,
    val transactionId: String
)

// Response models
data class CreatePaymentResponse(
    val success: Boolean,
    val orderId: String?,
    val amount: Long?,           // paise
    val currency: String?,
    val transactionId: String?,
    val error: String?,
    val message: String?
)

data class VerifyPaymentResponse(
    val success: Boolean,
    val status: String,          // SUCCESS, PENDING, FAILED, CANCELLED, NOT_FOUND
    val amount: Double?,
    val transactionId: String?,
    val message: String?,
    val alreadyCredited: Boolean?
)
```

### Retrofit interface

```kotlin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApiService {

    @POST("api/payment/create")
    suspend fun createPayment(
        @Body request: CreatePaymentRequest
    ): Response<CreatePaymentResponse>

    @POST("api/payment/verify")
    suspend fun verifyPayment(
        @Body request: VerifyPaymentRequest
    ): Response<VerifyPaymentResponse>
}
```

### Retrofit client singleton

```kotlin
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val paymentApi: PaymentApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.PAYMENT_BASE_URL)  // https://dograpay.online/
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PaymentApiService::class.java)
}
```

---

## 5. Call Create Payment API

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentRepository {

    suspend fun createPayment(
        userId: String,
        amountInRupees: Double
    ): Result<CreatePaymentResponse> {

        val transactionId = generateTransactionId()

        // IMPORTANT: Save transactionId locally BEFORE network call
        // In case the app crashes, you can still verify later
        saveTransactionIdLocally(transactionId)

        return try {
            val response = RetrofitClient.paymentApi.createPayment(
                CreatePaymentRequest(
                    apiKey = BuildConfig.PAYMENT_API_KEY,
                    platformName = BuildConfig.PLATFORM_NAME,
                    transactionId = transactionId,
                    userId = userId,
                    amount = amountInRupees
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.body()?.message ?: "Payment creation failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveTransactionIdLocally(transactionId: String) {
        // Save to SharedPreferences or Room DB
        // so you can verify even if the app is closed
    }
}
```

In your ViewModel / Activity:

```kotlin
// In ViewModel
private val paymentRepository = PaymentRepository()

fun initiatePayment(activity: Activity, userId: String, amount: Double) {
    viewModelScope.launch {
        _paymentState.value = PaymentState.Loading

        val result = paymentRepository.createPayment(userId, amount)

        result.onSuccess { response ->
            // Open Razorpay Checkout
            openRazorpayCheckout(
                activity = activity,
                orderId = response.orderId!!,
                amount = response.amount!!,       // already in paise
                transactionId = response.transactionId!!
            )
        }.onFailure { error ->
            _paymentState.value = PaymentState.Error(error.message ?: "Unknown error")
        }
    }
}
```

---

## 6. Open Razorpay Checkout

```kotlin
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private var currentTransactionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Preload Razorpay for faster checkout (call in onCreate)
        Checkout.preload(applicationContext)
    }

    fun openRazorpayCheckout(
        orderId: String,
        amount: Long,         // in paise (e.g. 50000 for ₹500)
        transactionId: String
    ) {
        currentTransactionId = transactionId

        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)  // Your public Razorpay Key

        val options = JSONObject().apply {
            put("name", BuildConfig.PLATFORM_NAME)
            put("description", "Payment for Transaction: $transactionId")
            put("order_id", orderId)
            put("currency", "INR")
            put("amount", amount)       // paise
            put("prefill", JSONObject().apply {
                // Optional: prefill user's details
                // put("email", "user@example.com")
                // put("contact", "9876543210")
            })
            put("theme", JSONObject().apply {
                put("color", "#3399cc")
            })
        }

        checkout.open(this, options)
    }

    // ── Razorpay callbacks ────────────────────────────────────────────────

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        // DO NOT credit the wallet here — the client cannot be trusted!
        // Call Verify Payment API instead.
        verifyPayment(currentTransactionId)
    }

    override fun onPaymentError(code: Int, response: String?) {
        // Payment was cancelled or failed
        showMessage("Payment failed: $response")
    }
}
```

> **Critical Rule:** Never trust `onPaymentSuccess` directly. Always call the Verify Payment API. The webhook has already updated the status — your verify call checks that.

---

## 7. Call Verify Payment API

```kotlin
// In PaymentRepository
suspend fun verifyPayment(transactionId: String): Result<VerifyPaymentResponse> {
    return try {
        val response = RetrofitClient.paymentApi.verifyPayment(
            VerifyPaymentRequest(
                apiKey = BuildConfig.PAYMENT_API_KEY,
                platformName = BuildConfig.PLATFORM_NAME,
                transactionId = transactionId
            )
        )

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Verification failed"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

With retry logic (webhook may take 1-2 seconds):

```kotlin
suspend fun verifyPaymentWithRetry(
    transactionId: String,
    maxRetries: Int = 3,
    delayMs: Long = 2000
): Result<VerifyPaymentResponse> {
    repeat(maxRetries) { attempt ->
        val result = verifyPayment(transactionId)

        result.onSuccess { response ->
            // If SUCCESS → return immediately
            if (response.status == "SUCCESS") return Result.success(response)
            // If PENDING → wait and retry
            if (response.status == "PENDING" && attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(delayMs)
            }
        }.onFailure {
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(delayMs)
            }
        }
    }
    return verifyPayment(transactionId)
}
```

---

## 8. Credit User's Wallet

```kotlin
// In PaymentActivity
private fun verifyPayment(transactionId: String) {
    lifecycleScope.launch {
        showLoading(true)

        val result = paymentRepository.verifyPaymentWithRetry(transactionId)

        result.onSuccess { response ->
            when {
                response.success && !response.alreadyCredited!! -> {
                    // ✅ First-time credit — add coins/balance to user's wallet
                    val amount = response.amount ?: 0.0
                    creditUserWallet(amount, transactionId)
                    showSuccess("₹$amount added to your wallet!")
                }

                response.success && response.alreadyCredited == true -> {
                    // Already credited in a previous session — don't credit again
                    showMessage("Payment already processed.")
                }

                response.status == "PENDING" -> {
                    // Webhook hasn't arrived yet — tell user to wait
                    showMessage("Payment is being processed. Check back in a moment.")
                }

                response.status == "FAILED" || response.status == "CANCELLED" -> {
                    showMessage("Payment ${response.status?.lowercase()}. Please try again.")
                }

                response.status == "NOT_FOUND" -> {
                    showMessage("Transaction not found. Contact support.")
                }
            }
        }.onFailure { error ->
            showMessage("Verification error: ${error.message}")
        }

        showLoading(false)
    }
}

private fun creditUserWallet(amount: Double, transactionId: String) {
    // Add credits/coins to user's in-app wallet
    // Save the credit locally and sync to your backend
    WalletManager.addBalance(amount)
    // Log the credit with transactionId for audit trail
}
```

---

## Complete Flow Summary

```
User clicks "Add Money"
        ↓
Android calls POST /api/payment/create
        ↓
Server verifies API key + rejects duplicate transactionId
        ↓
Server creates Razorpay Order → saves PENDING in Firestore
        ↓
Android receives orderId → opens Razorpay Checkout SDK
        ↓
User pays → Razorpay sends webhook to /api/webhook
        ↓
Server verifies webhook signature → sets status = SUCCESS
        ↓
Android receives onPaymentSuccess → calls POST /api/payment/verify
        ↓
Server finds SUCCESS + credited=false → sets credited=true → returns amount
        ↓
Android credits user wallet ✅
```

---

## Add to AndroidManifest.xml

```xml
<!-- Required for Razorpay -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Razorpay activity (add inside <application>) -->
<activity
    android:name="com.razorpay.CheckoutActivity"
    android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|screenSize|smallestScreenSize"
    android:exported="false"
    android:hardwareAccelerated="true"
    android:theme="@style/Checkout.Theme" />
```

---

## ProGuard Rules

Add to `proguard-rules.pro`:

```proguard
# Razorpay
-keepclassmembers class * {
    @com.razorpay.* <methods>;
}
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes SerializedName
```
