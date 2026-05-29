package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.KiryanaViewModel
import com.example.ui.Lang
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current.applicationContext as Application
                val vmFactory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return KiryanaViewModel(context) as T
                    }
                }
                val vm: KiryanaViewModel = viewModel(factory = vmFactory)
                KiryanaMainScreen(vm)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KiryanaMainScreen(vm: KiryanaViewModel) {
    val context = LocalContext.current
    val isUrdu by vm.isUrdu.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
    val currentTab by vm.currentTab.collectAsStateWithLifecycle()
    val uiMessage by vm.uiMessage.collectAsStateWithLifecycle()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isUrdu) "کھاتہ پرو ڈیش بورڈ" else "KIRYANA PRO LEDGER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            val mainTitle = when (currentTab) {
                                "DASHBOARD" -> if (isUrdu) "خلاصہ رپورٹ" else "Summary"
                                "INVENTORY" -> if (isUrdu) "اسٹاک لسٹ" else "Inventory"
                                "POS" -> if (isUrdu) "کیش کاونٹر" else "POS Register"
                                "LEDGER" -> if (isUrdu) "کھاتہ بک" else "Ledger"
                                "EXPENSES" -> if (isUrdu) "خرید دکان" else "Expenses"
                                else -> "Summary"
                            }
                            Text(
                                text = mainTitle,
                                style = MaterialTheme.typography.displayLarge,
                                fontSize = 32.sp,
                                lineHeight = 38.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { vm.toggleLanguage() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isUrdu) "English" else "اردو",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { vm.toggleRole() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isAdmin) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.12f),
                                    contentColor = if (isAdmin) Color.White else MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Outlined.Person,
                                    contentDescription = "Toggle Role"
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        Triple("DASHBOARD", Icons.Filled.Dashboard, "dashboard"),
                        Triple("INVENTORY", Icons.Filled.Inventory, "inventory"),
                        Triple("POS", Icons.Filled.ShoppingCart, "pos"),
                        Triple("LEDGER", Icons.Filled.ImportContacts, "ledger"),
                        Triple("EXPENSES", Icons.Filled.AccountBalanceWallet, "expenses")
                    )

                    items.forEach { (tab, icon, labelKey) ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { vm.selectTab(tab) },
                            icon = { Icon(icon, contentDescription = tab) },
                            label = {
                                Text(
                                    text = Lang.get(labelKey, isUrdu).uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "DASHBOARD" -> DashboardScreen(vm, isUrdu, isAdmin)
                "INVENTORY" -> InventoryScreen(vm, isUrdu, isAdmin)
                "POS" -> POSScreen(vm, isUrdu, isAdmin)
                "LEDGER" -> LedgerScreen(vm, isUrdu, isAdmin)
                "EXPENSES" -> ExpensesScreen(vm, isUrdu, isAdmin)
            }
        }
    }
}

