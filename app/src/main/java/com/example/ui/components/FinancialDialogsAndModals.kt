package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ClearanceEvent
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.DebtorSummary
import com.example.data.local.TransactionType
import com.example.ui.theme.LocalLedgerColors
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters

@Composable
fun ClearanceCelebrationDialog(
    event: ClearanceEvent,
    onDismiss: () -> Unit
) {
    val colors = LocalLedgerColors.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("clearance_celebration_modal"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.5.dp, colors.paymentEmerald),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF065F46), Color(0xFF10B981))
                            )
                        )
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "تصفير الحساب",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "إغلاق الذمة المالية وتصفير الحساب!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سند براءة ذمة ومخالصة مالية رسمية • ${event.referenceNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD1FAE5),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تم بحمد الله سداد كامل المبلغ المتبقي بالعملتين على العميل:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.debtor.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (event.debtor.phone.isNotBlank()) {
                        Text(
                            text = "هاتف: ${event.debtor.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = colors.elevatedSurface,
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CertificateRow(
                                label = "الدفعة الأخيرة المسددة:",
                                value = Formatters.formatByCurrency(
                                    event.lastPaymentAmount,
                                    event.lastPaymentCurrency
                                ),
                                valueColor = colors.paymentEmerald
                            )
                            CertificateRow(
                                label = "إجمالي المسدد بالدينار (IQD):",
                                value = Formatters.formatIqd(event.totalPaidIqd),
                                valueColor = colors.textPrimary
                            )
                            CertificateRow(
                                label = "إجمالي المسدد بالدولار (USD):",
                                value = Formatters.formatUsd(event.totalPaidUsd),
                                valueColor = colors.textPrimary
                            )
                            CertificateRow(
                                label = "تاريخ إغلاق الذمة:",
                                value = Formatters.formatDateTime(event.clearanceTimestamp),
                                valueColor = colors.textSecondary
                            )
                            HorizontalDivider(color = colors.subtleBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الرصيد المتبقي بالعملتين:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Surface(
                                    color = colors.paymentEmeraldSoft,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "0 د.ع | 0 $ (ذمة خالية)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.paymentEmerald,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "صدقة جارية عن أحمد أبو رسلان",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primaryInteractive
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            ExportAndBackupManager.shareClearanceCertificate(context, event)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_clearance_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إصدار ومشاركة سند براءة ذمة (واتساب / PDF)",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val pdf = ExportAndBackupManager.generateClearancePdfFile(context, event)
                            ExportAndBackupManager.printPdfFile(
                                context = context,
                                pdfFile = pdf,
                                jobName = "سند_براءة_ذمة_${event.debtor.name}"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("print_clearance_pdf_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "طباعة المخالصة المالية / حفظ كملف PDF",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_clearance_modal_button")
                    ) {
                        Text("إغلاق النافذة", color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateRow(
    label: String,
    value: String,
    valueColor: Color
) {
    val colors = LocalLedgerColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTransactionDialog(
    summaries: List<DebtorSummary>,
    preselectedDebtorId: Long? = null,
    initialType: String = TransactionType.PAYMENT,
    initialCurrency: String = CurrencyCode.IQD,
    onDismiss: () -> Unit,
    onConfirm: (debtorId: Long, type: String, currency: String, amount: Double, note: String) -> Unit
) {
    val colors = LocalLedgerColors.current
    var selectedSummary by remember {
        mutableStateOf(
            summaries.find { it.debtor.id == preselectedDebtorId } ?: summaries.firstOrNull()
        )
    }
    var txType by remember { mutableStateOf(initialType) }
    var txCurrency by remember { mutableStateOf(initialCurrency) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("record_transaction_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.primaryInteractive
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل عملية مالية جديدة",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Transaction Type Selector (Payment vs Debt)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isPayment = txType == TransactionType.PAYMENT
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { txType = TransactionType.PAYMENT }
                            .testTag("select_type_payment"),
                        color = if (isPayment) colors.paymentEmeraldSoft else colors.elevatedSurface,
                        border = BorderStroke(
                            width = if (isPayment) 1.5.dp else 1.dp,
                            color = if (isPayment) colors.paymentEmerald else colors.subtleBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (isPayment) colors.paymentEmerald else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تسديد دفعة",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isPayment) colors.paymentEmerald else colors.textSecondary
                            )
                        }
                    }

                    val isDebt = txType == TransactionType.DEBT
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { txType = TransactionType.DEBT }
                            .testTag("select_type_debt"),
                        color = if (isDebt) colors.debtCrimsonSoft else colors.elevatedSurface,
                        border = BorderStroke(
                            width = if (isDebt) 1.5.dp else 1.dp,
                            color = if (isDebt) colors.debtCrimson else colors.subtleBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isDebt) colors.debtCrimson else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إضافة دين جديد",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isDebt) colors.debtCrimson else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Dual Currency Selector (IQD vs USD)
                Text(
                    text = "اختر عملة المعاملة (فصل محاسبي تام):",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isIqd = txCurrency == CurrencyCode.IQD
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { txCurrency = CurrencyCode.IQD }
                            .testTag("select_currency_iqd"),
                        color = if (isIqd) colors.primaryInteractiveSoft else colors.elevatedSurface,
                        border = BorderStroke(
                            width = if (isIqd) 1.5.dp else 1.dp,
                            color = if (isIqd) colors.primaryInteractive else colors.subtleBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "دينار عراقي (د.ع)",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isIqd) colors.primaryInteractive else colors.textPrimary
                            )
                            Text(
                                text = "IQD",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    val isUsd = txCurrency == CurrencyCode.USD
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { txCurrency = CurrencyCode.USD }
                            .testTag("select_currency_usd"),
                        color = if (isUsd) colors.primaryInteractiveSoft else colors.elevatedSurface,
                        border = BorderStroke(
                            width = if (isUsd) 1.5.dp else 1.dp,
                            color = if (isUsd) colors.primaryInteractive else colors.subtleBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "دولار أمريكي ($)",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isUsd) colors.primaryInteractive else colors.textPrimary
                            )
                            Text(
                                text = "USD",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Debtor Picker
                if (preselectedDebtorId == null) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSummary?.let {
                                "${it.debtor.name} (${Formatters.formatIqd(it.currentBalanceIqd)} | ${Formatters.formatUsd(it.currentBalanceUsd)})"
                            } ?: "اختر العميل / المدين",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("العميل / المدين") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            summaries.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = item.debtor.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "المتبقي: ${Formatters.formatIqd(item.currentBalanceIqd)} | ${Formatters.formatUsd(item.currentBalanceUsd)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (item.hasActiveDebt) colors.debtCrimson else colors.paymentEmerald
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSummary = item
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    selectedSummary?.let { summary ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.elevatedSurface,
                            border = BorderStroke(1.dp, colors.subtleBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = summary.debtor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "الرصيد بالدينار: ${Formatters.formatIqd(summary.currentBalanceIqd)}  |  الرصيد بالدولار: ${Formatters.formatUsd(summary.currentBalanceUsd)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (summary.hasActiveDebt) colors.debtCrimson else colors.paymentEmerald
                                )
                            }
                        }
                    }
                }

                // Quick Full Settlement Button for the chosen currency
                val balanceForSelectedCurrency = if (txCurrency == CurrencyCode.USD) {
                    selectedSummary?.currentBalanceUsd ?: 0.0
                } else {
                    selectedSummary?.currentBalanceIqd ?: 0.0
                }

                AnimatedVisibility(visible = txType == TransactionType.PAYMENT && balanceForSelectedCurrency > 0.009) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                amountText = if (txCurrency == CurrencyCode.IQD) {
                                    balanceForSelectedCurrency.toLong().toString()
                                } else {
                                    balanceForSelectedCurrency.toString()
                                }
                                if (noteText.isBlank()) {
                                    noteText = "سداد كامل الرصيد المتبقي (${Formatters.currencyShortSymbol(txCurrency)}) وتصفية الحساب"
                                }
                            }
                            .testTag("quick_settle_full_balance_chip"),
                        color = colors.paymentEmeraldSoft,
                        border = BorderStroke(1.dp, colors.paymentEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = colors.paymentEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "سداد كامل المتبقي بهذه العملة",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.paymentEmerald
                                )
                            }
                            Text(
                                text = Formatters.formatByCurrency(balanceForSelectedCurrency, txCurrency),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.paymentEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = {
                        Text(
                            if (txCurrency == CurrencyCode.USD)
                                "المبلغ بالدولار الأمريكي ($ USD)"
                            else
                                "المبلغ بالدينار العراقي (د.ع IQD)"
                        )
                    },
                    placeholder = {
                        Text(if (txCurrency == CurrencyCode.USD) "مثال: 1500" else "مثال: 250000")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("البيان / ملاحظات العملية") },
                    placeholder = {
                        Text(
                            if (txType == TransactionType.PAYMENT)
                                "مثال: تسديد نقدي / وصل قبض رقم..."
                            else
                                "مثال: قائمة تجهيز بضاعة رقم..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.debtCrimson
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val debtorId = selectedSummary?.debtor?.id
                            val parsedAmount = amountText.replace(",", "").toDoubleOrNull()
                            if (debtorId == null) {
                                errorMessage = "يرجى اختيار العميل أولاً"
                                return@Button
                            }
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                                return@Button
                            }
                            onConfirm(debtorId, txType, txCurrency, parsedAmount, noteText)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("confirm_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (txType == TransactionType.PAYMENT)
                                colors.paymentEmerald
                            else
                                colors.debtCrimson
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (txType == TransactionType.PAYMENT)
                                "حفظ التسديد (${Formatters.currencyShortSymbol(txCurrency)})"
                            else
                                "تسجيل الدين (${Formatters.currencyShortSymbol(txCurrency)})",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebtorFormDialog(
    existingDebtor: DebtorEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        existingId: Long?,
        name: String,
        phone: String,
        address: String,
        creditLimitIqd: Double,
        creditLimitUsd: Double,
        dueDateMillis: Long?,
        notes: String,
        initialDebtAmount: Double,
        initialDebtCurrency: String,
        initialDebtNote: String
    ) -> Unit
) {
    val colors = LocalLedgerColors.current
    val isEditing = existingDebtor != null

    var name by remember { mutableStateOf(existingDebtor?.name ?: "") }
    var phone by remember { mutableStateOf(existingDebtor?.phone ?: "") }
    var address by remember { mutableStateOf(existingDebtor?.address ?: "") }
    var creditLimitIqdText by remember {
        mutableStateOf(
            if ((existingDebtor?.creditLimitIqd ?: 0.0) > 0.0)
                existingDebtor!!.creditLimitIqd.toLong().toString()
            else
                "5000000"
        )
    }
    var creditLimitUsdText by remember {
        mutableStateOf(
            if ((existingDebtor?.creditLimitUsd ?: 0.0) > 0.0)
                existingDebtor!!.creditLimitUsd.toLong().toString()
            else
                "5000"
        )
    }
    var notes by remember { mutableStateOf(existingDebtor?.notes ?: "") }
    var initialDebtText by remember { mutableStateOf("") }
    var initialDebtCurrency by remember { mutableStateOf(CurrencyCode.IQD) }
    var initialDebtNote by remember { mutableStateOf("رصيد افتتاحي مسجل عند فتح الحساب") }
    var dueDaysPreset by remember { mutableStateOf<Int?>(30) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("debtor_form_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = colors.primaryInteractive
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "تعديل بيانات المدين" else "إضافة مدين / عميل جديد",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("اسم المدين / الشركة *") },
                    placeholder = { Text("مثال: شركة الفرات للتجارة العامة") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debtor_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        placeholder = { Text("077XXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debtor_phone_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان / المحافظة") },
                        placeholder = { Text("بغداد - الكرادة") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debtor_address_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = creditLimitIqdText,
                        onValueChange = { creditLimitIqdText = it },
                        label = { Text("سقف الدين (د.ع IQD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debtor_limit_iqd_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = creditLimitUsdText,
                        onValueChange = { creditLimitUsdText = it },
                        label = { Text("سقف الدين ($ USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debtor_limit_usd_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تاريخ الاستحقاق المجدول:",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val options = listOf(
                        7 to "بعد 7 أيام",
                        15 to "بعد 15 يوم",
                        30 to "بعد 30 يوم",
                        -3 to "متأخر (تجربة تنبيه)",
                        null to "بدون تاريخ"
                    )
                    options.forEach { (days, label) ->
                        FilterChip(
                            selected = dueDaysPreset == days,
                            onClick = { dueDaysPreset = days },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryInteractiveSoft,
                                selectedLabelColor = colors.primaryInteractive
                            )
                        )
                    }
                }

                if (!isEditing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = colors.elevatedSurface,
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "رصيد دين افتتاحي (اختياري):",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = initialDebtCurrency == CurrencyCode.IQD,
                                        onClick = { initialDebtCurrency = CurrencyCode.IQD },
                                        label = { Text("د.ع IQD") }
                                    )
                                    FilterChip(
                                        selected = initialDebtCurrency == CurrencyCode.USD,
                                        onClick = { initialDebtCurrency = CurrencyCode.USD },
                                        label = { Text("$ USD") }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = initialDebtText,
                                    onValueChange = { initialDebtText = it },
                                    label = { Text("المبلغ (${Formatters.currencyShortSymbol(initialDebtCurrency)})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("debtor_initial_debt_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = initialDebtNote,
                                    onValueChange = { initialDebtNote = it },
                                    label = { Text("البيان") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.debtCrimson
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorText = "يرجى إدخال اسم المدين"
                                return@Button
                            }
                            val limitIqd = creditLimitIqdText.replace(",", "").toDoubleOrNull() ?: 0.0
                            val limitUsd = creditLimitUsdText.replace(",", "").toDoubleOrNull() ?: 0.0
                            val initDebt = initialDebtText.replace(",", "").toDoubleOrNull() ?: 0.0
                            val dueMillis = dueDaysPreset?.let {
                                System.currentTimeMillis() + (it * 24L * 60L * 60L * 1000L)
                            } ?: existingDebtor?.dueDateMillis

                            onSave(
                                existingDebtor?.id,
                                name,
                                phone,
                                address,
                                limitIqd,
                                limitUsd,
                                dueMillis,
                                notes,
                                initDebt,
                                initialDebtCurrency,
                                initialDebtNote
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("save_debtor_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                    ) {
                        Text(
                            text = if (isEditing) "حفظ التعديلات" else "إضافة المدين للسجل",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExchangeRateSettingsDialog(
    currentRate: Double,
    showUnifiedEstimate: Boolean,
    onDismiss: () -> Unit,
    onSave: (rate: Double, showUnified: Boolean) -> Unit
) {
    val colors = LocalLedgerColors.current
    var rateText by remember { mutableStateOf(currentRate.toLong().toString()) }
    var showUnified by remember { mutableStateOf(showUnifiedEstimate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exchange_rate_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = colors.primaryInteractive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات سعر الصرف التقريبي",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تبقى كافة السجلات والحسابات الأساسية مفصولة تماماً (IQD | USD)، ويُستخدم سعر الصرف فقط لعرض إجمالي تقديري موحد اختياري في لوحة التحكم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("سعر صرف 1 دولار أمريكي ($) مقابل الدينار (د.ع)") },
                    placeholder = { Text("مثال: 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إظهار الإجمالي التقديري الموحد بلوحة التحكم",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showUnified,
                        onCheckedChange = { showUnified = it }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val parsed = rateText.replace(",", "").toDoubleOrNull() ?: 1500.0
                            onSave(parsed, showUnified)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                    ) {
                        Text("حفظ الإعدادات", color = Color.White)
                    }
                }
            }
        }
    }
}
