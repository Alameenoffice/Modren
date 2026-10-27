package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.DebtorSummary
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.StatementEntry
import com.example.data.local.TransactionType
import com.example.ui.DebtorFilter
import com.example.ui.LedgerUiState
import com.example.ui.components.DeleteDebtorConfirmationDialog
import com.example.ui.components.DeleteTransactionConfirmationDialog
import com.example.ui.components.EditTransactionDialog
import com.example.ui.theme.LocalLedgerColors
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters

@Composable
fun DebtorsListScreen(
    uiState: LedgerUiState,
    officeName: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (DebtorFilter) -> Unit,
    onOpenAddDebtor: () -> Unit,
    onOpenEditDebtor: (DebtorEntity) -> Unit,
    onDeleteDebtor: (Long) -> Unit,
    onOpenDebtorProfile: (Long) -> Unit,
    onOpenRecordTransaction: (debtorId: Long, initialType: String, initialCurrency: String) -> Unit,
    onTriggerClearanceModal: (DebtorSummary) -> Unit
) {
    val colors = LocalLedgerColors.current
    val context = LocalContext.current
    var debtorPendingDeletion by remember { mutableStateOf<DebtorSummary?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("debtors_list_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إدارة المدينين والعملاء (${uiState.filteredSummaries.size})",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "فصل تام للأرصدة: (الرصيد بالدينار العراقي د.ع | الرصيد بالدولار الأمريكي $)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Button(
                            onClick = onOpenAddDebtor,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive),
                            modifier = Modifier.testTag("debtors_screen_add_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مدين جديد", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("ابحث باسم المدين، رقم الهاتف، أو المحافظة...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "بحث")
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debtors_search_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DebtorFilter.entries.forEach { filter ->
                            val selected = uiState.selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { onFilterChange(filter) },
                                label = { Text(filter.labelAr) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryInteractiveSoft,
                                    selectedLabelColor = colors.primaryInteractive
                                ),
                                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        if (uiState.debtors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debtors_empty_state_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(colors.primaryInteractiveSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = colors.primaryInteractive,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا يوجد أي مدين مسجل في السجل حالياً",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "أضف أول مدين لتسجيل الديون والدفعات بالدينار العراقي والدولار الأمريكي ومتابعة مواعيد الاستحقاق.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenAddDebtor,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة أول مدين", color = Color.White)
                        }
                    }
                }
            }
        } else if (uiState.filteredSummaries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد نتائج مطابقة للبحث أو الفلتر المحدد",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(uiState.filteredSummaries, key = { it.debtor.id }) { summary ->
                val preferredCurrency = if (summary.currentBalanceIqd > 0.009) {
                    CurrencyCode.IQD
                } else if (summary.currentBalanceUsd > 0.009) {
                    CurrencyCode.USD
                } else {
                    CurrencyCode.IQD
                }
                DebtorSummaryCard(
                    summary = summary,
                    onClickProfile = { onOpenDebtorProfile(summary.debtor.id) },
                    onEdit = { onOpenEditDebtor(summary.debtor) },
                    onRequestDelete = { debtorPendingDeletion = summary },
                    onSendWhatsAppReminder = {
                        ExportAndBackupManager.sendDebtReminderViaWhatsApp(
                            context = context,
                            officeName = officeName,
                            summary = summary
                        )
                    },
                    onRecordPayment = {
                        onOpenRecordTransaction(
                            summary.debtor.id,
                            TransactionType.PAYMENT,
                            preferredCurrency
                        )
                    },
                    onRecordDebt = {
                        onOpenRecordTransaction(
                            summary.debtor.id,
                            TransactionType.DEBT,
                            preferredCurrency
                        )
                    },
                    onShowClearance = { onTriggerClearanceModal(summary) }
                )
            }
        }
    }

    debtorPendingDeletion?.let { targetSummary ->
        DeleteDebtorConfirmationDialog(
            summary = targetSummary,
            onDismiss = { debtorPendingDeletion = null },
            onConfirmDelete = {
                onDeleteDebtor(targetSummary.debtor.id)
                debtorPendingDeletion = null
            }
        )
    }
}