// ======================== DASHBOARD SCREEN ========================
@Composable
fun DashboardScreen(vm: KiryanaViewModel, isUrdu: Boolean, isAdmin: Boolean) {
    val prods by vm.products.collectAsStateWithLifecycle()
    val custs by vm.customers.collectAsStateWithLifecycle()
    val trxs by vm.transactions.collectAsStateWithLifecycle()
    val exps by vm.expenses.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current
    var showBackupDialog by remember { mutableStateOf(false) }

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todaySales = trxs.filter { it.timestamp >= startOfToday && it.type != "UDHAAR_CREDIT" }.sumOf { it.amount }
    val todayCostOfGoods = trxs.filter { it.timestamp >= startOfToday && it.type != "UDHAAR_CREDIT" }.sumOf { it.costOfGoods }
    val todayExpenses = exps.filter { it.timestamp >= startOfToday }.sumOf { it.amount }
    val todayNetProfit = (todaySales - todayCostOfGoods - todayExpenses).coerceAtLeast(0.0)

    val currentMonthStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    val monthlySales = trxs.filter { it.timestamp >= currentMonthStart && it.type != "UDHAAR_CREDIT" }.sumOf { it.amount }

    val totalPendingUdhaar = custs.sumOf { it.totalUdhaar }
    val lowStockItems = prods.filter { it.stockQuantity <= it.lowStockThreshold }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isUrdu) "خوش آمدید! دکان کا حساب کتاب چالو ہے" else "Welcome! Your Kiryana ledger is active.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, d MMMM yyyy", if (isUrdu) Locale("ur") else Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = Lang.get("today_sales", isUrdu),
                        value = "Rs. ${formatPKR(todaySales)}",
                        icon = Icons.Default.TrendingUp,
                        containerColor = KhataMintSecondary,
                        contentColor = KhataGreenPrimary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = Lang.get("today_profit", isUrdu),
                        value = "Rs. ${formatPKR(todayNetProfit)}",
                        icon = Icons.Default.Savings,
                        containerColor = Color(0xFFFEF5E7),
                        contentColor = Color(0xFFB0730F)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = Lang.get("total_credit", isUrdu),
                        value = "Rs. ${formatPKR(totalPendingUdhaar)}",
                        icon = Icons.Default.CardMembership,
                        containerColor = Color(0xFFFDE8E8),
                        contentColor = Color(0xFFC53030)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = Lang.get("monthly_sales", isUrdu),
                        value = "Rs. ${formatPKR(monthlySales)}",
                        icon = Icons.Default.Assessment,
                        containerColor = Color(0xFFEBF5FB),
                        contentColor = Color(0xFF1F618D)
                    )
                }
            }
        }

        item {
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Lang.get("p_and_l", isUrdu),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    PLRow(label = Lang.get("revenue", isUrdu), value = "Rs. ${formatPKR(todaySales)}", isUrdu = isUrdu)
                    PLRow(label = Lang.get("cogs", isUrdu), value = "- Rs. ${formatPKR(todayCostOfGoods)}", isDark = true, isUrdu = isUrdu)
                    PLRow(label = Lang.get("total_expenses", isUrdu), value = "- Rs. ${formatPKR(todayExpenses)}", isDark = true, isUrdu = isUrdu)
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Lang.get("net_profit", isUrdu),
                            fontWeight = FontWeight.Bold,
                            color = KhataGreenPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Rs. ${formatPKR(todayNetProfit)}",
                            fontWeight = FontWeight.Bold,
                            color = KhataGreenPrimary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC53030))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${Lang.get("stock_alerts_title", isUrdu)} (${lowStockItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC53030)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (lowStockItems.isEmpty()) {
                        Text(
                            text = Lang.get("no_stock_alerts", isUrdu),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        lowStockItems.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${Lang.get("stock", isUrdu)}: ${item.stockQuantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC53030)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Lang.get("backup_restore", isUrdu),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Lang.get("backup_desc", isUrdu),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val json = vm.exportBackupJson()
                                clipboardManager.setText(AnnotatedString(json))
                                vm.showMessage(if (isUrdu) "بیک آپ کاپی ہو گیا! اسے کہیں محفوظ کر لیں۔" else "Backup copied to clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Lang.get("create_backup", isUrdu), fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Lang.get("restore_data", isUrdu), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        var backupInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(Lang.get("restore_data", isUrdu), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isUrdu) "نیچے اپنا پرانا بیک آپ پیسٹ کریں:" else "Paste your backup text below:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = backupInput,
                        onValueChange = { backupInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{ ... }") },
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = vm.importRestoreJson(backupInput)
                        if (success) {
                            showBackupDialog = false
                        }
                    }
                ) {
                    Text(Lang.get("save", isUrdu))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text(Lang.get("cancel", isUrdu))
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp), // modern rounded-3xl look
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            // High-contrast subtle tag matching "+12% Today" and "آج کی ریکوری"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(contentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val isRedCol = containerColor == BoldRose500 || containerColor == Color(0xFFFDE8E8) || containerColor == Color(0xFFC53030)
                Text(
                    text = if (isRedCol) 
                        (if (title.contains("Credit", ignoreCase = true) || title.contains("کھاتا") || title.contains("ادھار")) "آج کی ریکوری" else "Urgent")
                         else "+12% Today",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun PLRow(label: String, value: String, isDark: Boolean = false, isUrdu: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isDark) Color.Gray else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.DarkGray else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================== INVENTORY SCREEN ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(vm: KiryanaViewModel, isUrdu: Boolean, isAdmin: Boolean) {
    val prods by vm.filteredProducts.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    val search by vm.inventorySearchQuery.collectAsStateWithLifecycle()
    val activeCat by vm.selectedCategoryFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedForEdit by remember { mutableStateOf<Product?>(null) }
    var barcodeSimInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = search,
                onValueChange = { vm.inventorySearchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Lang.get("search_hint", isUrdu)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { vm.inventorySearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcodeSimInput,
                    onValueChange = { barcodeSimInput = it },
                    placeholder = { Text(Lang.get("scan_sim", isUrdu)) },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
                Button(
                    onClick = {
                        if (barcodeSimInput.isNotEmpty()) {
                            vm.handleBarcodeScan(barcodeSimInput)
                            barcodeSimInput = ""
                        }
                    },
                    modifier = Modifier.height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isUrdu) "اسکین" else "Scan")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = activeCat == "All",
                        onClick = { vm.selectedCategoryFilter.value = "All" },
                        label = { Text(if (isUrdu) "سب آئٹم" else "All Items") }
                    )
                }
                items(cats) { cat ->
                    FilterChip(
                        selected = activeCat == cat,
                        onClick = { vm.selectedCategoryFilter.value = cat },
                        label = { Text(cat) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (prods.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isUrdu) "کوئی اشیاء نہیں ملیں" else "No items found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(prods) { prod ->
                        val isLow = prod.stockQuantity <= prod.lowStockThreshold
                        val isOut = prod.stockQuantity <= 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isAdmin) {
                                        selectedForEdit = prod
                                        showAddDialog = true
                                    } else {
                                        vm.showMessage("Only store admins can modify product data!")
                                    }
                                },
                            elevation = CardDefaults.cardElevation(1.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${Lang.get("category", isUrdu)}: ${prod.category} | ${Lang.get("barcode", isUrdu)}: ${prod.barcode}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when {
                                                    isOut -> Color(0xFFFADBD8)
                                                    isLow -> Color(0xFFFDEBD0)
                                                    else -> Color(0xFFD5F5E3)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${Lang.get("stock", isUrdu)}: ${prod.stockQuantity}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isOut -> Color(0xFFC0392B)
                                                isLow -> Color(0xFFD35400)
                                                else -> Color(0xFF196F3D)
                                            }
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${Lang.get("purchase_price", isUrdu)}: Rs. ${prod.purchasePrice}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${Lang.get("selling_price", isUrdu)}: Rs. ${prod.sellingPrice}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KhataGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = {
                    selectedForEdit = null
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    }

    if (showAddDialog) {
        ProductFormDialog(
            isUrdu = isUrdu,
            productToEdit = selectedForEdit,
            categoriesList = cats.filter { it != "All" },
            onDismiss = { showAddDialog = false },
            onConfirm = { id, barcode, name, cat, buy, sell, stock, minTrigger ->
                vm.saveProduct(id, barcode, name, cat, buy, sell, stock, minTrigger)
                showAddDialog = false
            },
            onDelete = { prod ->
                vm.deleteProduct(prod)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProductFormDialog(
    isUrdu: Boolean,
    productToEdit: Product?,
    categoriesList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, String, Double, Double, Int, Int) -> Unit,
    onDelete: (Product) -> Unit
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "General") }
    var buyPrice by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "") }
    var sellPrice by remember { mutableStateOf(productToEdit?.sellingPrice?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(productToEdit?.stockQuantity?.toString() ?: "") }
    var minLimit by remember { mutableStateOf(productToEdit?.lowStockThreshold?.toString() ?: "5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (productToEdit == null) Lang.get("add_item", isUrdu) else Lang.get("edit_item", isUrdu),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Lang.get("item_name", isUrdu)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text(Lang.get("barcode", isUrdu)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (isUrdu) "خود کار طریقے سے بنائیں" else "Leave empty to auto-generate") }
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(Lang.get("category", isUrdu)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Grocery, Beverages, Bakery...") }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it },
                        label = { Text(if (isUrdu) "خرید قیمت" else "Purchase") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        label = { Text(if (isUrdu) "فروخت قیمت" else "Sale") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text(if (isUrdu) "اسٹاک تعداد" else "Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minLimit,
                        onValueChange = { minLimit = it },
                        label = { Text(if (isUrdu) "الرٹ حد" else "Alert Limit") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (productToEdit != null) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                        onClick = { onDelete(productToEdit) }
                    ) {
                        Text(Lang.get("delete", isUrdu))
                    }
                }
                Button(
                    onClick = {
                        val parsedBuy = buyPrice.toDoubleOrNull() ?: 0.0
                        val parsedSell = sellPrice.toDoubleOrNull() ?: 0.0
                        val parsedStock = stockQuantity.toIntOrNull() ?: 0
                        val parsedAlert = minLimit.toIntOrNull() ?: 5
                        onConfirm(
                            productToEdit?.id ?: 0,
                            barcode,
                            name,
                            category,
                            parsedBuy,
                            parsedSell,
                            parsedStock,
                            parsedAlert
                        )
                    }
                ) {
                    Text(Lang.get("save", isUrdu))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Lang.get("cancel", isUrdu))
            }
        }
    )
}

// ======================== POS POINT OF SALE ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(vm: KiryanaViewModel, isUrdu: Boolean, isAdmin: Boolean) {
    val prods by vm.products.collectAsStateWithLifecycle()
    val cart by vm.cart.collectAsStateWithLifecycle()
    val discount by vm.discount.collectAsStateWithLifecycle()
    val custs by vm.customers.collectAsStateWithLifecycle()
    val selectedCustForSale by vm.selectedCustomerForSale.collectAsStateWithLifecycle()
    val checkoutNote by vm.checkoutNote.collectAsStateWithLifecycle()
    val isCreditSale by vm.isSaleCredit.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var barcodeInput by remember { mutableStateOf("") }
    var quickSearch by remember { mutableStateOf("") }

    val subtotal = cart.sumOf { it.product.sellingPrice * it.quantity }
    val finalPayable = (subtotal - discount).coerceAtLeast(0.0)

    var showCustomerSelectDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastReceiptText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                elevation = CardDefaults.cardElevation(1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(Lang.get("scan_sim", isUrdu), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = { barcodeInput = it },
                            placeholder = { Text(if (isUrdu) "بارکوڈ اسکین یا ٹائپ کریں..." else "Scan/Type barcode") },
                            leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            maxLines = 1
                        )
                        Button(
                            onClick = {
                                if (barcodeInput.isNotEmpty()) {
                                    vm.handleBarcodeScan(barcodeInput)
                                    barcodeInput = ""
                                }
                            }
                        ) {
                            Text(if (isUrdu) "انٹر" else "Enter")
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = quickSearch,
                onValueChange = { quickSearch = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isUrdu) "آئٹم لسٹ سے فوری تلاش کریں..." else "Search product to add...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(10.dp),
                maxLines = 1
            )
        }

        item {
            val itemsForGrid = prods.filter {
                quickSearch.isEmpty() ||
                it.name.contains(quickSearch, ignoreCase = true) ||
                it.category.contains(quickSearch, ignoreCase = true) ||
                it.barcode.contains(quickSearch)
            }

            if (itemsForGrid.isNotEmpty()) {
                Text(if (isUrdu) "آئٹم منتخب کریں:" else "Select item to add:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    itemsForGrid.take(6).forEach { prod ->
                        Button(
                            onClick = { vm.addProductToCart(prod) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${Lang.get("stock", isUrdu)}: ${prod.stockQuantity}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("Rs. ${prod.sellingPrice}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Lang.get("cart", isUrdu), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = KhataGreenPrimary)
                if (cart.isNotEmpty()) {
                    TextButton(onClick = { vm.clearCart() }) {
                        Text(if (isUrdu) "کارٹ صاف کریں" else "Clear Cart", color = Color(0xFFC53030))
                    }
                }
            }
        }

        if (cart.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = Lang.get("cart_empty", isUrdu),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(cart) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${item.quantity} x Rs. ${item.product.sellingPrice} = Rs. ${formatPKR(item.product.sellingPrice * item.quantity)}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { vm.removeProductFromCart(item.product) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Gray)
                        }
                        Text(text = "${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { vm.addProductToCart(item.product) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = KhataGreenPrimary)
                        }
                    }
                }
            }
        }

        if (cart.isNotEmpty()) {
            item {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BillingStatsRow(label = Lang.get("total_bill", isUrdu), value = "Rs. ${formatPKR(subtotal)}")
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Lang.get("discount", isUrdu), fontSize = 13.sp)
                            OutlinedTextField(
                                value = if (discount == 0.0) "" else discount.toString(),
                                onValueChange = { vm.discount.value = it.toDoubleOrNull() ?: 0.0 },
                                modifier = Modifier.width(100.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                                shape = RoundedCornerShape(6.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                maxLines = 1
                            )
                        }

                        BillingStatsRow(label = Lang.get("payable_amount", isUrdu), value = "Rs. ${formatPKR(finalPayable)}", isBold = true, valueColor = KhataGreenPrimary)

                        Divider()

                        Text(Lang.get("payment_type", isUrdu), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { vm.isSaleCredit.value = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (!isCreditSale) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.1f),
                                    contentColor = if (!isCreditSale) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                Text(Lang.get("cash_sale", isUrdu), fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = { vm.isSaleCredit.value = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isCreditSale) Color(0xFFC53030) else Color.Gray.copy(alpha = 0.1f),
                                    contentColor = if (isCreditSale) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                Text(Lang.get("credit_sale", isUrdu), fontSize = 11.sp)
                            }
                        }

                        if (isCreditSale) {
                            Button(
                                onClick = { showCustomerSelectDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030))
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(selectedCustForSale?.name ?: Lang.get("select_credit_customer", isUrdu))
                            }
                        }

                        OutlinedTextField(
                            value = checkoutNote,
                            onValueChange = { vm.checkoutNote.value = it },
                            placeholder = { Text(if (isUrdu) "کوئی نوٹ یا گاہک کا نام..." else "Receipt note or walk-in name") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                val custLabel = if (isCreditSale) selectedCustForSale?.name ?: "Credit" else checkoutNote.ifEmpty { Lang.get("walk_in_customer", isUrdu) }
                                val builder = StringBuilder()
                                builder.append("${Lang.get("receipt_header", isUrdu)}\n")
                                builder.append("${if (isUrdu) "کسٹمر" else "Cust"}: $custLabel\n")
                                builder.append("${if (isUrdu) "تاریخ" else "Date"}: $stamp\n")
                                builder.append("----------------------------\n")
                                cart.forEach { item ->
                                    builder.append("${item.quantity}x ${item.product.name} = Rs. ${item.product.sellingPrice * item.quantity}\n")
                                }
                                builder.append("----------------------------\n")
                                builder.append("${Lang.get("total_bill", isUrdu)}: Rs. ${formatPKR(subtotal)}\n")
                                if (discount > 0.0) {
                                    builder.append("${Lang.get("discount", isUrdu)}: Rs. ${formatPKR(discount)}\n")
                                }
                                builder.append("${Lang.get("payable_amount", isUrdu)}: Rs. ${formatPKR(finalPayable)}\n")
                                builder.append("----------------------------\n")
                                builder.append(if (isUrdu) "شکریہ! دوبارہ تشریف لائیں۔\n" else "Thank you for shopping!\n")
                                
                                lastReceiptText = builder.toString()
                                vm.performPOSCheckout()
                                showReceiptDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KhataGreenPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Lang.get("complete_sale", isUrdu), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showReceiptDialog) {
        val clipboardManager = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showReceiptDialog = false },
            title = { Text(if (isUrdu) "فروخت کی سلپ" else "Receipt Slip", fontWeight = FontWeight.Bold) },
            text = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = lastReceiptText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(lastReceiptText))
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(if (isUrdu) "کاپی کریں" else "Copy Text")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, lastReceiptText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Slip via"))
                            showReceiptDialog = false
                        }
                    ) {
                        Text(Lang.get("whatsapp_receipt", isUrdu))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceiptDialog = false }) {
                    Text(if (isUrdu) "بند کریں" else "Close")
                }
            }
        )
    }

    if (showCustomerSelectDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerSelectDialog = false },
            title = { Text(Lang.get("select_credit_customer", isUrdu)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(custs) { cust ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.selectedCustomerForSale.value = cust
                                    showCustomerSelectDialog = false
                                }
                                .background(
                                    if (selectedCustForSale?.id == cust.id) MaterialTheme.colorScheme.secondary else Color.LightGray.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cust.name, fontWeight = FontWeight.Bold)
                            Text("Rs. ${formatPKR(cust.totalUdhaar)}", color = Color(0xFFC53030))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerSelectDialog = false }) {
                    Text(Lang.get("cancel", isUrdu))
                }
            }
        )
    }
}

