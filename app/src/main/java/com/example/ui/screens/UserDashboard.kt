package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Tournament
import com.example.data.Transaction
import com.example.viewmodel.EarnTossViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()

    var activeTab by remember { mutableStateOf("home") } // home, tournaments, wallet, profile

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    var showNotificationsDialog by remember { mutableStateOf(false) }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LuxuryGold)
                ) {
                    Text("DISMISS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "LIVE ESPORTS NOTICES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val alerts = listOf(
                        "🏆 BGMI Squad Battle starts in 15 minutes! Room credentials are live.",
                        "🎉 Bonus Added: Claim 50 bonus coins using code WELCOME190 inside passbook.",
                        "🔒 Identity Check: Verify Aadhaar KYC inside Withdrawal section for instant cashout.",
                        "⚡ Automated Gateway: Instant deposits with UPI are fully active 24/7."
                    )
                    alerts.forEach { alert ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color(0xFF2E3344), RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = alert,
                                color = OffWhite,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            containerColor = CharcoalSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Profile clickable avatar badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x22F59E0B))
                                .border(1.dp, LuxuryGold, CircleShape)
                                .clickable { activeTab = "profile" },
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarIcon = when (user?.avatarUrl?.lowercase()) {
                                "champion" -> Icons.Filled.MilitaryTech
                                "master" -> Icons.Filled.LocalActivity
                                "legend" -> Icons.Filled.Stars
                                else -> Icons.Filled.Person
                            }
                            Icon(
                                imageVector = avatarIcon,
                                contentDescription = "Profile Tab",
                                tint = LuxuryGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "DograPay Arena",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = OffWhite,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "PREMIUM ESPORTS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LuxuryGold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick balance display pill (clickable to wallet)
                    user?.let { player ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x29F59E0B))
                                .clickable { activeTab = "wallet" }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🪙 ${String.format("%.0f", player.balance)}",
                                color = LuxuryGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Add Coins",
                                tint = LuxuryGold,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                             )
                        }
                    }

                    // Notification bell with badge dot
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications bell",
                                tint = OffWhite,
                                modifier = Modifier.size(22.dp)
                            )
                            // Indicator badge dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalSurface,
                    titleContentColor = OffWhite
                ),
                modifier = Modifier.border(0.5.dp, Color(0xFF2E3344), RoundedCornerShape(0.dp))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(0.5.dp, Color(0xFF2E3344), RoundedCornerShape(0.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "tournaments",
                    onClick = { activeTab = "tournaments" },
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Tournaments") },
                    label = { Text("Tournaments", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "wallet",
                    onClick = { activeTab = "wallet" },
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Wallet", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "refer",
                    onClick = { activeTab = "refer" },
                    icon = { Icon(Icons.Filled.GroupAdd, contentDescription = "Refer") },
                    label = { Text("Refer", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
            }
        },
        containerColor = DarkSlate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkSlate)
        ) {
            // Tab content box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (activeTab) {
                    "home" -> HomeTab(
                        viewModel = viewModel,
                        onNavigateToTab = { activeTab = it }
                    )
                    "tournaments" -> TournamentsTab(
                        tournaments = tournaments,
                        currentUserId = user?.id ?: "",
                        onJoin = { viewModel.joinTournament(it) }
                    )
                    "wallet" -> WalletTab(viewModel = viewModel)
                    "refer" -> ReferTab(viewModel = viewModel)
                    "profile" -> ProfileTab(viewModel = viewModel)
                }
            }
        }
    }
}

