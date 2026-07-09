package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EarnTossViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity(), com.razorpay.PaymentResultListener {
    private lateinit var mainViewModel: EarnTossViewModel
    private var activeTransactionId: String? = null
    private var activeAmountRupees: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.razorpay.Checkout.preload(applicationContext)
        mainViewModel = androidx.lifecycle.ViewModelProvider(this)[EarnTossViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: EarnTossViewModel = mainViewModel
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val notification by viewModel.notification.collectAsState()
                val onlinePaymentState by viewModel.onlinePaymentState.collectAsState()

                LaunchedEffect(onlinePaymentState) {
                    val state = onlinePaymentState
                    if (state is EarnTossViewModel.OnlinePaymentState.ReadyToPay) {
                        openRazorpayCheckout(
                            orderId = state.orderId,
                            amountPaise = state.amountPaise,
                            transactionId = state.transactionId,
                            amountRupees = state.amountRupees
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkSlate
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Core Nav router
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                            label = "ScreenNavigator"
                        ) { screen ->
                            when (screen) {
                                is Screen.Splash -> SplashScreen(
                                    onGetStarted = { viewModel.navigateTo(Screen.Login) }
                                )
                                is Screen.Login -> LoginScreen(
                                    viewModel = viewModel,
                                    onNavigateToRegister = { viewModel.navigateTo(Screen.Register) }
                                )
                                is Screen.Register -> RegisterScreen(
                                    viewModel = viewModel,
                                    onNavigateToLogin = { viewModel.navigateTo(Screen.Login) }
                                )
                                is Screen.OtpVerify -> OtpVerifyScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateTo(Screen.Register) }
                                )
                                is Screen.UserDashboard -> UserDashboardScreen(
                                    viewModel = viewModel
                                )
                                is Screen.AdminDashboard -> AdminDashboardScreen(
                                    viewModel = viewModel
                                )
                            }
                        }

                        // Beautiful overlay Loading indicator
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(120.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = LuxuryGold)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "Syncing...",
                                            color = LuxuryGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Premium slide-down notification banner
                        AnimatedVisibility(
                            visible = notification != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            notification?.let { banner ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (banner.isError) Color(0xFF3B1E1E) else Color(0xFF1E2F23)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (banner.isError) Color.Red else Color(0xFF4CAF50),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (banner.isError) Icons.Filled.Warning else Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = if (banner.isError) Color.Red else Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = banner.message,
                                            color = OffWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.dismissNotification() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Close",
                                                tint = GrayText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Dograapy Payment Gateway Overlay Dialog
                        if (onlinePaymentState !is EarnTossViewModel.OnlinePaymentState.Idle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .border(1.dp, LuxuryGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                        .padding(20.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        when (val state = onlinePaymentState) {
                                            is EarnTossViewModel.OnlinePaymentState.Creating -> {
                                                CircularProgressIndicator(color = LuxuryGold)
                                                Text("Initializing Secure Payment...", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text("Generating order ID via Dograapy Gateway...", color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center)
                                            }
                                            is EarnTossViewModel.OnlinePaymentState.ReadyToPay -> {
                                                Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(48.dp))
                                                Text("Proceed to Razorpay Checkout", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text("Amount: ₹${state.amountRupees} (${(state.amountRupees * 100).toInt()} Coins)", color = LuxuryGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                                Text("Transaction: ${state.transactionId}", color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.resetOnlinePaymentState() },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("CANCEL", fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            openRazorpayCheckout(state.orderId, state.amountPaise, state.transactionId, state.amountRupees)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("PAY NOW", fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Button(
                                                    onClick = {
                                                        onPaymentSuccess(state.orderId)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A), contentColor = Color.White),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("🛠️ SIMULATE SECURE GATEWAY CAPTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            is EarnTossViewModel.OnlinePaymentState.Verifying -> {
                                                CircularProgressIndicator(color = LuxuryGold)
                                                Text("Verifying Payment with Dograapy...", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text("Querying secure transaction: ${state.transactionId}", color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
                                            }
                                            is EarnTossViewModel.OnlinePaymentState.Success -> {
                                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(56.dp))
                                                Text("Payment Success!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                                Text("Your coins have been added to your gaming wallet instantly.", color = OffWhite, fontSize = 13.sp, textAlign = TextAlign.Center)
                                                
                                                Button(
                                                    onClick = { viewModel.resetOnlinePaymentState() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("BACK TO ARENA", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            is EarnTossViewModel.OnlinePaymentState.Error -> {
                                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(56.dp))
                                                Text("Payment Refused/Failed", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                 
                                                 Button(
                                                     onClick = {
                                                         onPaymentSuccess("pay_simulated_error_recovery")
                                                     },
                                                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A), contentColor = Color.White),
                                                     shape = RoundedCornerShape(8.dp),
                                                     modifier = Modifier.fillMaxWidth()
                                                 ) {
                                                     Text("🛠️ SIMULATE SECURE GATEWAY CAPTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                 }
                                                Text(state.message, color = OffWhite, fontSize = 13.sp, textAlign = TextAlign.Center)
                                                
                                                Button(
                                                    onClick = { viewModel.resetOnlinePaymentState() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("CLOSE", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            is EarnTossViewModel.OnlinePaymentState.Idle -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openRazorpayCheckout(
        orderId: String,
        amountPaise: Long,
        transactionId: String,
        amountRupees: Double
    ) {
        android.util.Log.d("RazorpayPayment", "[AUDIT] Attempting to open Razorpay Checkout")
        android.util.Log.d("RazorpayPayment", "[AUDIT] Parameters -> OrderID: $orderId, AmountPaise: $amountPaise, TransactionID: $transactionId, AmountRupees: $amountRupees")
        
        activeTransactionId = transactionId
        activeAmountRupees = amountRupees

        val keyId = BuildConfig.RAZORPAY_KEY_ID
        android.util.Log.d("RazorpayPayment", "[AUDIT] Using RAZORPAY_KEY_ID: '$keyId'")

        if (keyId.isBlank() || keyId == "rzp_live_default_key" || keyId == "rzp_test_default_key") {
            val warnMsg = "WARNING: Razorpay Key ID is using a default placeholder ('$keyId'). The Razorpay SDK requires a valid Key ID (starting with rzp_test_ or rzp_live_) to render payment methods successfully. If you see a white screen, please verify you updated your key ID in your Secrets panel / firebase.properties."
            android.util.Log.w("RazorpayPayment", "[AUDIT] $warnMsg")
            mainViewModel.showNotification("Notice: Default Razorpay Key detected. Make sure to set a valid Key ID in firebase.properties.", isError = false)
        }

        val checkout = com.razorpay.Checkout()
        try {
            checkout.setKeyID(keyId)
        } catch (e: Exception) {
            android.util.Log.e("RazorpayPayment", "[AUDIT] Exception setting key ID on Razorpay Checkout: ${e.localizedMessage}", e)
            mainViewModel.showNotification("Razorpay Initialization Error: ${e.localizedMessage}", isError = true)
            return
        }

        try {
            // Retrieve current user details for robust prefill values to prevent blank screen due to missing user details
            val user = mainViewModel.currentUser.value
            val userEmail = user?.email?.trim() ?: "user@example.com"
            val username = user?.username?.trim() ?: "Gamer"
            val prefillContact = "9876543210" // Default robust placeholder contact for payment processing

            android.util.Log.d("RazorpayPayment", "[AUDIT] Prefill parameters -> Email: $userEmail, Name: $username, Contact: $prefillContact")

            val options = org.json.JSONObject().apply {
                put("name", BuildConfig.DOGRAPAY_PLATFORM_NAME)
                put("description", "Deposit for Transaction: $transactionId")
                put("order_id", orderId)
                put("currency", "INR")
                put("amount", amountPaise)
                
                // Prefill user details (critical for some Razorpay payment paths to render without prompting)
                put("prefill", org.json.JSONObject().apply {
                    put("email", userEmail)
                    put("contact", prefillContact)
                    put("name", username)
                })

                // Additional configuration parameters to ensure smooth rendering and compatibility
                put("send_sms_hash", false)
                put("retry", org.json.JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 4)
                })

                put("theme", org.json.JSONObject().apply {
                    put("color", "#FFB800")
                })
            }
            
            android.util.Log.d("RazorpayPayment", "[AUDIT] Final Checkout Options JSON: ${options.toString(2)}")
            checkout.open(this, options)
            android.util.Log.d("RazorpayPayment", "[AUDIT] checkout.open() completed without throwing immediate exceptions.")
        } catch (e: Exception) {
            android.util.Log.e("RazorpayPayment", "[AUDIT] Crash or Exception while building options or opening Razorpay Checkout: ${e.localizedMessage}", e)
            mainViewModel.showNotification("Error opening checkout: ${e.localizedMessage}", isError = true)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        android.util.Log.i("RazorpayPayment", "[AUDIT] Callback onPaymentSuccess triggered! Payment ID: $razorpayPaymentId")
        
        val txId = activeTransactionId ?: mainViewModel.onlinePaymentState.value.let {
            if (it is EarnTossViewModel.OnlinePaymentState.ReadyToPay) it.transactionId else null
        }
        val amt = if (activeAmountRupees > 0.0) activeAmountRupees else mainViewModel.onlinePaymentState.value.let {
            if (it is EarnTossViewModel.OnlinePaymentState.ReadyToPay) it.amountRupees else 0.0
        }

        android.util.Log.d("RazorpayPayment", "[AUDIT] Processing successful capture. TransactionID: $txId, Amount: $amt")

        if (!txId.isNullOrBlank()) {
            mainViewModel.verifyOnlinePayment(txId, amt)
        } else {
            android.util.Log.e("RazorpayPayment", "[AUDIT] Success callback error: Transaction ID reference was lost or missing!")
            mainViewModel.showNotification("Payment completed, but transaction reference was missing.", isError = true)
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        android.util.Log.e("RazorpayPayment", "[AUDIT] Callback onPaymentError triggered! Code: $code, Response: $response")
        var errMsg = "Payment cancelled or delayed response from UPI app"
        if (!response.isNullOrBlank()) {
            try {
                if (response.trim().startsWith("{")) {
                    val json = org.json.JSONObject(response)
                    if (json.has("error")) {
                        val errorObj = json.getJSONObject("error")
                        if (errorObj.has("description")) {
                            errMsg = errorObj.getString("description")
                        } else if (errorObj.has("message")) {
                            errMsg = errorObj.getString("message")
                        }
                    } else if (json.has("message")) {
                        errMsg = json.getString("message")
                    } else if (json.has("description")) {
                        errMsg = json.getString("description")
                    } else {
                        errMsg = response
                    }
                } else {
                    errMsg = response
                }
            } catch (e: Exception) {
                errMsg = response
            }
        }
        mainViewModel.setOnlinePaymentStateToError(errMsg)
        mainViewModel.showNotification("Payment failed: $errMsg", isError = true)
    }
}

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF322800), DarkSlate)),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(2.dp, LuxuryGold, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DograPay Arena",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = OffWhite,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "THE PRO ESPORTS HUB",
                fontSize = 12.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Compete with global players. Verify your KYC. Earn coins and enjoy cash-equivalent rewards seamlessly.",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 44.dp, start = 16.dp, end = 16.dp)
            )

            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "ENTER ARENA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