@Composable
fun BillingStatsRow(label: String, value: String, isBold: Boolean = false, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = if (isBold) 16.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}

// ======================== UDHAAR LEDGER KHATA ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(vm: KiryanaViewModel, isUrdu: Boolean, isAdmin: Boolean) {
    val custs by vm.filteredCustomers.collectAsStateWithLifecycle()
    val search by vm.ledgerSearchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddCustDialog by remember { mutableStateOf(false) }
    var selectedCustomerDetail by remember { mutableStateOf<Customer?>(null) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    if (selectedCustomerDetail == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { vm.ledgerSearchQuery.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(Lang.get("search_hint", isUrdu)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Lang.get("total_credit", isUrdu),
                                fontSize = 11.sp,
                                color = Color(0xFF9B1C1C)
                            )
                            Text(
                                text = "Rs. ${formatPKR(custs.sumOf { it.totalUdhaar })}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9B1C1C)
                            )
                        }
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = Color(0xFF9B1C1C))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (custs.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isUrdu) "کوئی گاہک رجسٹرڈ نہیں ہے" else "No customers found", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(custs) { cust ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCustomerDetail = cust },
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            text = "${Lang.get("phone", isUrdu)}: ${cust.phone} | ${cust.address}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Rs. ${formatPKR(cust.totalUdhaar)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cust.totalUdhaar > 0) Color(0xFFC53030) else Color(0xFF196F3D)
                                        )
                                        Text(
                                            text = if (cust.totalUdhaar > 0) Lang.get("outstanding_udhaar", isUrdu) else "بے باق (Clear)",
                                            fontSize = 9.sp,
                                            color = if (cust.totalUdhaar > 0) Color(0xFFC53030) else Color(0xFF196F3D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddCustDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    } else {
        val cust = selectedCustomerDetail!!
        val refreshedCustomer = vm.customers.collectAsStateWithLifecycle().value.find { it.id == cust.id } ?: cust
        val trxList by vm.repository.getTransactionsForCustomer(cust.id).collectAsStateWithLifecycle(emptyList())

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedCustomerDetail = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(refreshedCustomer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text(refreshedCustomer.phone, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(Lang.get("outstanding_udhaar", isUrdu), fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "Rs. ${formatPKR(refreshedCustomer.totalUdhaar)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (refreshedCustomer.totalUdhaar > 0) Color(0xFFC53030) else Color(0xFF196F3D)
                            )
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            onClick = {
                                val textMsg = if (isUrdu) {
                                    "السلام علیکم میاں ${refreshedCustomer.name} صاحب،\nمدینہ کریانہ جنرل اسٹور کی طرف سے مطلب کیا جاتا ہے کہ آپ کے کھاتے کی بقایا واجب الادا رقم Rs. ${formatPKR(refreshedCustomer.totalUdhaar)} ہے۔ برائے مہربانی اپنا بقایا جلد از جلد دکان پر جمع کروائیں۔ نوازش ہوگی۔"
                                } else {
                                    "Dear ${refreshedCustomer.name},\nThis is a friendly ledger reminder from Kiryana Store. Your current pending outstanding balance is Rs. ${formatPKR(refreshedCustomer.totalUdhaar)}. Please visit the shop to settle your bill. Thank you!"
                                }
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=${refreshedCustomer.phone}&text=${Uri.encode(textMsg)}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Lang.get("whatsapp_reminder", isUrdu), fontSize = 11.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showRecordPaymentDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = KhataGreenPrimary)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Lang.get("record_payment", isUrdu), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(Lang.get("payments_history", isUrdu), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (trxList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (isUrdu) "کھاتے میں کوئی پرانی تاریخ درج نہیں ہے" else "No account statements found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trxList) { item ->
                        val isPayment = item.type == "UDHAAR_CREDIT"
                        val stamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPayment) Lang.get("record_payment", isUrdu) else item.itemsSummary.ifEmpty { "Credit Purchase" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isPayment) Color(0xFF196F3D) else Color(0xFF1B3D2B)
                                )
                                Text(stamp, fontSize = 10.sp, color = Color.Gray)
                                if (item.note.isNotEmpty()) {
                                    Text("${if (isUrdu) "نوٹ" else "Note"}: ${item.note}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }

                            Text(
                                text = (if (isPayment) "- " else "+ ") + "Rs. ${formatPKR(item.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isPayment) Color(0xFF196F3D) else Color(0xFFC53030)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCustDialog) {
        CustomerFormDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddCustDialog = false },
            onConfirm = { name, phone, address ->
                vm.saveCustomer(0, name, phone, address)
                showAddCustDialog = false
            }
        )
    }

    if (showRecordPaymentDialog && selectedCustomerDetail != null) {
        var recAmount by remember { mutableStateOf("") }
        var recNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text(Lang.get("record_payment", isUrdu), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${selectedCustomerDetail!!.name} سے موصول ہوئی رقم درج کریں:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = recAmount,
                        onValueChange = { recAmount = it },
                        label = { Text(Lang.get("payment_amount", isUrdu)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = recNote,
                        onValueChange = { recNote = it },
                        label = { Text(Lang.get("notes", isUrdu)) },
                        placeholder = { Text(if (isUrdu) "وصول کنندہ کا نام یا تاریخ..." else "e.g. advance or monthly recovery") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = recAmount.toDoubleOrNull() ?: 0.0
                        if (parsed > 0.0) {
                            vm.recordPayment(selectedCustomerDetail!!.id, parsed, recNote)
                            showRecordPaymentDialog = false
                        } else {
                            vm.showMessage("Invalid Amount!")
                        }
                    }
                ) {
                    Text(Lang.get("save", isUrdu))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = false }) {
                    Text(Lang.get("cancel", isUrdu))
                }
            }
        )
    }
}

@Composable
fun CustomerFormDialog(
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Lang.get("add_customer", isUrdu), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Lang.get("customer_name", isUrdu)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Lang.get("phone", isUrdu)) },
                    placeholder = { Text("03xxxxxxxxx") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(Lang.get("address", isUrdu)) },
                    placeholder = { Text(if (isUrdu) "دکان یا گلی محلہ..." else "Neighborhood or street name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        onConfirm(name, phone, address)
                    }
                }
            ) {
                Text(Lang.get("save", isUrdu))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Lang.get("cancel", isUrdu))
            }
        }
    )
}

