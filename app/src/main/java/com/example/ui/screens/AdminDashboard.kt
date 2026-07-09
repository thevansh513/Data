package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PromoCode
import com.example.data.Tournament
import com.example.data.Transaction
import com.example.data.User
import com.example.viewmodel.EarnTossViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: EarnTossViewModel
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val supportTickets by viewModel.supportTickets.collectAsState()
    val promoCodes by viewModel.promoCodesList.collectAsState()

    var adminTab by remember { mutableStateOf("approve") } // approve, tournaments, users, promo, support

    val approvedTx = remember(transactions) { transactions.filter { it.status == "Approved" } }
    val totalDepositedCoins = remember(approvedTx) { approvedTx.filter { it.type == "Deposit" || it.type == "Online Deposit" }.sumOf { it.amount } }
    val totalWithdrawnCoins = remember(approvedTx) { approvedTx.filter { it.type == "Withdrawal" }.sumOf { it.amount } }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(LuxuryGold.copy(alpha = 0.2f), Color.Transparent)
                                    )
                                )
                                .border(1.dp, LuxuryGold, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Admin Shield Logo",
                                tint = LuxuryGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Admin Master Console",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite
                            )
                            Text(
                                text = "REAL-TIME SYSTEM INTERFACE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LuxuryGold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .testTag("admin_logout_button")
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFEF4444)
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
                    selected = adminTab == "approve",
                    onClick = { adminTab = "approve" },
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Payments") },
                    label = { Text("Payments", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = adminTab == "tournaments",
                    onClick = { adminTab = "tournaments" },
                    icon = { Icon(Icons.Filled.SportsEsports, contentDescription = "Matches") },
                    label = { Text("Matches", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = adminTab == "users",
                    onClick = { adminTab = "users" },
                    icon = { Icon(Icons.Filled.People, contentDescription = "Users Hub") },
                    label = { Text("Users Hub", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = adminTab == "promo",
                    onClick = { adminTab = "promo" },
                    icon = { Icon(Icons.Filled.LocalActivity, contentDescription = "Coupons") },
                    label = { Text("Promos", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = LuxuryGold,
                        indicatorColor = LuxuryGold,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
                NavigationBarItem(
                    selected = adminTab == "support",
                    onClick = { adminTab = "support" },
                    icon = { Icon(Icons.Filled.SupportAgent, contentDescription = "System Desk") },
                    label = { Text("Desk", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
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
            // Section: Global Real-Time Statistics Ticker
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0xFF2E3344), RoundedCornerShape(0.dp))
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column {
                            Text("PENDING PAYMENTS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Text(
                                    text = "${transactions.count { it.status == "Pending" }} Reqs",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LuxuryGold
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("ACTIVE MATCHES", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text(
                                    text = "${tournaments.size} Matches",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("TOTAL GAMERS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                                Text(
                                    text = "${allUsers.size} Registered",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("ACTIVE PROMOS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEC4899)))
                                Text(
                                    text = "${promoCodes.size} Coupons",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEC4899)
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("PENDING KYC", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Text(
                                    text = "${allUsers.count { it.kycStatus == "Pending" }} Audits",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("TOTAL DEPOSITS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text(
                                    text = "🪙 ${String.format("%.0f", totalDepositedCoins)} (₹${String.format("%.0f", totalDepositedCoins / 100.0)})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text("TOTAL WITHDRAWALS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Text(
                                    text = "🪙 ${String.format("%.0f", totalWithdrawnCoins)} (₹${String.format("%.0f", totalWithdrawnCoins / 100.0)})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = adminTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "AdminTabsAnimator"
                ) { targetTab ->
                    when (targetTab) {
                        "approve" -> AdminApproveTab(
                            transactions = transactions.filter { it.status == "Pending" },
                            onApprove = { viewModel.adminApproveTransaction(it) },
                            onReject = { viewModel.adminRejectTransaction(it) }
                        )
                        "tournaments" -> AdminTournamentsTab(
                            tournaments = tournaments,
                            onCreateTournament = { title, game, fee, prize, schedule ->
                                viewModel.adminCreateTournament(title, game, fee, prize, schedule)
                            },
                            onUpdateRoom = { tId, rId, pass ->
                                viewModel.adminUpdateRoomCredentials(tId, rId, pass)
                            },
                            onDeleteTournament = { tId ->
                                viewModel.adminDeleteTournament(tId)
                            }
                        )
                        "users" -> AdminUsersTab(
                            users = allUsers,
                            onAdjustCoins = { userId, delta, note ->
                                viewModel.adminAdjustUserCoins(userId, delta, note)
                            }
                        )
                        "promo" -> AdminPromoTab(
                            promoCodes = promoCodes,
                            onCreatePromo = { code, coins ->
                                viewModel.adminCreatePromoCode(code, coins)
                            }
                        )
                        "support" -> AdminSupportAndDeskTab(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: FINANCIAL TRANSACTIONS APPROVALS
// ==========================================
@Composable
fun AdminApproveTab(
    transactions: List<Transaction>,
    onApprove: (Transaction) -> Unit,
    onReject: (Transaction) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("All Clear! ✅", color = OffWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No pending coin deposit or withdrawal requests.", color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "PENDING FINANCIAL CLEARANCE (${transactions.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(transactions) { tx ->
                val isDeposit = tx.type == "Deposit"

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDeposit) Color(0xFF133224) else Color(0xFF4C1D1D))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tx.type.uppercase(),
                                        color = if (isDeposit) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "🪙 ${String.format("%.0f", tx.amount)} Coins",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite
                                )
                            }

                            Text(
                                text = tx.dateTime,
                                fontSize = 10.sp,
                                color = GrayText
                            )
                        }

                        Divider(color = Color(0xFF2E3344), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "Gamer/User ID:",
                            fontSize = 9.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = tx.userId,
                            fontSize = 12.sp,
                            color = OffWhite,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = if (isDeposit) "UPI TRANSACTION ID (UTR / REF):" else "WITHDRAWAL METHOD / UPI TARGET ID:",
                            fontSize = 9.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tx.reference.ifBlank { "N/A" },
                                fontSize = 13.sp,
                                color = LuxuryGold,
                                fontWeight = FontWeight.Bold
                            )
                            if (tx.reference.isNotBlank()) {
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(tx.reference)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Reference Code",
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onReject(tx) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3F1D1D),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("REJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { onApprove(tx) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF143225),
                                    contentColor = Color(0xFF10B981)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("APPROVE & CREDIT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: MATCH TOURNAMENTS & COORDINATES
// ==========================================
@Composable
fun AdminTournamentsTab(
    tournaments: List<Tournament>,
    onCreateTournament: (String, String, Double, Double, String) -> Unit,
    onUpdateRoom: (String, String, String) -> Unit,
    onDeleteTournament: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("") }
    var entryFeeText by remember { mutableStateOf("") }
    var prizePoolText by remember { mutableStateOf("") }
    var scheduleText by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Tournament Creation Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(listOf(LuxuryGold.copy(alpha = 0.5f), Color.Transparent))
                        ),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Publish E-Sports Match",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Match Title (e.g. Squad Showdown Clash)", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = game,
                        onValueChange = { game = it },
                        label = { Text("Game Name (e.g. BGMI, Free Fire, COD)", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = entryFeeText,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) entryFeeText = it },
                            label = { Text("Entry Fee (Coins)", color = GrayText) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = prizePoolText,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) prizePoolText = it },
                            label = { Text("Prize Pool (Coins)", color = GrayText) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = scheduleText,
                        onValueChange = { scheduleText = it },
                        label = { Text("Schedule Description (e.g. Today 08:30 PM)", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    Button(
                        onClick = {
                            val fee = entryFeeText.toDoubleOrNull() ?: 0.0
                            val prize = prizePoolText.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && game.isNotBlank() && scheduleText.isNotBlank() && prize > 0.0) {
                                onCreateTournament(title, game, fee, prize, scheduleText)
                                title = ""
                                game = ""
                                entryFeeText = ""
                                prizePoolText = ""
                                scheduleText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank() && game.isNotBlank() && scheduleText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("PUBLISH TOURNAMENT MATCH", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }

        // Live Matches Update & Delete Row Header
        item {
            Text(
                text = "ACTIVE MATCH SERVER DETAILS (${tournaments.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LuxuryGold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (tournaments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active tournament matches created.", color = GrayText, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(tournaments) { match ->
                var localRoomId by remember(match.roomId) { mutableStateOf(match.roomId) }
                var localPassword by remember(match.roomPassword) { mutableStateOf(match.roomPassword) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = match.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OffWhite
                                )
                                Text(
                                    text = "${match.game.uppercase()} • ID: ${match.id}",
                                    fontSize = 11.sp,
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Delete Action Icon
                            IconButton(
                                onClick = { onDeleteTournament(match.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AEF4444))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Tournament",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column {
                                Text("ENTRY FEE", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
                                Text("🪙 ${String.format("%.0f", match.entryFee)}", fontSize = 13.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text("PRIZE POOL", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
                                Text("🪙 ${String.format("%.0f", match.prizePool)}", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("PARTICIPANTS", fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
                                Text("${match.participants.size} Players", fontSize = 13.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color(0xFF2E3344), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Room Credential Fields
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = localRoomId,
                                onValueChange = { localRoomId = it },
                                label = { Text("Game Room ID", color = GrayText, fontSize = 9.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite
                                ),
                                modifier = Modifier.weight(1.2f)
                            )

                            OutlinedTextField(
                                value = localPassword,
                                onValueChange = { localPassword = it },
                                label = { Text("Room Password", color = GrayText, fontSize = 9.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = { onUpdateRoom(match.id, localRoomId, localPassword) },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("BROADCAST SERVER CREDENTIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: USER MANAGEMENT & BALANCE ADJUSTMENTS
// ==========================================
@Composable
fun AdminUsersTab(
    users: List<User>,
    onAdjustCoins: (String, Double, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserIdToAdjust by remember { mutableStateOf<String?>(null) }
    var adjustDeltaString by remember { mutableStateOf("") }
    var adjustNote by remember { mutableStateOf("") }

    val filteredUsers = users.filter {
        it.email.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "GAMER PROFILE MANAGEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Gamer ID, email, or username...", color = GrayText) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = LuxuryGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF2E3344),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No gamers match your search query.", color = GrayText, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredUsers) { player ->
                val isAdjusting = selectedUserIdToAdjust == player.id

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isAdjusting) LuxuryGold else Color(0xFF2E3344),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (player.role == "Admin") Icons.Filled.Shield else Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = if (player.role == "Admin") LuxuryGold else OffWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = player.username.ifBlank { "Unregistered Gamer" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OffWhite
                                    )
                                    Text(
                                        text = player.email,
                                        fontSize = 11.sp,
                                        color = GrayText
                                    )
                                }
                            }

                            // Balance display badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141722))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🪙 ${String.format("%.0f", player.balance)}",
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status pills row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Role Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (player.role == "Admin") Color(0xFF3F2B04) else Color(0xFF1E2230))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = player.role.uppercase(),
                                    color = if (player.role == "Admin") LuxuryGold else GrayText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // KYC Status Badge
                            val kycBg = when (player.kycStatus) {
                                "Approved" -> Color(0xFF143225)
                                "Pending" -> Color(0xFF3F2B04)
                                "Rejected" -> Color(0xFF3F1D1D)
                                else -> Color(0xFF1E2230)
                            }
                            val kycColor = when (player.kycStatus) {
                                "Approved" -> Color(0xFF10B981)
                                "Pending" -> Color(0xFFF59E0B)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> GrayText
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(kycBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "KYC: ${player.kycStatus.uppercase()}",
                                    color = kycColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Expandable coin adjust section
                        if (isAdjusting) {
                            Divider(color = Color(0xFF2E3344), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "ADJUST COIN WALLET BALANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = adjustDeltaString,
                                    onValueChange = { adjustDeltaString = it },
                                    label = { Text("Coins Delta (e.g. +500 or -200)", color = GrayText, fontSize = 9.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite
                                    ),
                                    modifier = Modifier.weight(1.2f)
                                )

                                OutlinedTextField(
                                    value = adjustNote,
                                    onValueChange = { adjustNote = it },
                                    label = { Text("Audit Note", color = GrayText, fontSize = 9.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedUserIdToAdjust = null
                                        adjustDeltaString = ""
                                        adjustNote = ""
                                    },
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val delta = adjustDeltaString.toDoubleOrNull()
                                        if (delta != null && adjustNote.isNotBlank()) {
                                            onAdjustCoins(player.id, delta, adjustNote)
                                            selectedUserIdToAdjust = null
                                            adjustDeltaString = ""
                                            adjustNote = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = adjustDeltaString.toDoubleOrNull() != null && adjustNote.isNotBlank(),
                                    modifier = Modifier.weight(1.4f).height(38.dp)
                                ) {
                                    Text("CONFIRM ADJUST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    selectedUserIdToAdjust = player.id
                                    adjustDeltaString = ""
                                    adjustNote = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230), contentColor = LuxuryGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp).border(0.5.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("MANAGE WALLET BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: PROMO CODE CAMPAIGNS MANAGEMENT
// ==========================================
@Composable
fun AdminPromoTab(
    promoCodes: List<PromoCode>,
    onCreatePromo: (String, Double) -> Unit
) {
    var promoCodeInput by remember { mutableStateOf("") }
    var promoValueInput by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Form to create promo code
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFFEC4899).copy(alpha = 0.5f), Color.Transparent))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.LocalActivity, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        Text(
                            text = "Launch Promotional Coupon",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it.uppercase() },
                        label = { Text("Coupon Code (e.g. BONANZA500)", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = promoValueInput,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) promoValueInput = it },
                        label = { Text("Grant Bonus Coins (e.g. 150)", color = GrayText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    Button(
                        onClick = {
                            val coins = promoValueInput.toDoubleOrNull() ?: 0.0
                            if (promoCodeInput.isNotBlank() && coins > 0.0) {
                                onCreatePromo(promoCodeInput.trim(), coins)
                                promoCodeInput = ""
                                promoValueInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        enabled = promoCodeInput.isNotBlank() && promoValueInput.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("PUBLISH PROMO COUPON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Promo Codes list header
        item {
            Text(
                text = "ACTIVE PROMO COUPONS (${promoCodes.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (promoCodes.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active promo code campaigns active.", color = GrayText, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(promoCodes) { coupon ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEC4899))
                                )
                                Text(
                                    text = coupon.code,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Grants: 🪙 ${String.format("%.0f", coupon.value)} Coins",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Claim stats
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF141722))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${coupon.claimedBy.size} Claims",
                                color = GrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: COMPLIANCE & SUPPORT DESK (KYC & TICKETS)
// ==========================================
@Composable
fun AdminSupportAndDeskTab(
    viewModel: EarnTossViewModel
) {
    var subTab by remember { mutableStateOf("kyc") } // kyc, tickets

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Simple Sub Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF141722))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "kyc") LuxuryGold else Color.Transparent)
                    .clickable { subTab = "kyc" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PENDING KYC",
                    color = if (subTab == "kyc") Color.Black else OffWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "tickets") LuxuryGold else Color.Transparent)
                    .clickable { subTab = "tickets" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SUPPORT DESK",
                    color = if (subTab == "tickets") Color.Black else OffWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (subTab == "kyc") {
            AdminKycHubTab(viewModel = viewModel)
        } else {
            AdminTicketsTab(viewModel = viewModel)
        }
    }
}

@Composable
fun AdminKycHubTab(
    viewModel: EarnTossViewModel
) {
    val users by viewModel.allUsers.collectAsState()
    val pendingKycUsers = users.filter { it.kycStatus == "Pending" }

    if (pendingKycUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("KYC Audited! ✅", color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No pending player identity verification documents.", color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(pendingKycUsers) { player ->
                var rejectReasonInput by remember { mutableStateOf("") }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gamer ID: ${player.username.ifBlank { player.email }}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )
                        Text(
                            text = "Email: ${player.email}",
                            fontSize = 12.sp,
                            color = GrayText
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Aadhaar Card No: ${player.aadhaar}",
                            fontSize = 13.sp,
                            color = OffWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Submitted Image Proofs:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF141722), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text("FRONT: ${player.kycAadhaarFront}", fontSize = 10.sp, color = OffWhite)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF141722), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text("BACK: ${player.kycAadhaarBack}", fontSize = 10.sp, color = OffWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = rejectReasonInput,
                            onValueChange = { rejectReasonInput = it },
                            placeholder = { Text("Reason (only required if rejecting)", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminRejectKyc(player.id, rejectReasonInput.ifBlank { "Unreadable photo details" }) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F1D1D), contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.adminApproveKyc(player.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF143225), contentColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("APPROVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTicketsTab(
    viewModel: EarnTossViewModel
) {
    val tickets by viewModel.supportTickets.collectAsState()

    if (tickets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Support Resolved! ✅", color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No open support ticket requests from players.", color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(tickets) { tkt ->
                var replyInput by remember { mutableStateOf("") }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
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
                            Text(
                                text = tkt.subject,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (tkt.status == "Open") Color(0xFF3F1D1D) else Color(0xFF143225))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tkt.status.uppercase(),
                                    color = if (tkt.status == "Open") Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "From: ${tkt.userEmail}  •  ${tkt.dateTime}",
                            fontSize = 11.sp,
                            color = GrayText,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        Text(
                            text = "Message: ${tkt.message}",
                            fontSize = 12.sp,
                            color = OffWhite,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (tkt.reply.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF141722), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Previous Reply: ${tkt.reply}",
                                    fontSize = 11.sp,
                                    color = LuxuryGold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = replyInput,
                            onValueChange = { replyInput = it },
                            placeholder = { Text("Type reply coordinates message...", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = {
                                if (replyInput.isNotBlank()) {
                                    viewModel.adminReplyTicket(tkt.id, replyInput)
                                    replyInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("SEND SUPPORT REPLY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
