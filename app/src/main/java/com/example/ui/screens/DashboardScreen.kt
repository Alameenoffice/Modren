package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorSummary
import com.example.data.local.TransactionType
import com.example.ui.AppScreen
import com.example.ui.DebtorFilter
import com.example.ui.LedgerUiState
import com.example.ui.MonthlyStatPoint
import com.example.ui.components.DebtVsPaymentInteractiveChart
import com.example.ui.components.DualCurrencyKpiCard
import com.example.ui.components.FinancialKpiCard
import com.example.ui.theme.LocalLedgerColors
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import java.util.Calendar

@Composable
fun DashboardScreen(
    uiState: LedgerUiState,
    officeName: String,
    usdToIqdRate: Double,
    showUnifiedEstimate: Boolean,
    onOpenExchangeRateDialog: () -> Unit,
    onOpenRecordTransaction: (debtorId: Long?, initialType: String, initialCurrency: String) -> Unit,
    onOpenAddDebtor: () -> Unit,
    onOpenDebtorProfile: (Long) -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit,
    onSelectFilterAndGoToDebtors: (DebtorFilter) -> Unit
) {
    val colors = LocalLedgerColors.current
    val context = LocalContext.current

    // Compute 6-month dual-currency chart series
    val sixMonthPoints = remember(uiState.transactions) {
        val nowCal = Calendar.getInstance()
        (5 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply {
                timeInMillis = nowCal.timeInMillis
                add(Calendar.MONTH, -offset)
            }
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val monthTxs = uiState.transactions.filter {
                Formatters.isSameMonth(it.timestamp, year, month)
            }
            val dIqd = monthTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val pIqd = monthTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val rateIqd = if (dIqd > 0.0) ((pIqd / dIqd) * 100.0).coerceIn(0.0, 100.0) else 0.0

            val dUsd = monthTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val pUsd = monthTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val rateUsd = if (dUsd > 0.0) ((pUsd / dUsd) * 100.0).coerceIn(0.0, 100.0) else 0.0

            MonthlyStatPoint(
                monthIndex = month,
                monthName = Formatters.getMonthName(month),
                totalDebtIqd = dIqd,
                totalPaidIqd = pIqd,
                collectionRateIqd = rateIqd,
                totalDebtUsd = dUsd,
                totalPaidUsd = pUsd,
                collectionRateUsd = rateUsd
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Office Identity & Quick Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = colors.primaryInteractive,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = officeName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("dashboard_office_name_title")
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "لوحة التحكم المالية المركزية • فصل محاسبي تام (د.ع IQD | $ USD)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenExchangeRateDialog() }
                                .testTag("open_exchange_rate_settings_button"),
                            color = colors.elevatedSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.subtleBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "تعديل اسم المكتب وسعر الصرف",
                                    tint = colors.primaryInteractive,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1$ = ${Formatters.formatNumber(usdToIqdRate)} د.ع",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.primaryInteractive
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (uiState.debtors.isEmpty()) {
                                    onOpenAddDebtor()
                                } else {
                                    onOpenRecordTransaction(
                                        null,
                                        TransactionType.PAYMENT,
                                        CurrencyCode.IQD
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_record_payment_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل دفعة / دين", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onOpenAddDebtor,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_add_debtor_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.primaryInteractive)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = colors.primaryInteractive,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة مدين جديد", color = colors.primaryInteractive)
                        }
                    }
                }
            }
        }

        // Elegant Empty State Banner when the ledger is completely empty (Clean State)
        if (uiState.debtors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clean_empty_state_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                    border = BorderStroke(1.5.dp, colors.primaryInteractive.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.primaryInteractiveSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = colors.primaryInteractive,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "سجل الديون فارغ وجاهز للعمل",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جميع الأرصدة الآن (0 د.ع | 0 $). ابدأ بإضافة أول مدين لتسجيل الديون، الدفعات، وإرسال وصولات القبض والمطالبات عبر واتساب باسم «$officeName».",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenAddDebtor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("empty_state_add_first_debtor_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة أول مدين الآن", color = Color.White, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }

        // 4 KPI Cards (Dual-Currency Separated)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DualCurrencyKpiCard(
                        title = "إجمالي الديون المستحقة",
                        iqdValue = Formatters.formatIqd(uiState.totalOutstandingDebtIqd),
                        usdValue = Formatters.formatUsd(uiState.totalOutstandingDebtUsd),
                        subtitle = "منفصلة بدقة (IQD | USD)",
                        icon = Icons.Default.TrendingDown,
                        accentColor = colors.debtCrimson,
                        softBackgroundColor = colors.debtCrimsonSoft,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_outstanding_debt"),
                        onClick = { onSelectFilterAndGoToDebtors(DebtorFilter.HIGHEST_IQD) }
                    )
                    DualCurrencyKpiCard(
                        title = "إجمالي التسديدات المحصلة",
                        iqdValue = Formatters.formatIqd(uiState.totalCollectedPaymentsIqd),
                        usdValue = Formatters.formatUsd(uiState.totalCollectedPaymentsUsd),
                        subtitle = "تحصيل د.ع: ${Formatters.formatNumber(uiState.collectionRatioIqd)}% | $: ${Formatters.formatNumber(uiState.collectionRatioUsd)}%",
                        icon = Icons.Default.TrendingUp,
                        accentColor = colors.paymentEmerald,
                        softBackgroundColor = colors.paymentEmeraldSoft,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_collected_payments"),
                        onClick = { onNavigateToScreen(AppScreen.REPORTS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DualCurrencyKpiCard(
                        title = "إجمالي الديون الكلية المسجلة",
                        iqdValue = Formatters.formatIqd(uiState.totalGrossDebtIqd),
                        usdValue = Formatters.formatUsd(uiState.totalGrossDebtUsd),
                        subtitle = "إجمالي حركة السجل بالعملتين",
                        icon = Icons.Default.AccountBalance,
                        accentColor = colors.primaryInteractive,
                        softBackgroundColor = colors.primaryInteractiveSoft,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_net_liquidity"),
                        onClick = { onNavigateToScreen(AppScreen.REPORTS) }
                    )
                    FinancialKpiCard(
                        title = "المدينون النشطون",
                        value = "${uiState.activeDebtorsCount} عميل",
                        subtitle = "متأخرون: ${uiState.overdueDebtorsCount} • مسددون: ${uiState.settledDebtorsCount}",
                        icon = Icons.Default.Groups,
                        accentColor = if (uiState.overdueDebtorsCount > 0) colors.warningAmber else colors.primaryInteractive,
                        softBackgroundColor = if (uiState.overdueDebtorsCount > 0) colors.warningAmberSoft else colors.primaryInteractiveSoft,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_active_debtors"),
                        onClick = { onSelectFilterAndGoToDebtors(DebtorFilter.ACTIVE) }
                    )
                }
            }
        }

        // Optional Approximate Unified Estimate Card (Based on Exchange Rate Setting)
        item {
            AnimatedVisibility(visible = showUnifiedEstimate) {
                val unifiedRemainingIqd = uiState.totalOutstandingDebtIqd + (uiState.totalOutstandingDebtUsd * usdToIqdRate)
                val unifiedRemainingUsd = if (usdToIqdRate > 0) unifiedRemainingIqd / usdToIqdRate else 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenExchangeRateDialog() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface),
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = colors.primaryInteractive,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الإجمالي التقديري الموحد للديون المستحقة (بسعر صرف 1$ = ${Formatters.formatNumber(usdToIqdRate)} د.ع)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.primaryInteractive
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ما يعادل تقريباً: ${Formatters.formatIqd(unifiedRemainingIqd)}  (أو ${Formatters.formatUsd(unifiedRemainingUsd)})",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "تعديل اسم المكتب وسعر الصرف",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Interactive Comparison Chart with IQD / USD Toggle
        item {
            DebtVsPaymentInteractiveChart(
                title = "حركة الديون والتسديدات (آخر 6 أشهر)",
                subtitle = "بدّل بين (د.ع IQD) و ($ USD) واضغط على أي شهر للتفاصيل",
                points = sixMonthPoints
            )
        }

        // Priority Debtors & Quick Settlement
        item {
            val priorityDebtors = uiState.debtorSummaries
                .filter { it.hasActiveDebt }
                .sortedWith(
                    compareByDescending<DebtorSummary> { it.isOverdue }
                        .thenByDescending { it.currentBalanceIqd }
                )
                .take(4)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "متابعة الذمم المالية والتسديد السريع",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "الأرصدة مفصولة بوضوح: (الرصيد بالدينار العراقي | الرصيد بالدولار الأمريكي)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        TextButton(onClick = { onNavigateToScreen(AppScreen.DEBTORS) }) {
                            Text("عرض الكل")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (priorityDebtors.isEmpty()) {
                        Text(
                            text = if (uiState.debtors.isEmpty())
                                "لا يوجد مدينون مسجلون حالياً. اضغط على «إضافة مدين جديد» للبدء."
                            else
                                "جميع الحسابات مسددة بالكامل حالياً.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.debtors.isEmpty()) colors.textSecondary else colors.paymentEmerald,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        priorityDebtors.forEachIndexed { idx, summary ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = colors.subtleBorder
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenDebtorProfile(summary.debtor.id) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = summary.debtor.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (summary.isOverdue) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = colors.debtCrimsonSoft,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.WarningAmber,
                                                        contentDescription = null,
                                                        tint = colors.debtCrimson,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "متأخر عن السداد",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colors.debtCrimson
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "الرصيد بالدينار: ${Formatters.formatIqd(summary.currentBalanceIqd)}  |  الرصيد بالدولار: ${Formatters.formatUsd(summary.currentBalanceUsd)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }

                                val preferredCurrency = if (summary.currentBalanceIqd > 0.009) {
                                    CurrencyCode.IQD
                                } else {
                                    CurrencyCode.USD
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            ExportAndBackupManager.sendDebtReminderViaWhatsApp(
                                                context = context,
                                                officeName = officeName,
                                                summary = summary
                                            )
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "تذكير واتساب",
                                            tint = colors.paymentEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onOpenRecordTransaction(
                                                    summary.debtor.id,
                                                    TransactionType.PAYMENT,
                                                    preferredCurrency
                                                )
                                            }
                                            .testTag("dashboard_settle_debtor_${summary.debtor.id}"),
                                        color = colors.paymentEmeraldSoft,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, colors.paymentEmerald)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = colors.paymentEmerald,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تسديد",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = colors.paymentEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Feed
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أحدث العمليات المسجلة",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        TextButton(onClick = { onNavigateToScreen(AppScreen.REPORTS) }) {
                            Text("سجل الجرد الكامل")
                        }
                    }

                    val recentTxs = uiState.transactions.take(6)
                    if (recentTxs.isEmpty()) {
                        Text(
                            text = "لا توجد عمليات مسجلة بعد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        recentTxs.forEachIndexed { idx, tx ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = colors.subtleBorder
                                )
                            }
                            val debtor = uiState.debtorMap[tx.debtorId]
                            val isPayment = tx.type == TransactionType.PAYMENT
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        debtor?.let { onOpenDebtorProfile(it.id) }
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPayment) colors.paymentEmeraldSoft else colors.debtCrimsonSoft
                                            ),
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
                                                text = debtor?.name ?: "عميل #${tx.debtorId}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
                                        }
                                        Text(
                                            text = "${tx.note} • ${Formatters.formatDateTime(tx.timestamp)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Text(
                                    text = (if (isPayment) "+ " else "- ") + Formatters.formatByCurrency(tx.amount, tx.currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isPayment) colors.paymentEmerald else colors.debtCrimson,
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