// --- HOME TAB (DASHBOARD METRICS AND BANNER CAROUSEL) ---
@Composable
fun HomeTab(
    viewModel: EarnTossViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    // Countdown Timer State - starts at 18 minutes and 42 seconds (1122 seconds) and ticks down!
    var secondsLeft by remember { mutableStateOf(1122) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft--
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val countdownText = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Wallet Balance Card at the top
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(LuxuryGold.copy(alpha = 0.5f), Color.Transparent))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE WALLET BALANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🪙 ${String.format("%.0f", user?.balance ?: 0.0)} Coins",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = LuxuryGold
                        )
                        Text(
                            text = "Equivalent to ₹${String.format("%.2f", (user?.balance ?: 0.0) / 100.0)} INR",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onNavigateToTab("wallet") },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DEPOSIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1.5 Quick Actions (Deposit, Withdraw, Refer, Support)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deposit Quick Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("wallet") }
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1610B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Deposit",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                    }
                }

                // Withdraw Quick Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("wallet") }
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x16EF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Payments,
                                contentDescription = "Withdraw",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Withdraw", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                    }
                }

                // Refer Quick Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("refer") }
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x16FFB300)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GroupAdd,
                                contentDescription = "Refer",
                                tint = LuxuryGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Refer & Earn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite, maxLines = 1)
                    }
                }

                // Support Quick Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("profile") }
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x163B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HeadsetMic,
                                contentDescription = "Support",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Support", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                    }
                }
            }
        }

        // 2. Large Hero Banner for Live Tournaments
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFFEF4444).copy(alpha = 0.4f), Color.Transparent))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2335),
                                    Color(0xFF3B1D1D)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE NOW",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                               text = "DAILY PRO ARENA SERIES",
                               color = LuxuryGold,
                               fontSize = 10.sp,
                               fontWeight = FontWeight.Bold,
                               letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Dominate the BGMI Esports Arena",
                            color = OffWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Compete with top players across India, register for custom rooms, and turn your skills into direct wallet rewards instantly.",
                            color = GrayText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onNavigateToTab("tournaments") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLAY NOW", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // 3. Countdown to Next Match Widget
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2230)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("NEXT ESCORE MATCH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            Text("BGMI Squad Battle #401", fontSize = 10.sp, color = GrayText)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = countdownText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LuxuryGold
                        )
                        Text("COUNTDOWN", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Featured Tournaments Carousel (Horizontal Scroll)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FEATURED TOURNAMENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    letterSpacing = 0.5.sp
                )

                if (tournaments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CharcoalSurface, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matches listed yet.", color = GrayText, fontSize = 11.sp)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tournaments.take(5)) { match ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { onNavigateToTab("tournaments") }
                                    .border(0.5.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = match.game.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = match.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OffWhite,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("PRIZE POOL", fontSize = 8.sp, color = GrayText)
                                            Text("🪙 ${String.format("%.0f", match.prizePool)}", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("ENTRY FEE", fontSize = 8.sp, color = GrayText)
                                            Text("🪙 ${String.format("%.0f", match.entryFee)}", fontSize = 12.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Winners Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RECENT TOURNAMENT WINNERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )
                    }

                    val winners = listOf(
                        Triple("thevansh513", "BGMI Squad #208", "🪙 5,000"),
                        Triple("rahul_gaming", "Free Fire Solo #104", "🪙 2,500"),
                        Triple("esports_hero", "COD Mobile #099", "🪙 1,500"),
                        Triple("ludo_master", "Ludo Classic #302", "🪙 500")
                    )

                    winners.forEachIndexed { idx, (winner, battle, prize) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1AF59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("#${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LuxuryGold)
                                }
                                Column {
                                    Text(text = winner, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                    Text(text = battle, fontSize = 10.sp, color = GrayText)
                                }
                            }
                            Text(text = prize, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                        if (idx < winners.size - 1) {
                            Divider(color = Color(0xFF2E3344), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE TAB (AVATAR, STATS GRID, AND SETTINGS) ---
@Composable
fun ProfileTab(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val userId = user?.id ?: ""
    
    // 1. Calculate dynamic player statistics
    val matchesPlayed = remember(tournaments, userId) {
        tournaments.count { it.participants.containsKey(userId) }
    }
    
    val userTransactions = remember(transactions, userId) {
        transactions.filter { it.userId == userId && it.status == "Approved" }
    }
    
    val totalWinnings = remember(userTransactions) {
        userTransactions.filter { 
            it.type.contains("Win", ignoreCase = true) || 
            it.type.contains("Winning", ignoreCase = true) ||
            it.type.contains("Payout", ignoreCase = true)
        }.sumOf { it.amount }
    }
    
    val winRate = remember(matchesPlayed) {
        if (matchesPlayed > 0) {
            val rate = (matchesPlayed * 33 + 41) % 55 + 20
            "$rate%"
        } else {
            "0%"
        }
    }
    
    val kdRatio = remember(matchesPlayed) {
        if (matchesPlayed > 0) {
            val kd = (matchesPlayed * 0.17 + 1.25)
            String.format("%.2f", kd)
        } else {
            "0.00"
        }
    }
    
    val rankBadge = remember(matchesPlayed) {
        when {
            matchesPlayed <= 1 -> "Bronze"
            matchesPlayed <= 3 -> "Silver"
            matchesPlayed <= 6 -> "Gold"
            matchesPlayed <= 12 -> "Diamond"
            else -> "Conqueror"
        }
    }
    
    val rankColor = remember(rankBadge) {
        when (rankBadge) {
            "Bronze" -> Color(0xFFCD7F32)
            "Silver" -> Color(0xFFC0C0C0)
            "Gold" -> LuxuryGold
            "Diamond" -> Color(0xFF00BFFF)
            else -> Color(0xFFE0115F) // Conqueror Ruby Red
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Core Profile Details Card with Rank Badge representation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Avatar
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AF59E0B))
                            .border(2.dp, rankColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarIcon = when (user?.avatarUrl?.lowercase()) {
                            "champion" -> Icons.Filled.MilitaryTech
                            "master" -> Icons.Filled.LocalActivity
                            "legend" -> Icons.Filled.Stars
                            else -> Icons.Filled.SportsEsports
                        }
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Avatar",
                            tint = rankColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user?.username?.ifBlank { user?.email } ?: "Gamer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // UID badge
                    Text(
                        text = "UID: ${userId.take(12).uppercase()}",
                        fontSize = 10.sp,
                        color = GrayText,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rank Badge Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(rankColor.copy(alpha = 0.15f))
                            .border(1.dp, rankColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = rankColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = "RANK: ${rankBadge.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = rankColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Gamer Stats Grid
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ARENA PERFORMANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Total Winnings Stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WINNINGS", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🪙 ${String.format("%.0f", totalWinnings)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }

                        // Divider
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF2E3344)))

                        // Matches Played Stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("MATCHES", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$matchesPlayed", fontSize = 16.sp, fontWeight = FontWeight.Black, color = OffWhite)
                        }

                        // Divider
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF2E3344)))

                        // Win Rate Stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WIN RATE", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(winRate, fontSize = 16.sp, fontWeight = FontWeight.Black, color = LuxuryGold)
                        }

                        // Divider
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF2E3344)))

                        // K/D Ratio Stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("K/D RATIO", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(kdRatio, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        // 2.5 Arena Achievements
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ARENA ACHIEVEMENTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.MilitaryTech,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Achievement 1: Slayer Legend
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Slayer Legend (50 Finishes)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OffWhite)
                            Text("34 / 50", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LuxuryGold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0D1117))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f)
                                    .fillMaxHeight()
                                    .background(LuxuryGold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Achievement 2: First Blood (Unlocked!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1122C55E))
                            .border(0.5.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("First Blood", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            Text("Enter your very first match in EarnToss arena.", fontSize = 9.sp, color = GrayText)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF22C55E))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("UNLOCKED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Achievement 3: Elite Competitor
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Elite Competitor (Join 5 matches)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OffWhite)
                            Text("3 / 5", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LuxuryGold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0D1117))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.60f)
                                    .fillMaxHeight()
                                    .background(LuxuryGold)
                            )
                        }
                    }
                }
            }
        }

        // 3. Customize Profile and identity details (SettingsTab)
        item {
            SettingsTab(viewModel = viewModel)
        }
    }
}

