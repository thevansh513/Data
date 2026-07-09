package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.EarnTossViewModel

// Color tokens redefined globally for a professional, high-end Luxury Gold & Dark Obsidian Theme
val DarkSlate = Color(0xFF0D1117)       // Background: #0D1117
val CharcoalSurface = Color(0xFF1B2335)  // Cards: #1B2335
val LuxuryGold = Color(0xFFFFB300)       // Accent: Gold (#FFB300)
val PaleGold = Color(0xFFFFC107)         // Shimmering lighter gold
val OffWhite = Color(0xFFFFFFFF)         // Text: White
val GrayText = Color(0xFF94A3B8)         // Elegant Steel Gray for Subtext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: EarnTossViewModel,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Forgot Password Flow state variables
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var resetStep by remember { mutableStateOf(1) } // 1: Enter email, 2: Enter OTP & New Password

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero branding logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF322800), DarkSlate)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, LuxuryGold, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Tournament Arena Logo",
                    tint = LuxuryGold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DograPay Arena",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = OffWhite,
                letterSpacing = 1.sp
            )

            Text(
                text = "PREMIUM ESPORTS ARENA",
                fontSize = 11.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Animated Switch between login box and forgot password box
            AnimatedContent(
                targetState = showForgotPassword,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ForgotPassToggle"
            ) { isForgot ->
                if (isForgot) {
                    // --- FORGOT PASSWORD PANEL ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Reset Your Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = if (resetStep == 1) 
                                    "Enter your registered email to receive a password reset verification code."
                                else "Enter the 6-digit verification code sent to your email and set a new password.",
                                fontSize = 12.sp,
                                color = GrayText,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (resetStep == 1) {
                                // Email field
                                OutlinedTextField(
                                    value = resetEmail,
                                    onValueChange = { resetEmail = it },
                                    label = { Text("Email Address", color = GrayText) },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = LuxuryGold) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedLabelColor = LuxuryGold,
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.sendResetPasswordOtp(resetEmail) {
                                            resetStep = 2
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("SEND VERIFICATION OTP", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // OTP Code field
                                OutlinedTextField(
                                    value = resetOtp,
                                    onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) resetOtp = it },
                                    label = { Text("6-Digit OTP Code", color = GrayText) },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = LuxuryGold) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedLabelColor = LuxuryGold,
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                // New Password
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Enter New Password", color = GrayText) },
                                    leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null, tint = LuxuryGold) },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedLabelColor = LuxuryGold,
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.verifyResetOtpAndChangePassword(resetOtp, newPassword) {
                                            showForgotPassword = false
                                            resetStep = 1
                                            resetEmail = ""
                                            resetOtp = ""
                                            newPassword = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("UPDATE PASSWORD", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = { 
                                    showForgotPassword = false
                                    resetStep = 1
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Back to Login", color = LuxuryGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // --- LOGIN PANEL ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Welcome Back Player",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Email Input
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address", color = GrayText) },
                                placeholder = { Text("e.g. player@example.com", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email", tint = LuxuryGold) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedLabelColor = LuxuryGold,
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input")
                                    .padding(bottom = 12.dp)
                            )

                            // Password Input
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = GrayText) },
                                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = "Password", tint = LuxuryGold) },
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(icon, contentDescription = "Toggle password visibility", tint = GrayText)
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedLabelColor = LuxuryGold,
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                                    .padding(bottom = 12.dp)
                            )

                            // Forgot Password Text Link
                            Text(
                                text = "Forgot Password?",
                                color = LuxuryGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(bottom = 16.dp)
                                    .clickable { showForgotPassword = true }
                            )

                            // Login Button
                            Button(
                                onClick = { viewModel.login(email, password) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryGold,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_button")
                            ) {
                                Text(
                                    text = "LOGIN",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegister() }
                    .testTag("signup_link"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = GrayText,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up (Free Coins)",
                    color = LuxuryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: EarnTossViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate)
            .statusBarsPadding()
            .navigationBarsPadding(),
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
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF322800), DarkSlate)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = "Sign Up Logo",
                    tint = LuxuryGold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join DograPay Arena",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = OffWhite
            )

            Text(
                text = "CREATE FREE GAMING ACCOUNT",
                fontSize = 11.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Sign Up Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "To guarantee fair play, multiple registrations are audited. Registration earns you 1000 free default practice coins!",
                        fontSize = 11.sp,
                        color = GrayText,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", color = GrayText) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email", tint = LuxuryGold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = LuxuryGold,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email")
                            .padding(bottom = 12.dp)
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = GrayText) },
                        leadingIcon = { Icon(Icons.Filled.Password, contentDescription = "Password", tint = LuxuryGold) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = "Toggle password visibility", tint = GrayText)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = LuxuryGold,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password")
                            .padding(bottom = 12.dp)
                    )

                    // Optional Referral Code Input
                    OutlinedTextField(
                        value = referralCode,
                        onValueChange = { referralCode = it.uppercase() },
                        label = { Text("Referral Code (Optional)", color = GrayText) },
                        placeholder = { Text("e.g. WELCOME200", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Group, contentDescription = "Referrer", tint = LuxuryGold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = LuxuryGold,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )

                    // Submit OTP Button
                    Button(
                        onClick = { viewModel.signUpSendOtp(email, password, referralCode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_otp_button")
                    ) {
                        Text(
                            text = "SEND EMAIL OTP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogin() }
                    .testTag("login_link"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = GrayText,
                    fontSize = 14.sp
                )
                Text(
                    text = "Login",
                    color = LuxuryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerifyScreen(
    viewModel: EarnTossViewModel,
    onNavigateBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate)
            .statusBarsPadding()
            .navigationBarsPadding(),
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
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF322800), DarkSlate)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "OTP Lock Logo",
                    tint = LuxuryGold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Email OTP Verification",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite
            )

            Text(
                text = "CONFIRM SECURITY CODE",
                fontSize = 11.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter 6-Digit OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "We have sent a verification code to your email. Please enter the 6-digit number to finalize registering your gaming wallet.",
                        fontSize = 12.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // 6 digit code input
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) code = it },
                        placeholder = { Text("000000", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = LuxuryGold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = LuxuryGold,
                            unfocusedTextColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .testTag("otp_input")
                            .padding(bottom = 24.dp)
                    )

                    // Submit Verification
                    Button(
                        onClick = { viewModel.verifyOtpAndRegister(code) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_otp_button")
                    ) {
                        Text(
                            text = "VERIFY & REGISTER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.testTag("back_to_register_button")
            ) {
                Text(
                    text = "Go Back to Sign Up",
                    color = GrayText,
                    fontSize = 14.sp
                )
            }
        }
    }
}