@Composable
private fun DebtorSummaryCard(
    summary: DebtorSummary,
    onClickProfile: () -> Unit,
    onEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onSendWhatsAppReminder: () -> Unit,
    onRecordPayment: () -> Unit,
    onRecordDebt: () -> Unit,
    onShowClearance: () -> Unit
) {
    val colors = LocalLedgerColors.current
    val debtor = summary.debtor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickProfile() }
            .testTag("debtor_card_${debtor.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(
            width = if (summary.isOverdue) 1.5.dp else 1.dp,
            color = when {
                summary.isOverdue -> colors.debtCrimson
                summary.isSettled -> colors.paymentEmerald.copy(alpha = 0.6f)
                else -> colors.subtleBorder
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debtor.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (debtor.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = debtor.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        if (debtor.address.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = debtor.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (debtor.dueDateMillis != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = if (summary.isOverdue) colors.debtCrimson else colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (summary.isOverdue)
                                    "تاريخ الاستحقاق: ${Formatters.formatDate(debtor.dueDateMillis)} (متأخر عن السداد منذ ${summary.overdueDays} يوم)"
                                else
                                    "تاريخ الاستحقاق: ${Formatters.formatDate(debtor.dueDateMillis)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (summary.isOverdue) colors.debtCrimson else colors.textMuted,
                                fontWeight = if (summary.isOverdue) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                val (badgeLabel, badgeBg, badgeFg) = when {
                    summary.isSettled -> Triple("مسدد بالكامل ✓", colors.paymentEmeraldSoft, colors.paymentEmerald)
                    summary.isOverdue -> Triple("متأخر عن السداد", colors.debtCrimsonSoft, colors.debtCrimson)
                    summary.isOverLimit -> Triple("تجاوز السقف", colors.warningAmberSoft, colors.warningAmber)
                    else -> Triple("نشط", colors.primaryInteractiveSoft, colors.primaryInteractive)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Strictly Separated Dual-Currency Boxes: (الرصيد بالدينار العراقي | الرصيد بالدولار الأمريكي)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Iraqi Dinar Box (IQD)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.elevatedSurface,
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "الرصيد بالدينار العراقي (IQD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = Formatters.formatIqd(summary.currentBalanceIqd),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (summary.currentBalanceIqd > 0.009) colors.debtCrimson else colors.paymentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المسدد: ${Formatters.formatIqd(summary.totalPaidIqd)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // US Dollar Box (USD)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.elevatedSurface,
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "الرصيد بالدولار الأمريكي (USD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = Formatters.formatUsd(summary.currentBalanceUsd),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (summary.currentBalanceUsd > 0.009) colors.debtCrimson else colors.paymentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المسدد: ${Formatters.formatUsd(summary.totalPaidUsd)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (summary.isSettled) {
                    Button(
                        onClick = onShowClearance,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سند براءة ذمة", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسديد", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onRecordDebt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.debtCrimson.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.debtCrimson,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("إضافة دين", style = MaterialTheme.typography.labelLarge, color = colors.debtCrimson)
                }

                OutlinedButton(
                    onClick = onClickProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.subtleBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = colors.primaryInteractive,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("كشف حساب", style = MaterialTheme.typography.labelLarge, color = colors.primaryInteractive)
                }

                IconButton(
                    onClick = onSendWhatsAppReminder,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("whatsapp_reminder_${debtor.id}")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "تذكير بالدين عبر واتساب",
                        tint = colors.paymentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("edit_debtor_${debtor.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل المدين",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_debtor_${debtor.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "حذف المدين",
                        tint = colors.debtCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DebtorStatementScreen(
    summary: DebtorSummary,
    officeName: String,
    statementEntries: List<StatementEntry>,
    onBack: () -> Unit,
    onEditDebtor: (DebtorEntity) -> Unit,
    onDeleteDebtor: (Long) -> Unit,
    onRecordTransaction: (debtorId: Long, initialType: String, initialCurrency: String) -> Unit,
    onSettleAllBalancesNow: (DebtorSummary) -> Unit,
    onUpdateTransaction: (
        existingTransaction: LedgerTransactionEntity,
        newType: String,
        newCurrency: String,
        newAmount: Double,
        newNote: String
    ) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onTriggerPaymentReceiptModal: (DebtorEntity, StatementEntry) -> Unit,
    onTriggerClearanceModal: (DebtorSummary) -> Unit
) {
    BackHandler { onBack() }

    val colors = LocalLedgerColors.current
    val context = LocalContext.current
    val debtor = summary.debtor

    val reversedEntries = remember(statementEntries) { statementEntries.reversed() }

    var showDeleteDebtorDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<LedgerTransactionEntity?>(null) }
    var deletingTransaction by remember { mutableStateOf<LedgerTransactionEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("debtor_statement_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("statement_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                            Column {
                                Text(
                                    text = "$officeName • كشف حساب مزدوج العملة",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.primaryInteractive
                                )
                                Text(
                                    text = debtor.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    ExportAndBackupManager.sendDebtReminderViaWhatsApp(
                                        context = context,
                                        officeName = officeName,
                                        summary = summary
                                    )
                                },
                                modifier = Modifier.testTag("statement_whatsapp_reminder_button")
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "إرسال تذكير بالدين عبر واتساب",
                                    tint = colors.paymentEmerald
                                )
                            }
                            IconButton(onClick = { onEditDebtor(debtor) }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = colors.textSecondary)
                            }
                            IconButton(
                                onClick = { showDeleteDebtorDialog = true },
                                modifier = Modifier.testTag("statement_delete_debtor_button")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المدين", tint = colors.debtCrimson)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "الهاتف: ${debtor.phone.ifBlank { "غير مسجل" }} • العنوان: ${debtor.address.ifBlank { "غير مسجل" }} • الاستحقاق: ${Formatters.formatDate(debtor.dueDateMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    if (summary.isOverdue) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.debtCrimsonSoft,
                            border = BorderStroke(1.dp, colors.debtCrimson)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تنبيه استحقاق: هذا المدين متأخر عن السداد منذ ${summary.overdueDays} يوم (${Formatters.formatDate(debtor.dueDateMillis)})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.debtCrimson,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two Side-by-Side Currency Balance Panels (IQD | USD)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.elevatedSurface,
                            border = BorderStroke(1.dp, colors.subtleBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "حساب الدينار العراقي (IQD)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.primaryInteractive
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الديون: ${Formatters.formatIqd(summary.totalDebtIqd)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "المسدد: ${Formatters.formatIqd(summary.totalPaidIqd)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.paymentEmerald
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "المتبقي: ${Formatters.formatIqd(summary.currentBalanceIqd)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (summary.currentBalanceIqd > 0.009) colors.debtCrimson else colors.paymentEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.elevatedSurface,
                            border = BorderStroke(1.dp, colors.subtleBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "حساب الدولار الأمريكي (USD)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.primaryInteractive
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الديون: ${Formatters.formatUsd(summary.totalDebtUsd)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "المسدد: ${Formatters.formatUsd(summary.totalPaidUsd)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.paymentEmerald
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "المتبقي: ${Formatters.formatUsd(summary.currentBalanceUsd)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (summary.currentBalanceUsd > 0.009) colors.debtCrimson else colors.paymentEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (IQD / USD Payment & Debt)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val defCurr = if (summary.currentBalanceIqd > 0.009) CurrencyCode.IQD else CurrencyCode.USD
                                onRecordTransaction(debtor.id, TransactionType.PAYMENT, defCurr)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل تسديد (د.ع / $)", color = Color.White)
                        }

                        Button(
                            onClick = {
                                onRecordTransaction(debtor.id, TransactionType.DEBT, CurrencyCode.IQD)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.debtCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة دين (د.ع / $)", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Smart WhatsApp Claim / Reminder Button
                    OutlinedButton(
                        onClick = {
                            ExportAndBackupManager.sendDebtReminderViaWhatsApp(
                                context = context,
                                officeName = officeName,
                                summary = summary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.paymentEmerald)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = colors.paymentEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال تذكير بالدين / مطالبة مالية عبر واتساب باسم «$officeName»",
                            color = colors.paymentEmerald,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Full Balance Settlement or Clearance Certificate
                    if (summary.hasActiveDebt) {
                        OutlinedButton(
                            onClick = { onSettleAllBalancesNow(summary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("statement_instant_full_settlement_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, colors.paymentEmerald)
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = colors.paymentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تصفير كامل الأرصدة المتبقية الآن (${Formatters.formatIqd(summary.currentBalanceIqd)} | ${Formatters.formatUsd(summary.currentBalanceUsd)})",
                                color = colors.paymentEmerald,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else if (summary.isSettled) {
                        Button(
                            onClick = { onTriggerClearanceModal(summary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("statement_open_clearance_certificate_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "عرض ومشاركة سند براءة الذمة والمخالصة المالية",
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Export CSV & Print PDF Statement Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = ExportAndBackupManager.buildStatementCsv(
                                    debtor = debtor,
                                    entries = statementEntries,
                                    officeName = officeName
                                )
                                ExportAndBackupManager.saveAndShareFile(
                                    context = context,
                                    fileName = "statement_${debtor.id}.csv",
                                    mimeType = "text/csv",
                                    contentBytes = csv.toByteArray(Charsets.UTF_8),
                                    shareTitle = "تصدير كشف حساب ${debtor.name} (Excel CSV)"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير Excel CSV")
                        }

                        OutlinedButton(
                            onClick = {
                                val pdf = ExportAndBackupManager.generateStatementOrReportPdfFile(
                                    context = context,
                                    title = "كشف حساب تفصيلي: ${debtor.name}",
                                    subtitle = "الهاتف: ${debtor.phone} • العنوان: ${debtor.address}",
                                    summaryLines = listOf(
                                        "حساب الدينار (IQD): الديون ${Formatters.formatIqd(summary.totalDebtIqd)} | المسدد ${Formatters.formatIqd(summary.totalPaidIqd)} | المتبقي ${Formatters.formatIqd(summary.currentBalanceIqd)}",
                                        "حساب الدولار (USD): الديون ${Formatters.formatUsd(summary.totalDebtUsd)} | المسدد ${Formatters.formatUsd(summary.totalPaidUsd)} | المتبقي ${Formatters.formatUsd(summary.currentBalanceUsd)}"
                                    ),
                                    tableHeaders = listOf("التاريخ", "العملية والعملة", "المبلغ", "تراكمي د.ع", "تراكمي $"),
                                    tableRows = statementEntries.map { e ->
                                        val typeAr = if (e.transaction.type == TransactionType.DEBT) "دين" else "تسديد"
                                        listOf(
                                            Formatters.formatDate(e.transaction.timestamp),
                                            "$typeAr (${e.transaction.currency})",
                                            Formatters.formatByCurrency(e.transaction.amount, e.transaction.currency),
                                            Formatters.formatIqd(e.runningBalanceIqd),
                                            Formatters.formatUsd(e.runningBalanceUsd)
                                        )
                                    },
                                    fileNamePrefix = "statement_${debtor.id}",
                                    officeName = officeName
                                )
                                ExportAndBackupManager.printPdfFile(
                                    context = context,
                                    pdfFile = pdf,
                                    jobName = "كشف_حساب_${debtor.name}"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة / PDF")
                        }
                    }
                }
            }
        }

        // Chronological Statement Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل العمليات والرصيد التراكمي للعملتين (${statementEntries.size} عملية)",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "تعديل / حذف / وصل قبض",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        if (reversedEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Text(
                        text = "لا توجد عمليات مالية مسجلة لهذا العميل حتى الآن.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(reversedEntries, key = { it.transaction.id }) { entry ->
                val tx = entry.transaction
                val isPayment = tx.type == TransactionType.PAYMENT

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (isPayment) colors.paymentEmeraldSoft else colors.debtCrimsonSoft,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (isPayment) "دفعة تسديد" else "إضافة دين",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isPayment) colors.paymentEmerald else colors.debtCrimson,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = colors.primaryInteractiveSoft,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = tx.currency,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.primaryInteractive,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Formatters.formatDateTime(tx.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tx.note,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "الرصيد التراكمي بعدها: ${Formatters.formatIqd(entry.runningBalanceIqd)}  |  ${Formatters.formatUsd(entry.runningBalanceUsd)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.primaryInteractive,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isPayment) "- " else "+ ") + Formatters.formatByCurrency(tx.amount, tx.currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isPayment) colors.paymentEmerald else colors.debtCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isPayment) {
                                        IconButton(
                                            onClick = { onTriggerPaymentReceiptModal(debtor, entry) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("tx_receipt_${tx.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.ReceiptLong,
                                                contentDescription = "إرسال وصل قبض واتساب",
                                                tint = colors.paymentEmerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { editingTransaction = tx },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("tx_edit_${tx.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "تعديل العملية",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { deletingTransaction = tx },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("tx_delete_${tx.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "حذف العملية",
                                            tint = colors.debtCrimson,
                                            modifier = Modifier.size(18.dp)
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

    if (showDeleteDebtorDialog) {
        DeleteDebtorConfirmationDialog(
            summary = summary,
            onDismiss = { showDeleteDebtorDialog = false },
            onConfirmDelete = {
                onDeleteDebtor(debtor.id)
                showDeleteDebtorDialog = false
            }
        )
    }

    editingTransaction?.let { targetTx ->
        EditTransactionDialog(
            transaction = targetTx,
            onDismiss = { editingTransaction = null },
            onSave = { newType, newCurrency, newAmount, newNote ->
                onUpdateTransaction(targetTx, newType, newCurrency, newAmount, newNote)
                editingTransaction = null
            }
        )
    }

    deletingTransaction?.let { targetTx ->
        DeleteTransactionConfirmationDialog(
            transaction = targetTx,
            onDismiss = { deletingTransaction = null },
            onConfirmDelete = {
                onDeleteTransaction(targetTx.id)
                deletingTransaction = null
            }
        )
    }
}