// --- GAMES HUB TAB (3 GAMES IN ONE PANEL) ---

@Composable
fun GamesTab(
    viewModel: EarnTossViewModel
) {
    var gameSelection by remember { mutableStateOf("periodic") } // periodic, instant, aviator

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(CharcoalSurface, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val buttons = listOf("periodic" to "Periodic H&T", "instant" to "Instant 1v1", "aviator" to "Aviator Plane")
            buttons.forEach { (key, title) ->
                val selected = gameSelection == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) LuxuryGold else Color.Transparent)
                        .clickable { gameSelection = key }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else GrayText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (gameSelection) {
                "periodic" -> PeriodicHeadTailGame(viewModel = viewModel)
                "instant" -> InstantHeadTailGame(viewModel = viewModel)
                "aviator" -> AviatorGameView(viewModel = viewModel)
            }
        }
    }
}

// GAME A: PERIODIC HEAD AND TAIL (GLOBAL SYNCED RESULTS)

@Composable
fun PeriodicHeadTailGame(
    viewModel: EarnTossViewModel
) {
    val countdown by viewModel.periodCountdown.collectAsState()
    val periodId by viewModel.activePeriodId.collectAsState()
    val history by viewModel.periodHistory.collectAsState()
    val userBets by viewModel.userBets.collectAsState()

    var betAmountText by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header stats Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Global Period Countdown Match",
                        fontSize = 13.sp,
                        color = GrayText,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "PERIOD ID: #$periodId",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big beautiful circular countdown visual
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AF59E0B))
                            .border(3.dp, LuxuryGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d", countdown),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = LuxuryGold
                            )
                            Text(
                                text = "SECONDS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Match resolution automatically flips coin globally every 30 seconds. Double your coins with 1.9x rewards on wins!",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = GrayText,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Active Bettor Placement Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Place Your Bets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = betAmountText,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) betAmountText = it },
                        label = { Text("Bet Coins Count", color = GrayText) },
                        placeholder = { Text("e.g. 50", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = LuxuryGold) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Place Head
                        Button(
                            onClick = {
                                val amt = betAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    viewModel.placePeriodicBet("HEAD", amt)
                                    betAmountText = ""
                                } else {
                                    viewModel.showNotification("Please enter a valid bet amount", isError = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PaleGold, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("BET HEAD 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Place Tail
                        Button(
                            onClick = {
                                val amt = betAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    viewModel.placePeriodicBet("TAIL", amt)
                                    betAmountText = ""
                                } else {
                                    viewModel.showNotification("Please enter a valid bet amount", isError = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("BET TAIL 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Display active bets placed by user on this round
                    if (userBets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF2E3344))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Active Round Bets: " + userBets.map { "${it.key}: 🪙${it.value}" }.joinToString(", "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Historic trends list
        item {
            Text(
                text = "Recent Period Flares (History)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Awaiting first flip stats data...", color = GrayText, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(history) { result ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Period: #${result.periodId}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            Text(text = result.dateTime, fontSize = 10.sp, color = GrayText)
                        }

                        // Circular result indicator
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (result.result == "HEAD") Color(0x1AF59E0B) else Color(0x33F59E0B))
                                .border(
                                    1.dp,
                                    if (result.result == "HEAD") PaleGold else Color(0xFFF59E0B),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = result.result.take(1),
                                color = if (result.result == "HEAD") PaleGold else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// GAME B: INSTANT HEAD AND TAIL (1V1 INSTANT RESULT)

@Composable
fun InstantHeadTailGame(
    viewModel: EarnTossViewModel
) {
    var betAmountText by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("HEAD") }
    
    // Animation/Result States
    var isSpinning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var wonMatch by remember { mutableStateOf(false) }
    var landedResult by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Instant 1v1 Arena",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Spinning coin graphics simulation
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1E2230),
                                        Color(0xFF141722)
                                    )
                                )
                            )
                            .border(3.dp, LuxuryGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.size(50.dp))
                        } else {
                            Icon(
                                imageVector = if (landedResult == "TAIL") Icons.Filled.Stars else Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showResult && !isSpinning) {
                        Text(
                            text = if (wonMatch) "🎉 YOU WON 2x REWARDS!" else "❌ MATCH LOST!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (wonMatch) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Text(
                            text = "Landed on: $landedResult",
                            fontSize = 13.sp,
                            color = GrayText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Select your prediction, enter bet amount, and click Spin Arena to flip coins instantly!",
                            fontSize = 12.sp,
                            color = GrayText,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customize Arena Bet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Pick Head or Tail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF141722), RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        listOf("HEAD", "TAIL").forEach { opt ->
                            val active = selectedOption == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) PaleGold else Color.Transparent)
                                    .clickable { selectedOption = opt }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else OffWhite,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = betAmountText,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) betAmountText = it },
                        label = { Text("Coin Investment", color = GrayText) },
                        placeholder = { Text("e.g. 100", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = LuxuryGold) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = betAmountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0) {
                                viewModel.showNotification("Please enter a valid bet", isError = true)
                                return@Button
                            }
                            isSpinning = true
                            showResult = false
                            viewModel.playInstantHeadAndTail(selectedOption, amt) { result, won ->
                                landedResult = result
                                wonMatch = won
                                isSpinning = false
                                showResult = true
                                betAmountText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isSpinning
                    ) {
                        Text(
                            text = if (isSpinning) "COIN SPINNING..." else "SPIN ARENA (2x Payout)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// GAME C: AVIATOR MULTIPLIER CRASH GAME

@Composable
fun AviatorGameView(
    viewModel: EarnTossViewModel
) {
    val multiplier by viewModel.aviatorMultiplier.collectAsState()
    val flightState by viewModel.aviatorState.collectAsState()
    val betActive by viewModel.aviatorBetActive.collectAsState()

    var betAmtText by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aviator Crash Flight",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Plane graphics representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (flightState == "Crashed") {
                            Text(
                                text = "FLEW AWAY 💥",
                                color = Color(0xFFEF4444),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else if (flightState == "Flying") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.FlightTakeoff,
                                    contentDescription = "Plane",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${multiplier}x",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Flight,
                                    contentDescription = "Plane Idle",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready for Takeoff",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Cash out before the plane crashes to multiply your coins! Wait too long, and you lose your bet.",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (betActive) {
                        Text(
                            text = "Flight Multiplier: ${multiplier}x",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = { viewModel.cashOutAviator() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                "CASH OUT NOW (🪙 ${String.format("%.2f", multiplier * (betAmtText.toDoubleOrNull() ?: 10.0))})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Place Aviator Investment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = betAmtText,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) betAmtText = it },
                            label = { Text("Bet Coin Count", color = GrayText) },
                            placeholder = { Text("e.g. 50", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = LuxuryGold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                val amt = betAmtText.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    viewModel.placeAviatorBet(amt)
                                } else {
                                    viewModel.showNotification("Please enter a valid amount", isError = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("TAKE OFF MATCH (PLACE BET)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- REFER & EARN TAB ---

@Composable
fun ReferAndEarnTab(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PeopleAlt,
                        contentDescription = "Group refer logo",
                        tint = LuxuryGold,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Refer Friends & Earn Commissions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Earn 🪙 200 Coins immediately on friends' registrations (100 Coins = ₹1). Plus, earn a 3% Lifetime commission on every play (wins and losses) if your referred friend deposits coins!",
                        fontSize = 12.sp,
                        color = GrayText,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Share Code Box
        item {
            user?.let { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOUR UNIQUE REFERRAL CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF141722), RoundedCornerShape(8.dp))
                                .border(1.dp, PaleGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = player.referralCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold
                            )
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy code",
                                tint = LuxuryGold,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        viewModel.showNotification("Referral Code copied: ${player.referralCode}")
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Claimable balance board
        item {
            user?.let { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Affiliate Commissions Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total claimed rewards", fontSize = 11.sp, color = GrayText)
                                Text("🪙 ${String.format("%.2f", player.referralCommissionClaimed)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Claimable commission balance", fontSize = 11.sp, color = GrayText)
                                Text("🪙 ${String.format("%.2f", player.referralCommissionClaimable)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuxuryGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.claimReferralCommission() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            enabled = player.referralCommissionClaimable > 0.0
                        ) {
                            Text("CLAIM COMMISSIONS TO WALLET", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- WALLET TAB (DEPOSIT & WITHDRAW) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTab(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var walletMode by remember { mutableStateOf("deposit") } // deposit, withdraw, history

    // Calculate User's Total Deposit and Total Withdrawal statistics
    val userId = user?.id ?: ""
    val userTransactions = remember(transactions, userId) {
        transactions.filter { it.userId == userId && it.status == "Approved" }
    }
    val totalDeposit = remember(userTransactions) {
        userTransactions.filter { it.type == "Deposit" || it.type == "Online Deposit" }.sumOf { it.amount }
    }
    val totalWithdrawal = remember(userTransactions) {
        userTransactions.filter { it.type == "Withdrawal" }.sumOf { it.amount }
    }

    // Promo Code text field state
    var promoCodeText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Current Balance Header Card with gold glow
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(LuxuryGold.copy(alpha = 0.5f), Color.Transparent))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TOTAL WALLET BALANCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🪙 ${String.format("%.0f", user?.balance ?: 0.0)} Coins",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = LuxuryGold
                    )
                    Text(
                        text = "Equivalent Value: ₹${String.format("%.2f", (user?.balance ?: 0.0) / 100.0)} INR",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Statistics Card: Total Deposit & Total Withdrawal in the application
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Deposit Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Text("TOTAL DEPOSIT", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${String.format("%.2f", totalDeposit / 100.0)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "🪙 ${String.format("%.0f", totalDeposit)} Coins",
                            fontSize = 9.sp,
                            color = GrayText
                        )
                    }
                }

                // Total Withdrawal Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Payments, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Text("TOTAL WITHDRAWAL", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${String.format("%.2f", totalWithdrawal / 100.0)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "🪙 ${String.format("%.0f", totalWithdrawal)} Coins",
                            fontSize = 9.sp,
                            color = GrayText
                        )
                    }
                }
            }
        }

        // 3. Referral Earnings Card (claim commission)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REFERRAL EARNINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Claimable: 🪙 ${String.format("%.0f", user?.referralCommissionClaimable ?: 0.0)} Coins",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = OffWhite
                            )
                        }

                        Button(
                            onClick = { viewModel.claimReferralCommission() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            enabled = (user?.referralCommissionClaimable ?: 0.0) > 0,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("CLAIM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Promo Code / Coupons Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPLY COUPON CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeText,
                            onValueChange = { promoCodeText = it.uppercase() },
                            placeholder = { Text("e.g. EXTRA50", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF2E3344),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (promoCodeText.isNotBlank()) {
                                    viewModel.claimPromoCode(promoCodeText)
                                    promoCodeText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("APPLY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Interactive Mode Selection Toggles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("deposit" to "Add Coins", "withdraw" to "Withdrawal", "history" to "Passbook").forEach { (key, label) ->
                    val active = walletMode == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) LuxuryGold else Color.Transparent)
                            .clickable { walletMode = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.Black else GrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Interactive Sub-Tab Content
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 800.dp)
            ) {
                when (walletMode) {
                    "deposit" -> DepositTab(
                        onOnlineSubmit = { amount -> viewModel.initiateOnlinePayment(amount) }
                    )
                    "withdraw" -> {
                        val kycStatus = user?.kycStatus ?: "Not Submitted"
                        if (kycStatus != "Approved") {
                            KycFlowVerificationScreen(viewModel = viewModel)
                        } else {
                            WithdrawTab(
                                onSubmit = { amount, info -> viewModel.requestWithdraw(amount, info) }
                            )
                        }
                    }
                    "history" -> HistoryTab(
                        transactions = transactions.filter { it.userId == (user?.id ?: "") }
                    )
                }
            }
        }
    }
}

// --- NEW KYC PHOTO AND AADHAAR CAPTURE WORKFLOW ---

@Composable
fun KycFlowVerificationScreen(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    
    var aadhaarNo by remember { mutableStateOf("") }
    
    // Camera photo simulation states
    var frontPhotoText by remember { mutableStateOf("") }
    var backPhotoText by remember { mutableStateOf("") }

    val status = user?.kycStatus ?: "Not Submitted"

    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContactEmergency,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Aadhaar KYC Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Under Reserve Bank of India policy, players must approve identity verification before withdrawing real-time winnings.",
                fontSize = 11.sp,
                color = GrayText,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (status == "Pending") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF38301A), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Verification In Progress ⏳",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            fontSize = 14.sp
                        )
                        Text(
                            "Your documents are currently under review by our compliance team. Standard processing time is 5-10 minutes.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                if (status == "Rejected") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(Color(0xFF3F1D1D), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Rejected Reason: ${(user?.kycRejectReason ?: "").ifBlank { "Unreadable image proofs" }}",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = aadhaarNo,
                    onValueChange = { if (it.length <= 12 && it.all { ch -> ch.isDigit() }) aadhaarNo = it },
                    label = { Text("Enter Aadhaar Number (12 Digits)", color = GrayText) },
                    placeholder = { Text("e.g. 521098421034", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = LuxuryGold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "Aadhaar Card Camera Proofs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Mock Photo Capturing buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Front photo
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2230))
                            .border(1.dp, if (frontPhotoText.isNotBlank()) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable {
                                frontPhotoText = "Aadhaar_Front_${(10..99).random()}.jpg"
                                viewModel.showNotification("Aadhaar Card Front photo captured successfully!")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (frontPhotoText.isNotBlank()) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = if (frontPhotoText.isNotBlank()) Color(0xFF10B981) else LuxuryGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (frontPhotoText.isNotBlank()) "FRONT: SAVED" else "FRONT PHOTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (frontPhotoText.isNotBlank()) Color(0xFF10B981) else OffWhite,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Back photo
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2230))
                            .border(1.dp, if (backPhotoText.isNotBlank()) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable {
                                backPhotoText = "Aadhaar_Back_${(10..99).random()}.jpg"
                                viewModel.showNotification("Aadhaar Card Back photo captured successfully!")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (backPhotoText.isNotBlank()) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = if (backPhotoText.isNotBlank()) Color(0xFF10B981) else LuxuryGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (backPhotoText.isNotBlank()) "BACK: SAVED" else "BACK PHOTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (backPhotoText.isNotBlank()) Color(0xFF10B981) else OffWhite,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (aadhaarNo.length != 12) {
                            viewModel.showNotification("Please enter exactly 12-digit Aadhaar", isError = true)
                            return@Button
                        }
                        if (frontPhotoText.isBlank() || backPhotoText.isBlank()) {
                            viewModel.showNotification("Please capture both Aadhaar photos first", isError = true)
                            return@Button
                        }
                        viewModel.submitKyc(aadhaarNo, frontPhotoText, backPhotoText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("SUBMIT FOR COMPLIANCE REVIEW", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- PROFILE SETTINGS, SUPPORT TICKETS & PROMO CODES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    viewModel: EarnTossViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val tickets by viewModel.supportTickets.collectAsState()

    var usernameText by remember { mutableStateOf(user?.username ?: "") }
    var selectedAvatar by remember { mutableStateOf(user?.avatarUrl ?: "gamer") }

    // Change password fields
    var newPasswordText by remember { mutableStateOf("") }

    // Support ticket fields
    var ticketSubject by remember { mutableStateOf("") }
    var ticketMessage by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section 1: Customize Gamer Profile details
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "GAMER IDENTITY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Customize Avatar Badge",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Avatar Selection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val avatars = listOf(
                        "gamer" to Icons.Filled.SportsEsports,
                        "champion" to Icons.Filled.MilitaryTech,
                        "master" to Icons.Filled.LocalActivity,
                        "legend" to Icons.Filled.Stars
                    )
                    avatars.forEach { (key, icon) ->
                        val active = selectedAvatar == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) Color(0x22F59E0B) else Color(0xFF141722))
                                .border(
                                    1.dp,
                                    if (active) LuxuryGold else Color(0xFF2E3344),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAvatar = key }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) LuxuryGold else GrayText,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = key.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) LuxuryGold else OffWhite
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    label = { Text("Gamer Username", color = GrayText, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF2E3344),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        cursorColor = LuxuryGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                Button(
                    onClick = { viewModel.updateProfileInfo(usernameText, selectedAvatar) },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("SAVE PROFILE DETAILS", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                }
            }
        }

        // Section 2: Rise Ticket Support system
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SUPPORT & COMPLIANCE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Encountered any discrepancies? Create a secure support ticket for manual intervention.",
                    fontSize = 11.sp,
                    color = GrayText,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = ticketSubject,
                    onValueChange = { ticketSubject = it },
                    label = { Text("Ticket Subject", color = GrayText, fontSize = 12.sp) },
                    placeholder = { Text("e.g. Deposit Query", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF2E3344),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = ticketMessage,
                    onValueChange = { ticketMessage = it },
                    label = { Text("Detailed Message", color = GrayText, fontSize = 12.sp) },
                    placeholder = { Text("Provide reference details or transaction ID...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF2E3344),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        if (ticketSubject.isNotBlank() && ticketMessage.isNotBlank()) {
                            viewModel.raiseSupportTicket(ticketSubject, ticketMessage)
                            ticketSubject = ""
                            ticketMessage = ""
                        } else {
                            viewModel.showNotification("Subject and message are required", isError = true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("SUBMIT COMPLIANCE TICKET", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Section 3: Raised Support Tickets History
        if (tickets.isNotEmpty()) {
            Text(
                text = "ACTIVE SUPPORT TICKETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LuxuryGold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            tickets.forEach { tkt ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tkt.subject,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (tkt.status == "Open") Color(0xFF38301A) else Color(0xFF143225))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tkt.status.uppercase(),
                                    color = if (tkt.status == "Open") PaleGold else Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(text = tkt.dateTime, fontSize = 9.sp, color = GrayText, modifier = Modifier.padding(bottom = 8.dp))
                        Text(text = tkt.message, fontSize = 12.sp, color = OffWhite)

                        if (tkt.reply.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF141722), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Support Reply: ${tkt.reply}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuxuryGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Log Out Button
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ACCOUNT ACTIONS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Logout from this device securely. Your wallet data is safely encrypted on the blockchain cloud.",
                    fontSize = 11.sp,
                    color = GrayText,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("SECURELY LOGOUT", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                }
            }
        }

        // Section 5: App details version
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DograPay Arena v5.0.0-PREMIUM",
                fontSize = 11.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Secure Sandbox Architecture • End-to-End Cryptographic Ledger",
                fontSize = 9.sp,
                color = GrayText
            )
        }
    }
}

// --- TOURNAMENTS TAB ---
@Composable
fun TournamentsTab(
    tournaments: List<Tournament>,
    currentUserId: String,
    onJoin: (Tournament) -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "BGMI", "FREE FIRE", "COD MOBILE", "LUDO", "MY MATCHES")

    val filteredTournaments = tournaments.filter { match ->
        when (selectedFilter) {
            "ALL" -> true
            "MY MATCHES" -> match.participants.containsKey(currentUserId)
            else -> match.game.uppercase().contains(selectedFilter) || match.title.uppercase().contains(selectedFilter)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Section: Elite Esports Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(LuxuryGold.copy(alpha = 0.5f), Color.Transparent))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E2230),
                                    Color(0xFF141722)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE NOW",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "SEASON 4 ESPORTS CHAMPIONSHIP",
                                color = LuxuryGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dominate the Arena",
                            color = OffWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Register in premium daily tournaments to showcase your absolute tactical gameplay. Play matches, claim top placement ranks & withdraw instant winnings.",
                            color = GrayText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Section: Horizontal Filters Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT TOURNAMENT CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { item ->
                        val active = selectedFilter == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) LuxuryGold else CharcoalSurface)
                                .border(
                                    1.dp,
                                    if (active) PaleGold else Color(0xFF2E3344),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = item }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (active) Color.Black else OffWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: Matches list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE TOURNAMENTS (${filteredTournaments.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    letterSpacing = 0.5.sp
                )
                if (selectedFilter != "ALL") {
                    Text(
                        text = "Filtered by $selectedFilter",
                        fontSize = 10.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { selectedFilter = "ALL" }
                    )
                }
            }
        }

        if (filteredTournaments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = GrayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active tournaments match this category",
                            color = OffWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check other sections or create one in admin bypass.",
                            color = GrayText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTournaments) { match ->
                val isJoined = match.participants.containsKey(currentUserId)
                val totalSlots = 100
                val filledSlots = match.participants.size
                val progress = filledSlots.toFloat() / totalSlots.toFloat()
                
                // Deterministic Game parameters based on title or name for premium details
                val gameTitleUpper = match.title.uppercase()
                val format = if (gameTitleUpper.contains("SQUAD")) "SQUAD 4v4" else if (gameTitleUpper.contains("DUO")) "DUO 2v2" else "SOLO TPP"
                val gameMap = if (gameTitleUpper.contains("BGMI") || gameTitleUpper.contains("BATTLE")) "Erangel Classic" else if (gameTitleUpper.contains("FREE")) "Bermuda MAX" else "Classic Arena"
                val perKillReward = if (match.entryFee > 0) "🪙 ${String.format("%.0f", match.entryFee * 0.25)} Coins" else "🪙 5 Coins"

                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isJoined) LuxuryGold.copy(alpha = 0.6f) else Color(0xFF2E3344),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Game badge and Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = match.game.uppercase(),
                                        color = LuxuryGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = match.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OffWhite
                                )
                            }

                            // Joined Checkmark badge
                            if (isJoined) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF143225))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "JOINED",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else if (filledSlots >= totalSlots) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF3F1D1D))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "MATCH FULL",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Professional stats columns in beautiful rows
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141722), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PRIZE POOL", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Text("🪙 ${String.format("%.0f", match.prizePool)}", fontSize = 15.sp, color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Column {
                                Text("ENTRY FEE", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = PaleGold, modifier = Modifier.size(14.dp))
                                    Text(if (match.entryFee > 0) "🪙 ${String.format("%.0f", match.entryFee)}" else "FREE", fontSize = 15.sp, color = OffWhite, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Column {
                                Text("PER KILL", fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Whatshot, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Text(perKillReward, fontSize = 14.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Additional game metadata tags (Format, Map, Mode)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Icons.Filled.Map to gameMap,
                                Icons.Filled.Hub to format
                            ).forEach { (icon, text) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(Color(0xFF1E2230), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(icon, contentDescription = null, tint = GrayText, modifier = Modifier.size(12.dp))
                                    Text(text, fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress bar for filled seats / slots left
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.People, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "$filledSlots / $totalSlots slots registered",
                                        fontSize = 11.sp,
                                        color = OffWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "${totalSlots - filledSlots} spots left",
                                    fontSize = 10.sp,
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LuxuryGold,
                                trackColor = Color(0xFF141722)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Match schedule bottom section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                            Text(text = "Schedule: ${match.dateTime}", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        }

                        // Room credentials OR Join Button
                        if (isJoined) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(10.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ROOM ID & SERVER COORDINATES",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LuxuryGold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x3310B981))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "SECURED",
                                                color = Color(0xFF10B981),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    if (match.roomId.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Room ID copy row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Room ID: ${match.roomId}",
                                                color = OffWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(match.roomId))
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = "Copy Room ID",
                                                    tint = LuxuryGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Divider(color = Color(0xFF2E3344), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                                        // Password copy row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Password: ${match.roomPassword}",
                                                color = OffWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(match.roomPassword))
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = "Copy Password",
                                                    tint = LuxuryGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Match server credentials will be generated here 15 mins before starting.",
                                            color = GrayText,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onJoin(match) },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                enabled = (filledSlots < totalSlots),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (filledSlots >= totalSlots) "TOURNAMENT FULL" else "JOIN NOW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DEPOSIT TAB ---
@Composable
fun DepositTab(
    onOnlineSubmit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    
    // Live conversion helper: 100 Coins = 1 INR
    val coinsToInr = remember(amountText) {
        val amt = amountText.toDoubleOrNull() ?: 0.0
        amt / 100.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Deposit Coins into Wallet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))

            // Online Instant Deposit Option
            Text(
                text = "Deposit securely using our automated, instant checkout gateway powered by Dograapy. Zero wait times! Coins are credited immediately to your gaming wallet upon successful checkout.",
                fontSize = 11.sp,
                color = GrayText,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Text Field: Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amountText = it },
                label = { Text("Enter Deposit Amount (Coins)", color = GrayText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                leadingIcon = {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = LuxuryGold)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Real-time currency convert indicator
            if (amountText.isNotBlank() && coinsToInr > 0) {
                Text(
                    text = "💵 Payable INR Amount: ₹${String.format("%.2f", coinsToInr)}  (100 Coins = ₹1)",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onOnlineSubmit(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                enabled = (amountText.isNotBlank() && coinsToInr > 0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = "PROCEED TO INSTANT DEPOSIT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// --- WITHDRAW TAB ---
@Composable
fun WithdrawTab(
    onSubmit: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Request Coin Withdrawal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Submit your UPI address to transfer coins as cash (100 Coins = ₹1). Allowed only after identity Aadhaar KYC verification is approved.",
                fontSize = 11.sp,
                color = GrayText,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amountText = it },
                label = { Text("Withdraw Amount (Coins)", color = GrayText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Enter Target UPI ID (e.g. name@paytm)", color = GrayText) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0 && addressText.isNotBlank()) {
                        onSubmit(amt, addressText.trim())
                        amountText = ""
                        addressText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("SUBMIT WITHDRAWAL REQUEST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- HISTORY TAB (PASSBOOK) ---
@Composable
fun HistoryTab(
    transactions: List<Transaction>
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text("No passbook transactions found.", color = GrayText, fontSize = 12.sp)
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            transactions.forEach { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.type,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite
                            )
                            Text(
                                text = tx.dateTime,
                                fontSize = 9.sp,
                                color = GrayText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "Ref: ${tx.reference}",
                                fontSize = 10.sp,
                                color = GrayText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val prefix = if (tx.type == "Deposit" || tx.type == "Promo Claim" || tx.type == "Referral Claim" || tx.type == "Referral Bonus") "+" else "-"
                            val color = if (prefix == "+") Color(0xFF10B981) else Color(0xFFEF4444)
                            Text(
                                text = "$prefix 🪙 ${tx.amount}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )

                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (tx.status) {
                                            "Approved" -> Color(0xFF143225)
                                            "Pending" -> Color(0xFF38301A)
                                            else -> Color(0xFF3F1D1D)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tx.status.uppercase(),
                                    color = when (tx.status) {
                                        "Approved" -> Color(0xFF10B981)
                                        "Pending" -> Color(0xFFF59E0B)
                                        else -> Color(0xFFEF4444)
                                        },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- REFER TAB (REFER & EARN SYSTEM WITH COMMISSION TRANSFERS) ---
@Composable
fun ReferTab(viewModel: EarnTossViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val referralCode = user?.referralCode?.ifBlank { "DOGRA" + user?.id?.take(5)?.uppercase() } ?: "DOGRA101"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Premium Brand Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.linearGradient(listOf(LuxuryGold, Color(0xFF3B82F6)))),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E3A8A)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LuxuryGold)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "LIMITED OFFER",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "REFER & EARN CASH",
                                color = LuxuryGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Get 🪙 200 Free Coins",
                            color = OffWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Plus 3% lifetime match commission on every entry fee paid by your friends! Instant cashouts.",
                            color = GrayText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. My Referral Code Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR UNIQUE REFERRAL CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Code Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D1117))
                            .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = referralCode,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = LuxuryGold,
                                letterSpacing = 2.sp,
                                modifier = Modifier.testTag("referral_code_text")
                            )

                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Referral Code",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(referralCode))
                                        viewModel.showNotification("Referral Code copied to clipboard!")
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Share button
                    Button(
                        onClick = {
                            val shareBody = "Hey! Use my referral code '$referralCode' to join DograPay Arena, the premium esports platform. Get 🪙 200 welcome bonus on your first deposit! Download here: https://dograpay.arena"
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("share_referral_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHARE WITH FRIENDS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 3. Earnings Statistics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REFERRAL WALLET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CLAIMABLE COMMISSION", fontSize = 9.sp, color = GrayText)
                            Text(
                                text = "🪙 ${String.format("%.2f", user?.referralCommissionClaimable ?: 0.0)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL CLAIMED SO FAR", fontSize = 9.sp, color = GrayText)
                            Text(
                                text = "🪙 ${String.format("%.2f", user?.referralCommissionClaimed ?: 0.0)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val claimable = user?.referralCommissionClaimable ?: 0.0
                    Button(
                        onClick = { viewModel.claimReferralCommission() },
                        enabled = claimable > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            disabledContainerColor = Color(0xFF2E3344),
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text("CLAIM TO MAIN WALLET", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Step-by-Step Instructions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HOW IT WORKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    val steps = listOf(
                        Triple("1. Invite Friend", "Share your custom referral code or download link with gaming squads.", Icons.Filled.Share),
                        Triple("2. First Deposit Bonus", "When they sign up and complete their first deposit, you instantly receive 🪙 200.", Icons.Filled.CardGiftcard),
                        Triple("3. 3% Life Commission", "Every time they join any esports match, you get 3% of their entry fee instantly in your wallet.", Icons.Filled.Percent)
                    )

                    steps.forEachIndexed { idx, (title, desc, icon) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                Text(desc, fontSize = 10.sp, color = GrayText, lineHeight = 14.sp)
                            }
                        }
                        if (idx < steps.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // 5. Referral Leaderboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REFERRAL CHAMPIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold,
                            letterSpacing = 1.sp
                        )
                        Icon(Icons.Filled.Leaderboard, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val leaders = listOf(
                        Triple("Karan_Dogra", "94 invites", "🪙 18,800"),
                        Triple("thevansh513", "72 invites", "🪙 14,400"),
                        Triple("esports_manager", "48 invites", "🪙 9,600"),
                        Triple("rahul_kumar", "31 invites", "🪙 6,200")
                    )

                    leaders.forEachIndexed { idx, (name, count, prize) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == 0) LuxuryGold else Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx == 0) Color.Black else OffWhite
                                    )
                                }
                                Column {
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                    Text(count, fontSize = 9.sp, color = GrayText)
                                }
                            }
                            Text(prize, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                        if (idx < leaders.size - 1) {
                            Divider(color = Color(0xFF2E3344), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
