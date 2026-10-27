package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerDatabase
import com.example.data.local.TransactionType
import com.example.data.repository.LedgerRepository
import com.example.ui.AppScreen
import com.example.ui.LedgerUiState
import com.example.ui.LedgerViewModel
import com.example.ui.components.ClearanceCelebrationDialog
import com.example.ui.components.DebtorFormDialog
import com.example.ui.components.ExchangeRateSettingsDialog
import com.example.ui.components.RecordTransactionDialog
import com.example.ui.screens.BackupAndExportScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtorStatementScreen
import com.example.ui.screens.DebtorsListScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.MyApplicationTheme
import com.example.util.Formatters
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = LedgerDatabase.getInstance(applicationContext)
        val repository = LedgerRepository(database.ledgerDao())

        setContent {
            val viewModel: LedgerViewModel = viewModel(
                factory = LedgerViewModel.provideFactory(repository, applicationContext)
            )
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                LedgerProductionApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavDestination(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

private val mainDestinations = listOf(
    NavDestination(AppScreen.DASHBOARD, "لوحة التحكم", Icons.Default.Dashboard, "nav_dashboard"),
    NavDestination(AppScreen.DEBTORS, "المدينون", Icons.Default.Groups, "nav_debtors"),
    NavDestination(AppScreen.REPORTS, "التقارير والجرد", Icons.Default.Assessment, "nav_reports"),
    NavDestination(AppScreen.BACKUP, "النسخ والتصدير", Icons.Default.Backup, "nav_backup")
)

@Composable
fun LedgerProductionApp(viewModel: LedgerViewModel) {
    val context = LocalContext.current
    val colors = LocalLedgerColors.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val usdToIqdRate by viewModel.usdToIqdRate.collectAsStateWithLifecycle()
    val showUnifiedEstimate by viewModel.showUnifiedEstimate.collectAsStateWithLifecycle()
    val activeClearanceEvent by viewModel.activeClearanceEvent.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val hasLocalSnapshot by viewModel.hasLocalSnapshot.collectAsStateWithLifecycle()
    val localSnapshotTimestamp by viewModel.localSnapshotTimestamp.collectAsStateWithLifecycle()

    // Dialog states
    var showTransactionDialog by remember { mutableStateOf(false) }
    var txDialogPreselectedDebtorId by remember { mutableStateOf<Long?>(null) }
    var txDialogInitialType by remember { mutableStateOf(TransactionType.PAYMENT) }
    var txDialogInitialCurrency by remember { mutableStateOf(CurrencyCode.IQD) }

    var showDebtorFormDialog by remember { mutableStateOf(false) }
    var editingDebtor by remember { mutableStateOf<DebtorEntity?>(null) }

    var showExchangeRateDialog by remember { mutableStateOf(false) }

    var isSidebarCollapsed by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        if (currentScreen == AppScreen.DEBTOR_DETAIL) {
            viewModel.navigateBackFromDetail()
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                CollapsibleSidebar(
                    isCollapsed = isSidebarCollapsed,
                    currentScreen = currentScreen,
                    onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    onSelectScreen = { viewModel.navigateTo(it) }
                )

                Column(modifier = Modifier.weight(1f)) {
                    TopSaaSHeader(
                        uiState = uiState,
                        isWideScreen = true,
                        isDarkMode = isDarkMode,
                        onOpenDrawer = {},
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onOpenExchangeRate = { showExchangeRateDialog = true },
                        onNewTransaction = {
                            txDialogPreselectedDebtorId = null
                            txDialogInitialType = TransactionType.PAYMENT
                            txDialogInitialCurrency = CurrencyCode.IQD
                            showTransactionDialog = true
                        }
                    )

                    StatusFeedbackBanner(
                        message = statusBannerMessage,
                        onDismiss = { viewModel.clearStatusBanner() }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MainScreenContent(
                            currentScreen = currentScreen,
                            uiState = uiState,
                            viewModel = viewModel,
                            usdToIqdRate = usdToIqdRate,
                            showUnifiedEstimate = showUnifiedEstimate,
                            hasLocalSnapshot = hasLocalSnapshot,
                            localSnapshotTimestamp = localSnapshotTimestamp,
                            onOpenExchangeRateDialog = { showExchangeRateDialog = true },
                            onOpenRecordTransaction = { debtorId, type, currency ->
                                txDialogPreselectedDebtorId = debtorId
                                txDialogInitialType = type
                                txDialogInitialCurrency = currency
                                showTransactionDialog = true
                            },
                            onOpenAddDebtor = {
                                editingDebtor = null
                                showDebtorFormDialog = true
                            },
                            onOpenEditDebtor = { debtor ->
                                editingDebtor = debtor
                                showDebtorFormDialog = true
                            },
                            onCreateLocalSnapshot = { viewModel.createLocalSnapshotBackup(context) },
                            onRestoreLocalSnapshot = { viewModel.restoreFromLocalSnapshot(context) }
                        )
                    }
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = colors.sidebarSurface,
                        drawerContentColor = Color.White
                    ) {
                        MobileDrawerContent(
                            currentScreen = currentScreen,
                            uiState = uiState,
                            onSelectScreen = { screen ->
                                viewModel.navigateTo(screen)
                                scope.launch { drawerState.close() }
                            },
                            onNewTransaction = {
                                scope.launch { drawerState.close() }
                                txDialogPreselectedDebtorId = null
                                txDialogInitialType = TransactionType.PAYMENT
                                txDialogInitialCurrency = CurrencyCode.IQD
                                showTransactionDialog = true
                            },
                            onOpenExchangeRate = {
                                scope.launch { drawerState.close() }
                                showExchangeRateDialog = true
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = colors.background,
                    topBar = {
                        TopSaaSHeader(
                            uiState = uiState,
                            isWideScreen = false,
                            isDarkMode = isDarkMode,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onOpenExchangeRate = { showExchangeRateDialog = true },
                            onNewTransaction = {
                                txDialogPreselectedDebtorId = null
                                txDialogInitialType = TransactionType.PAYMENT
                                txDialogInitialCurrency = CurrencyCode.IQD
                                showTransactionDialog = true
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = colors.cardSurface,
                            tonalElevation = 4.dp
                        ) {
                            mainDestinations.forEach { dest ->
                                val selected = currentScreen == dest.screen ||
                                    (currentScreen == AppScreen.DEBTOR_DETAIL && dest.screen == AppScreen.DEBTORS)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(dest.screen) },
                                    icon = {
                                        Icon(dest.icon, contentDescription = dest.label)
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.primaryInteractive,
                                        selectedTextColor = colors.primaryInteractive,
                                        indicatorColor = colors.primaryInteractiveSoft,
                                        unselectedIconColor = colors.textMuted,
                                        unselectedTextColor = colors.textMuted
                                    ),
                                    modifier = Modifier.testTag(dest.tag)
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                txDialogPreselectedDebtorId =
                                    if (currentScreen == AppScreen.DEBTOR_DETAIL) uiState.selectedDebtorId else null
                                txDialogInitialType = TransactionType.PAYMENT
                                txDialogInitialCurrency = CurrencyCode.IQD
                                showTransactionDialog = true
                            },
                            containerColor = colors.primaryInteractive,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_new_transaction")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "عملية جديدة")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "عملية جديدة",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        StatusFeedbackBanner(
                            message = statusBannerMessage,
                            onDismiss = { viewModel.clearStatusBanner() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            MainScreenContent(
                                currentScreen = currentScreen,
                                uiState = uiState,
                                viewModel = viewModel,
                                usdToIqdRate = usdToIqdRate,
                                showUnifiedEstimate = showUnifiedEstimate,
                                hasLocalSnapshot = hasLocalSnapshot,
                                localSnapshotTimestamp = localSnapshotTimestamp,
                                onOpenExchangeRateDialog = { showExchangeRateDialog = true },
                                onOpenRecordTransaction = { debtorId, type, currency ->
                                    txDialogPreselectedDebtorId = debtorId
                                    txDialogInitialType = type
                                    txDialogInitialCurrency = currency
                                    showTransactionDialog = true
                                },
                                onOpenAddDebtor = {
                                    editingDebtor = null
                                    showDebtorFormDialog = true
                                },
                                onOpenEditDebtor = { debtor ->
                                    editingDebtor = debtor
                                    showDebtorFormDialog = true
                                },
                                onCreateLocalSnapshot = { viewModel.createLocalSnapshotBackup(context) },
                                onRestoreLocalSnapshot = { viewModel.restoreFromLocalSnapshot(context) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTransactionDialog) {
        RecordTransactionDialog(
            summaries = uiState.debtorSummaries,
            preselectedDebtorId = txDialogPreselectedDebtorId,
            initialType = txDialogInitialType,
            initialCurrency = txDialogInitialCurrency,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { debtorId, type, currency, amount, note ->
                viewModel.recordTransaction(debtorId, type, currency, amount, note)
            }
        )
    }

    if (showDebtorFormDialog) {
        DebtorFormDialog(
            existingDebtor = editingDebtor,
            onDismiss = { showDebtorFormDialog = false },
            onSave = { existingId, name, phone, address, limitIqd, limitUsd, dueMillis, notes, initDebt, initCurrency, initNote ->
                viewModel.saveDebtor(
                    existingId = existingId,
                    name = name,
                    phone = phone,
                    address = address,
                    creditLimitIqd = limitIqd,
                    creditLimitUsd = limitUsd,
                    dueDateMillis = dueMillis,
                    notes = notes,
                    initialDebtAmount = initDebt,
                    initialDebtCurrency = initCurrency,
                    initialDebtNote = initNote
                )
            }
        )
    }

    if (showExchangeRateDialog) {
        ExchangeRateSettingsDialog(
            currentRate = usdToIqdRate,
            showUnifiedEstimate = showUnifiedEstimate,
            onDismiss = { showExchangeRateDialog = false },
            onSave = { rate, showUnified ->
                viewModel.updateExchangeRateSettings(rate, showUnified)
            }
        )
    }

    activeClearanceEvent?.let { clearanceEvent ->
        ClearanceCelebrationDialog(
            event = clearanceEvent,
            onDismiss = { viewModel.dismissClearanceModal() }
        )
    }
}

@Composable
private fun MainScreenContent(
    currentScreen: AppScreen,
    uiState: LedgerUiState,
    viewModel: LedgerViewModel,
    usdToIqdRate: Double,
    showUnifiedEstimate: Boolean,
    hasLocalSnapshot: Boolean,
    localSnapshotTimestamp: Long,
    onOpenExchangeRateDialog: () -> Unit,
    onOpenRecordTransaction: (debtorId: Long?, initialType: String, initialCurrency: String) -> Unit,
    onOpenAddDebtor: () -> Unit,
    onOpenEditDebtor: (DebtorEntity) -> Unit,
    onCreateLocalSnapshot: () -> Unit,
    onRestoreLocalSnapshot: () -> Unit
) {
    when (currentScreen) {
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                uiState = uiState,
                usdToIqdRate = usdToIqdRate,
                showUnifiedEstimate = showUnifiedEstimate,
                onOpenExchangeRateDialog = onOpenExchangeRateDialog,
                onOpenRecordTransaction = onOpenRecordTransaction,
                onOpenAddDebtor = onOpenAddDebtor,
                onOpenDebtorProfile = { viewModel.openDebtorProfile(it) },
                onNavigateToScreen = { viewModel.navigateTo(it) },
                onSelectFilterAndGoToDebtors = { filter ->
                    viewModel.updateFilter(filter)
                    viewModel.navigateTo(AppScreen.DEBTORS)
                }
            )
        }

        AppScreen.DEBTORS -> {
            DebtorsListScreen(
                uiState = uiState,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onFilterChange = { viewModel.updateFilter(it) },
                onOpenAddDebtor = onOpenAddDebtor,
                onOpenEditDebtor = onOpenEditDebtor,
                onOpenDebtorProfile = { viewModel.openDebtorProfile(it) },
                onOpenRecordTransaction = { debtorId, type, currency ->
                    onOpenRecordTransaction(debtorId, type, currency)
                },
                onTriggerClearanceModal = { viewModel.triggerClearanceModalForDebtor(it) }
            )
        }

        AppScreen.DEBTOR_DETAIL -> {
            val selectedSummary = uiState.debtorSummaries.find {
                it.debtor.id == uiState.selectedDebtorId
            } ?: uiState.debtorSummaries.firstOrNull()

            if (selectedSummary != null) {
                val entries = remember(selectedSummary.debtor.id, uiState.transactions) {
                    viewModel.buildStatementEntries(selectedSummary.debtor.id, uiState.transactions)
                }
                DebtorStatementScreen(
                    summary = selectedSummary,
                    statementEntries = entries,
                    onBack = { viewModel.navigateBackFromDetail() },
                    onEditDebtor = onOpenEditDebtor,
                    onDeleteDebtor = { viewModel.deleteDebtor(it) },
                    onRecordTransaction = { debtorId, type, currency ->
                        onOpenRecordTransaction(debtorId, type, currency)
                    },
                    onSettleAllBalancesNow = { summary ->
                        viewModel.settleAllRemainingBalancesForDebtor(summary)
                    },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onTriggerClearanceModal = { viewModel.triggerClearanceModalForDebtor(it) }
                )
            } else {
                viewModel.navigateTo(AppScreen.DEBTORS)
            }
        }

        AppScreen.REPORTS -> {
            ReportsScreen(
                uiState = uiState,
                onOpenDebtorProfile = { viewModel.openDebtorProfile(it) }
            )
        }

        AppScreen.BACKUP -> {
            BackupAndExportScreen(
                uiState = uiState,
                hasLocalSnapshot = hasLocalSnapshot,
                localSnapshotTimestamp = localSnapshotTimestamp,
                onCreateLocalSnapshot = onCreateLocalSnapshot,
                onRestoreLocalSnapshot = onRestoreLocalSnapshot,
                onRestoreFromJsonString = { viewModel.restoreDatabaseFromJsonString(it) },
                onGenerateBackupJson = { viewModel.generateFullBackupJsonString() }
            )
        }
    }
}

@Composable
private fun TopSaaSHeader(
    uiState: LedgerUiState,
    isWideScreen: Boolean,
    isDarkMode: Boolean,
    onOpenDrawer: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenExchangeRate: () -> Unit,
    onNewTransaction: () -> Unit
) {
    val colors = LocalLedgerColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.cardSurface,
        border = BorderStroke(1.dp, colors.subtleBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle Top Dedication Ribbon ("صدقة جارية عن أحمد أبو رسلان")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(vertical = 5.dp, horizontal = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.dedication_banner),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF6EE7B7),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("dedication_banner_text")
                        )
                    }
                    Text(
                        text = "${Formatters.formatIqd(uiState.totalOutstandingDebtIqd)} | ${Formatters.formatUsd(uiState.totalOutstandingDebtUsd)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF8FAFC)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isWideScreen) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "القائمة الجانبية",
                                tint = colors.textPrimary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primaryInteractive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "نظام العملات المزدوج (د.ع IQD | $ USD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onOpenExchangeRate) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "إعدادات سعر الصرف",
                            tint = colors.primaryInteractive
                        )
                    }

                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("toggle_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل الوضع الليلي والنهاري",
                            tint = colors.textPrimary
                        )
                    }

                    if (isWideScreen) {
                        Button(
                            onClick = onNewTransaction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عملية جديدة", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSidebar(
    isCollapsed: Boolean,
    currentScreen: AppScreen,
    onToggleCollapse: () -> Unit,
    onSelectScreen: (AppScreen) -> Unit
) {
    val colors = LocalLedgerColors.current
    val width by animateDpAsState(
        targetValue = if (isCollapsed) 78.dp else 250.dp,
        label = "sidebar_width"
    )

    Surface(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        color = colors.sidebarSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isCollapsed) {
                        Text(
                            text = "سجل الديون",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onToggleCollapse) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                            contentDescription = "طي القائمة",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                mainDestinations.forEach { dest ->
                    val selected = currentScreen == dest.screen ||
                        (currentScreen == AppScreen.DEBTOR_DETAIL && dest.screen == AppScreen.DEBTORS)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectScreen(dest.screen) },
                        color = if (selected) colors.primaryInteractive else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label,
                                tint = Color.White
                            )
                            if (!isCollapsed) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (!isCollapsed) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.dedication_banner),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF6EE7B7)
                        )
                        Text(
                            text = "نظام العملات المزدوج (IQD | USD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileDrawerContent(
    currentScreen: AppScreen,
    uiState: LedgerUiState,
    onSelectScreen: (AppScreen) -> Unit,
    onNewTransaction: () -> Unit,
    onOpenExchangeRate: () -> Unit
) {
    val colors = LocalLedgerColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primaryInteractive),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "سجل الديون والمحاسبة",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.dedication_banner),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF6EE7B7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(16.dp))

            mainDestinations.forEach { dest ->
                val selected = currentScreen == dest.screen
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScreen(dest.screen) },
                    color = if (selected) colors.primaryInteractive else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(dest.icon, contentDescription = dest.label, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenExchangeRate() },
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = Color(0xFF6EE7B7))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "إعدادات سعر الصرف (IQD / USD)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onNewTransaction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل عملية جديدة (د.ع / $)", color = Color.White)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.08f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.dedication_banner),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF6EE7B7)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المستحق: ${Formatters.formatIqd(uiState.totalOutstandingDebtIqd)} | ${Formatters.formatUsd(uiState.totalOutstandingDebtUsd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
private fun StatusFeedbackBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    val colors = LocalLedgerColors.current
    AnimatedVisibility(visible = !message.isNullOrBlank()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = colors.paymentEmeraldSoft,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.paymentEmerald)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.paymentEmerald,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق التنبيه",
                        tint = colors.paymentEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