// ======================== EXPENSES TRACKING ========================
@Composable
fun ExpensesScreen(vm: KiryanaViewModel, isUrdu: Boolean, isAdmin: Boolean) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    val categories = listOf("rent", "electricity", "purchase_stock", "salaries", "others")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9EBEA)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isUrdu) "کل اخراجات کی تفصیل" else "Total logged store expenses",
                            fontSize = 11.sp,
                            color = Color(0xFF78281F)
                        )
                        Text(
                            text = "Rs. ${formatPKR(expenses.sumOf { it.amount })}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78281F)
                        )
                    }
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF78281F))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isUrdu) "کوئی اخراجات درج نہیں ہیں" else "No logged expenses found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(1.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Lang.get(exp.category, isUrdu),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(exp.description, fontSize = 12.sp, color = Color.DarkGray)
                                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(exp.timestamp))
                                    Text(dateStr, fontSize = 10.sp, color = Color.Gray)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Rs. ${formatPKR(exp.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF78281F)
                                    )
                                    if (isAdmin) {
                                        IconButton(onClick = { vm.deleteExpense(exp.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddExpenseDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    if (showAddExpenseDialog) {
        var selectedCategory by remember { mutableStateOf("rent") }
        var expAmount by remember { mutableStateOf("") }
        var expDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text(Lang.get("add_expense", isUrdu), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(Lang.get("expense_category", isUrdu), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(Lang.get(cat, isUrdu), fontSize = 10.sp) }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            categories.drop(3).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(Lang.get(cat, isUrdu), fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = expAmount,
                        onValueChange = { expAmount = it },
                        label = { Text(Lang.get("amount", isUrdu)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = expDesc,
                        onValueChange = { expDesc = it },
                        label = { Text(if (isUrdu) "تفصیل / نام" else "Description / Detail") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = expAmount.toDoubleOrNull() ?: 0.0
                        if (parsed > 0.0) {
                            vm.saveExpense(selectedCategory, parsed, expDesc)
                            showAddExpenseDialog = false
                        } else {
                            vm.showMessage("Invalid Expense Amount!")
                        }
                    }
                ) {
                    Text(Lang.get("save", isUrdu))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text(Lang.get("cancel", isUrdu))
                }
            }
        )
    }
}

fun formatPKR(value: Double): String {
    val df = DecimalFormat("#,##,###")
    return df.format(value)
}
