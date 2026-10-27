package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyCode
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.CommittedCustomerRank
import com.example.ui.LedgerUiState
import com.example.ui.MonthlyStatPoint
import com.example.ui.ReportTab
import com.example.ui.components.CollectionGrowthCurveCard
import com.example.ui.theme.LocalLedgerColors
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    uiState: LedgerUiState,
    onOpenDebtorProfile: (Long) -> Unit
) {
    val colors = LocalLedgerColors.current
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(ReportTab.DAILY) }

    val nowCal = remember { Calendar.getInstance() }
    var selectedDayMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var annualTableCurrency by remember { mutableStateOf(CurrencyCode.IQD) }

    // Custom filter states
    var customRangeDays by remember { mutableIntStateOf(30) }
    var customCurrencyFilter by remember { mutableStateOf<String?>(null) } // null = All, "IQD", "USD"
    var customDebtorId by remember { mutableStateOf<Long?>(null) }
    var debtorDropdownExpanded by remember { mutableStateOf(false) }

    val activeReportTransactions = remember(
        uiState.transactions,
        selectedTab,
        selectedDayMillis,
        selectedMonth,
        selectedYear,
        customRangeDays,
        customCurrencyFilter,
        customDebtorId
    ) {
        when (selectedTab) {
            ReportTab.DAILY -> uiState.transactions.filter {
                Formatters.isSameDay(it.timestamp, selectedDayMillis)
            }.sortedByDescending { it.timestamp }

            ReportTab.MONTHLY -> uiState.transactions.filter {
                Formatters.isSameMonth(it.timestamp, selectedYear, selectedMonth)
            }

            ReportTab.ANNUAL -> uiState.transactions.filter {
                Formatters.isSameYear(it.timestamp, selectedYear)
            }

            ReportTab.CUSTOM -> {
                val cutoff = System.currentTimeMillis() - (customRangeDays * 24L * 60L * 60L * 1000L)
                uiState.transactions.filter { tx ->
                    val inDate = tx.timestamp >= cutoff
                    val inCurrency = customCurrencyFilter == null || tx.currency == customCurrencyFilter
                    val inDebtor = customDebtorId == null || tx.debtorId == customDebtorId
                    inDate && inCurrency && inDebtor
                }
            }
        }
    }

    // Separated Period Totals (IQD & USD)
    val periodDebtsIqd = remember(activeReportTransactions) {
        activeReportTransactions.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
    }
    val periodPaymentsIqd = remember(activeReportTransactions) {
        activeReportTransactions.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
    }
    val periodRateIqd = remember(periodDebtsIqd, periodPaymentsIqd) {
        if (periodDebtsIqd > 0.0) ((periodPaymentsIqd / periodDebtsIqd) * 100.0).coerceIn(0.0, 100.0) else 0.0
    }

    val periodDebtsUsd = remember(activeReportTransactions) {
        activeReportTransactions.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
    }
    val periodPaymentsUsd = remember(activeReportTransactions) {
        activeReportTransactions.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
    }
    val periodRateUsd = remember(periodDebtsUsd, periodPaymentsUsd) {
        if (periodDebtsUsd > 0.0) ((periodPaymentsUsd / periodDebtsUsd) * 100.0).coerceIn(0.0, 100.0) else 0.0
    }

    // Annual 12-month table & curve
    val annualMonths = remember(uiState.transactions, selectedYear) {
        (0..11).map { m ->
            val mTxs = uiState.transactions.filter {
                Formatters.isSameMonth(it.timestamp, selectedYear, m)
            }
            val dIqd = mTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val pIqd = mTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val rateIqd = if (dIqd > 0.0) ((pIqd / dIqd) * 100.0).coerceIn(0.0, 100.0) else 0.0

            val dUsd = mTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val pUsd = mTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val rateUsd = if (dUsd > 0.0) ((pUsd / dUsd) * 100.0).coerceIn(0.0, 100.0) else 0.0

            MonthlyStatPoint(
                monthIndex = m,
                monthName = Formatters.getMonthName(m),
                totalDebtIqd = dIqd,
                totalPaidIqd = pIqd,
                collectionRateIqd = rateIqd,
                totalDebtUsd = dUsd,
                totalPaidUsd = pUsd,
                collectionRateUsd = rateUsd
            )
        }
    }

    val committedCustomers = remember(activeReportTransactions, uiState.debtorMap) {
        activeReportTransactions
            .groupBy { it.debtorId }
            .mapNotNull { (debtorId, txs) ->
                val debtor = uiState.debtorMap[debtorId] ?: return@mapNotNull null
                val paidIqd = txs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
                val debtIqd = txs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
                val paidUsd = txs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
                val debtUsd = txs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
                if (paidIqd <= 0.0 && paidUsd <= 0.0) return@mapNotNull null
                val pct = when {
                    debtIqd > 0.0 -> ((paidIqd / debtIqd) * 100.0).coerceAtMost(100.0)
                    debtUsd > 0.0 -> ((paidUsd / debtUsd) * 100.0).coerceAtMost(100.0)
                    else -> 100.0
                }
                CommittedCustomerRank(
                    debtor = debtor,
                    paidIqd = paidIqd,
                    debtIqd = debtIqd,
                    paidUsd = paidUsd,
                    debtUsd = debtUsd,
                    commitmentPercentage = pct
                )
            }
            .sortedWith(
                compareByDescending<CommittedCustomerRank> { it.paidIqd }
                    .thenByDescending { it.paidUsd }
            )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الحسابات والتقارير التفصيلية الدقيقة (IQD | USD)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "الجرد اليومي بالساعة، التقرير الشهري، الجرد السنوي، والفلترة حسب العملة والعميل",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            FilterChip(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                label = {
                                    Text(
                                        text = tab.labelAr,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryInteractive,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("report_tab_${tab.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedTab) {
                        ReportTab.DAILY -> {
                            val dayMs = 24L * 60L * 60L * 1000L
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { selectedDayMillis -= dayMs }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "اليوم السابق")
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (Formatters.isSameDay(selectedDayMillis, System.currentTimeMillis()))
                                            "جرد اليوم (${Formatters.formatDate(selectedDayMillis)})"
                                        else
                                            "جرد تاريخ: ${Formatters.formatDate(selectedDayMillis)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.primaryInteractive
                                    )
                                    Text(
                                        text = "اضغط الأسهم للتنقل بين الأيام",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textMuted
                                    )
                                }
                                IconButton(onClick = { selectedDayMillis += dayMs }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "اليوم التالي")
                                }
                            }
                        }

                        ReportTab.MONTHLY -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedMonth == 0) {
                                            selectedMonth = 11
                                            selectedYear -= 1
                                        } else {
                                            selectedMonth -= 1
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق")
                                }
                                Text(
                                    text = "تقرير شهر ${Formatters.getMonthName(selectedMonth)} $selectedYear",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.primaryInteractive
                                )
                                IconButton(
                                    onClick = {
                                        if (selectedMonth == 11) {
                                            selectedMonth = 0
                                            selectedYear += 1
                                        } else {
                                            selectedMonth += 1
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر التالي")
                                }
                            }
                        }

                        ReportTab.ANNUAL -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { selectedYear -= 1 }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "السنة السابقة")
                                }
                                Text(
                                    text = "التقرير المالي السنوي لعام $selectedYear",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.primaryInteractive
                                )
                                IconButton(onClick = { selectedYear += 1 }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "السنة التالية")
                                }
                            }
                        }

                        ReportTab.CUSTOM -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Date Range Filter
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        7 to "آخر 7 أيام",
                                        30 to "آخر 30 يوم",
                                        90 to "آخر 3 أشهر",
                                        365 to "خلال سنة كاملة"
                                    ).forEach { (days, label) ->
                                        FilterChip(
                                            selected = customRangeDays == days,
                                            onClick = { customRangeDays = days },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                // Currency Filter (All, IQD, USD)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = customCurrencyFilter == null,
                                        onClick = { customCurrencyFilter = null },
                                        label = { Text("كل العملات (IQD + USD)") }
                                    )
                                    FilterChip(
                                        selected = customCurrencyFilter == CurrencyCode.IQD,
                                        onClick = { customCurrencyFilter = CurrencyCode.IQD },
                                        label = { Text("دينار عراقي فقط (د.ع)") }
                                    )
                                    FilterChip(
                                        selected = customCurrencyFilter == CurrencyCode.USD,
                                        onClick = { customCurrencyFilter = CurrencyCode.USD },
                                        label = { Text("دولار أمريكي فقط ($)") }
                                    )
                                }

                                ExposedDropdownMenuBox(
                                    expanded = debtorDropdownExpanded,
                                    onExpandedChange = { debtorDropdownExpanded = !debtorDropdownExpanded }
                                ) {
                                    val selectedDebtorName = customDebtorId?.let { id ->
                                        uiState.debtorMap[id]?.name
                                    } ?: "جميع العملاء والمدينين"

                                    OutlinedTextField(
                                        value = selectedDebtorName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("فلترة حسب العميل") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = debtorDropdownExpanded)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = debtorDropdownExpanded,
                                        onDismissRequest = { debtorDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("جميع العملاء والمدينين") },
                                            onClick = {
                                                customDebtorId = null
                                                debtorDropdownExpanded = false
                                            }
                                        )
                                        uiState.debtors.forEach { d ->
                                            DropdownMenuItem(
                                                text = { Text(d.name) },
                                                onClick = {
                                                    customDebtorId = d.id
                                                    debtorDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = ExportAndBackupManager.buildReportCsv(
                                    reportTitle = selectedTab.labelAr,
                                    transactions = activeReportTransactions,
                                    debtorMap = uiState.debtorMap
                                )
                                ExportAndBackupManager.saveAndShareFile(
                                    context = context,
                                    fileName = "report_${selectedTab.name.lowercase()}.csv",
                                    mimeType = "text/csv",
                                    contentBytes = csv.toByteArray(Charsets.UTF_8),
                                    shareTitle = "تصدير ${selectedTab.labelAr} (Excel CSV)"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_report_csv_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير Excel CSV")
                        }

                        Button(
                            onClick = {
                                val pdf = ExportAndBackupManager.generateStatementOrReportPdfFile(
                                    context = context,
                                    title = "تقرير: ${selectedTab.labelAr}",
                                    subtitle = "عدد العمليات: ${activeReportTransactions.size}",
                                    summaryLines = listOf(
                                        "حركة الدينار (IQD): الديون ${Formatters.formatIqd(periodDebtsIqd)} | التحصيل ${Formatters.formatIqd(periodPaymentsIqd)} (${Formatters.formatNumber(periodRateIqd)}%)",
                                        "حركة الدولار (USD): الديون ${Formatters.formatUsd(periodDebtsUsd)} | التحصيل ${Formatters.formatUsd(periodPaymentsUsd)} (${Formatters.formatNumber(periodRateUsd)}%)"
                                    ),
                                    tableHeaders = listOf("التاريخ والوقت", "اسم العميل", "العملية والعملة", "المبلغ", "البيان"),
                                    tableRows = activeReportTransactions.map { tx ->
                                        val typeAr = if (tx.type == TransactionType.DEBT) "دين" else "تسديد"
                                        listOf(
                                            Formatters.formatDateTime(tx.timestamp),
                                            uiState.debtorMap[tx.debtorId]?.name ?: "عميل #${tx.debtorId}",
                                            "$typeAr (${tx.currency})",
                                            Formatters.formatByCurrency(tx.amount, tx.currency),
                                            tx.note
                                        )
                                    },
                                    fileNamePrefix = "audit_${selectedTab.name.lowercase()}"
                                )
                                ExportAndBackupManager.printPdfFile(
                                    context = context,
                                    pdfFile = pdf,
                                    jobName = "تقرير_${selectedTab.labelAr}"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("print_report_pdf_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة / حفظ PDF", color = Color.White)
                        }
                    }
                }
            }
        }

        // Dual-Currency Period Summary Metrics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ملخص الفترة مفصولاً حسب العملة (${activeReportTransactions.size} عملية)",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // IQD Summary Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.elevatedSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("حركة الدينار العراقي (IQD)", style = MaterialTheme.typography.labelLarge, color = colors.primaryInteractive)
                                Text(
                                    text = "نسبة التحصيل: ${Formatters.formatNumber(periodRateIqd)}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.paymentEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الديون: ${Formatters.formatIqd(periodDebtsIqd)}", style = MaterialTheme.typography.titleSmall, color = colors.debtCrimson)
                                Text("التسديدات: ${Formatters.formatIqd(periodPaymentsIqd)}", style = MaterialTheme.typography.titleSmall, color = colors.paymentEmerald)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (periodRateIqd / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = colors.paymentEmerald,
                                trackColor = colors.subtleBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // USD Summary Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.elevatedSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("حركة الدولار الأمريكي (USD)", style = MaterialTheme.typography.labelLarge, color = colors.primaryInteractive)
                                Text(
                                    text = "نسبة التحصيل: ${Formatters.formatNumber(periodRateUsd)}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.paymentEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الديون: ${Formatters.formatUsd(periodDebtsUsd)}", style = MaterialTheme.typography.titleSmall, color = colors.debtCrimson)
                                Text("التسديدات: ${Formatters.formatUsd(periodPaymentsUsd)}", style = MaterialTheme.typography.titleSmall, color = colors.paymentEmerald)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (periodRateUsd / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = colors.paymentEmerald,
                                trackColor = colors.subtleBorder
                            )
                        }
                    }
                }
            }
        }

        // Tab-specific Sections
        when (selectedTab) {
            ReportTab.DAILY -> {
                item {
                    Text(
                        text = "قائمة عمليات اليوم بالساعة (${activeReportTransactions.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                }
                if (activeReportTransactions.isEmpty()) {
                    item {
                        EmptyReportBox("لا توجد عمليات مسجلة في هذا اليوم.")
                    }
                } else {
                    items(activeReportTransactions, key = { it.id }) { tx ->
                        HourlyTransactionRow(
                            tx = tx,
                            debtorName = uiState.debtorMap[tx.debtorId]?.name ?: "عميل #${tx.debtorId}",
                            onClick = { onOpenDebtorProfile(tx.debtorId) }
                        )
                    }
                }
            }

            ReportTab.MONTHLY -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = colors.paymentEmerald
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "أكثر العملاء التزاماً بالسداد خلال الشهر",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (committedCustomers.isEmpty()) {
                                Text(
                                    text = "لا توجد دفعات تسديد مسجلة في هذا الشهر.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            } else {
                                committedCustomers.take(5).forEachIndexed { idx, item ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = colors.subtleBorder
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenDebtorProfile(item.debtor.id) },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = colors.paymentEmeraldSoft,
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    text = "#${idx + 1}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = colors.paymentEmerald,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.debtor.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = "معدل الالتزام: ${Formatters.formatNumber(item.commitmentPercentage)}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (item.paidIqd > 0) {
                                                Text(
                                                    text = Formatters.formatIqd(item.paidIqd),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = colors.paymentEmerald,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (item.paidUsd > 0) {
                                                Text(
                                                    text = Formatters.formatUsd(item.paidUsd),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = colors.primaryInteractive,
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

                items(activeReportTransactions, key = { it.id }) { tx ->
                    HourlyTransactionRow(
                        tx = tx,
                        debtorName = uiState.debtorMap[tx.debtorId]?.name ?: "عميل #${tx.debtorId}",
                        onClick = { onOpenDebtorProfile(tx.debtorId) }
                    )
                }
            }

            ReportTab.ANNUAL -> {
                item {
                    CollectionGrowthCurveCard(
                        title = "منحنى نمو التحصيل السنوي ($selectedYear)",
                        subtitle = "تطور إجمالي التسديدات المحصلة على مدار الاثني عشر شهراً",
                        points = annualMonths,
                        selectedCurrency = annualTableCurrency,
                        onCurrencyChange = { annualTableCurrency = it }
                    )
                }

                // 12-Month Standard Accounting Table separated by currency
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "جدول الأشهر الاثني عشر ($selectedYear)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = annualTableCurrency == CurrencyCode.IQD,
                                        onClick = { annualTableCurrency = CurrencyCode.IQD },
                                        label = { Text("د.ع IQD") }
                                    )
                                    FilterChip(
                                        selected = annualTableCurrency == CurrencyCode.USD,
                                        onClick = { annualTableCurrency = CurrencyCode.USD },
                                        label = { Text("$ USD") }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = colors.elevatedSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الشهر", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                                    Text("الديون (${Formatters.currencyShortSymbol(annualTableCurrency)})", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1.2f))
                                    Text("التحصيل (${Formatters.currencyShortSymbol(annualTableCurrency)})", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1.2f))
                                    Text("النسبة", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                                }
                            }

                            annualMonths.forEachIndexed { idx, pt ->
                                if (idx > 0) {
                                    HorizontalDivider(color = colors.subtleBorder)
                                }
                                val d = if (annualTableCurrency == CurrencyCode.USD) pt.totalDebtUsd else pt.totalDebtIqd
                                val p = if (annualTableCurrency == CurrencyCode.USD) pt.totalPaidUsd else pt.totalPaidIqd
                                val r = if (annualTableCurrency == CurrencyCode.USD) pt.collectionRateUsd else pt.collectionRateIqd

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pt.monthName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = Formatters.formatByCurrency(d, annualTableCurrency),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (d > 0) colors.debtCrimson else colors.textMuted,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = Formatters.formatByCurrency(p, annualTableCurrency),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (p > 0) colors.paymentEmerald else colors.textMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = "${Formatters.formatNumber(r)}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.primaryInteractive,
                                        modifier = Modifier.weight(0.8f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ReportTab.CUSTOM -> {
                if (activeReportTransactions.isEmpty()) {
                    item {
                        EmptyReportBox("لا توجد عمليات ضمن نطاق الفلترة المحدد.")
                    }
                } else {
                    items(activeReportTransactions, key = { it.id }) { tx ->
                        HourlyTransactionRow(
                            tx = tx,
                            debtorName = uiState.debtorMap[tx.debtorId]?.name ?: "عميل #${tx.debtorId}",
                            onClick = { onOpenDebtorProfile(tx.debtorId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyTransactionRow(
    tx: LedgerTransactionEntity,
    debtorName: String,
    onClick: () -> Unit
) {
    val colors = LocalLedgerColors.current
    val isPayment = tx.type == TransactionType.PAYMENT

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder)
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPayment) colors.paymentEmeraldSoft else colors.debtCrimsonSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPayment) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPayment) colors.paymentEmerald else colors.debtCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = debtorName,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = colors.primaryInteractiveSoft,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tx.currency,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primaryInteractive,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = Formatters.formatTime(tx.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primaryInteractive
                        )
                    }
                    Text(
                        text = "${tx.note} • ${Formatters.formatDate(tx.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = (if (isPayment) "+ " else "- ") + Formatters.formatByCurrency(tx.amount, tx.currency),
                style = MaterialTheme.typography.titleMedium,
                color = if (isPayment) colors.paymentEmerald else colors.debtCrimson,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyReportBox(message: String) {
    val colors = LocalLedgerColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(24.dp)
        )
    }
}
