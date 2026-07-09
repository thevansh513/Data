package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface FirebaseIdentityApi {
    @POST("v1/accounts:signUp")
    suspend fun signUp(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthRequest
    ): FirebaseAuthResponse

    @POST("v1/accounts:signInWithPassword")
    suspend fun signIn(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthRequest
    ): FirebaseAuthResponse

    @POST("v1/accounts:update")
    suspend fun updateAccount(
        @Query("key") apiKey: String,
        @Body request: FirebaseUpdateAccountRequest
    ): FirebaseUpdateAccountResponse
}

interface FirebaseDatabaseApi {
    @GET("users/{userId}.json")
    suspend fun getUser(@Path("userId") userId: String): User?

    @PUT("users/{userId}.json")
    suspend fun saveUser(@Path("userId") userId: String, @Body user: User): User

    @GET("users.json")
    suspend fun getAllUsers(): Map<String, User>?

    @GET("tournaments.json")
    suspend fun getTournaments(): Map<String, Tournament>?

    @PUT("tournaments/{tournamentId}.json")
    suspend fun saveTournament(@Path("tournamentId") tournamentId: String, @Body tournament: Tournament): Tournament

    @DELETE("tournaments/{tournamentId}.json")
    suspend fun deleteTournament(@Path("tournamentId") tournamentId: String): retrofit2.Response<Unit>

    @PATCH("tournaments/{tournamentId}.json")
    suspend fun updateTournament(@Path("tournamentId") tournamentId: String, @Body fields: Map<String, String?>): Map<String, String?>

    @PATCH("tournaments/{tournamentId}/participants.json")
    suspend fun joinTournament(@Path("tournamentId") tournamentId: String, @Body fields: Map<String, Boolean>): Map<String, Boolean>

    @GET("transactions.json")
    suspend fun getTransactions(): Map<String, Transaction>?

    @PUT("transactions/{transactionId}.json")
    suspend fun saveTransaction(@Path("transactionId") transactionId: String, @Body transaction: Transaction): Transaction

    @PATCH("transactions/{transactionId}.json")
    suspend fun updateTransactionStatus(@Path("transactionId") transactionId: String, @Body fields: Map<String, String>): Map<String, String>

    // Support Ticket Support
    @GET("tickets.json")
    suspend fun getTickets(): Map<String, SupportTicket>?

    @PUT("tickets/{ticketId}.json")
    suspend fun saveTicket(@Path("ticketId") ticketId: String, @Body ticket: SupportTicket): SupportTicket

    // Promo Code Support
    @GET("promoCodes.json")
    suspend fun getPromoCodes(): Map<String, PromoCode>?

    @PUT("promoCodes/{code}.json")
    suspend fun savePromoCode(@Path("code") code: String, @Body promoCode: PromoCode): PromoCode

    @PATCH("promoCodes/{code}/claimedBy.json")
    suspend fun claimPromoCode(@Path("code") code: String, @Body claimedBy: Map<String, Boolean>): Map<String, Boolean>

    // Period Game History Support
    @GET("periodGames.json")
    suspend fun getPeriodGames(): Map<String, PeriodGameResult>?

    @PUT("periodGames/{periodId}.json")
    suspend fun savePeriodGameResult(@Path("periodId") periodId: String, @Body gameResult: PeriodGameResult): PeriodGameResult
}

interface OtpEmailApi {
    @POST("api/email")
    suspend fun sendEmail(@Body request: EmailRequest): EmailResponse
}

interface DograapyPaymentApi {
    @POST("api/payment/create")
    suspend fun createPayment(@Body request: CreatePaymentRequest): CreatePaymentResponse

    @POST("api/payment/verify")
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): VerifyPaymentResponse
}

object FirebaseClient {
    @Volatile
    var idToken: String? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val databaseHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = idToken
            if (!token.isNullOrBlank()) {
                val originalUrl = originalRequest.url
                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("auth", token)
                    .build()
                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
        .build()

    val identityApi: FirebaseIdentityApi = Retrofit.Builder()
        .baseUrl("https://identitytoolkit.googleapis.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FirebaseIdentityApi::class.java)

    val API_KEY: String = try {
        val key = BuildConfig.FIREBASE_API_KEY
        if (key.isNullOrBlank() || key == "YOUR_FIREBASE_API_KEY") "" else key
    } catch (e: Throwable) {
        ""
    }

    val databaseUrl: String = try {
        val url = BuildConfig.FIREBASE_DATABASE_URL
        if (url.isNullOrBlank() || url == "YOUR_FIREBASE_DATABASE_URL" || url.startsWith("https://YOUR_DATABASE_NAME")) {
            "https://placeholder-firebase.firebaseio.com/"
        } else {
            if (url.endsWith("/")) url else "$url/"
        }
    } catch (e: Throwable) {
        "https://placeholder-firebase.firebaseio.com/"
    }

    val databaseApi: FirebaseDatabaseApi = Retrofit.Builder()
        .baseUrl(databaseUrl)
        .client(databaseHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FirebaseDatabaseApi::class.java)

    val emailApi: OtpEmailApi = Retrofit.Builder()
        .baseUrl("https://turbo-happiness-eight.vercel.app/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OtpEmailApi::class.java)

    val dograapyApi: DograapyPaymentApi = Retrofit.Builder()
        .baseUrl("https://dograpay.online/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(DograapyPaymentApi::class.java)
}
