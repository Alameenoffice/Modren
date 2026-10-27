package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DebtorSummary
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.theme.LocalLedgerColors
import com.example.util.Formatters

@Composable
fun OnboardingSetupDialog(
    initialOfficeName: String,
    initialRate: Double,
    onComplete: (officeName: String, exchangeRate: Double, optionalPin: String?) -> Unit
) {
    val colors = LocalLedgerColors.current
    var officeName by remember {
        mutableStateOf(
            if (initialOfficeName == "مكتب المحاسبة والتجارة العامة") "" else initialOfficeName
        )
    }
    var exchangeRateText by remember { mutableStateOf(initialRate.toLong().toString()) }
    var optionalPinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Mandatory first-time setup */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("onboarding_setup_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.5.dp, colors.primaryInteractive),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                            )
                        )
                        .padding(vertical = 22.dp, horizontal = 20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "مرحباً بك في نظام سجل الديون والمحاسبة",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "إعداد أولي لمرة واحدة لتخصيص الترويسة، وصولات القبض، وسعر الصرف",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "1. اسم المكتب أو الشركة أو التاجر *",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "سيظهر هذا الاسم بشكل بارز في الشريط العلوي، القائمة الجانبية، التقارير، ووصولات ومطالبات واتساب.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = officeName,
                        onValueChange = {
                            officeName = it
                            errorMessage = null
                        },
                        label = { Text("اسم المكتب أو الشركة أو التاجر") },
                        placeholder = { Text("مثال: شركة النور للتجارة العامة") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_office_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. سعر صرف الدولار مقابل الدينار العراقي *",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "تبقى أرصدة الدينار (د.ع) والدولار ($) مفصولة بدقة، ويُستخدم سعر الصرف لحساب الإجمالي التقديري الموحد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exchangeRateText,
                        onValueChange = {
                            exchangeRateText = it
                            errorMessage = null
                        },
                        label = { Text("سعر صرف 1 دولار ($) بالدينار العراقي (د.ع)") },
                        placeholder = { Text("مثال: 1500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_exchange_rate_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "3. رمز قفل التطبيق الاختياري (PIN من 4 أرقام)",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "اتركه فارغاً إذا كنت لا ترغب بقفل التطبيق، أو أدخل 4 أرقام لحماية السجل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optionalPinText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(4)
                            optionalPinText = digits
                            errorMessage = null
                        },
                        label = { Text("رمز الحماية (4 أرقام - اختياري)") },
                        placeholder = { Text("مثال: 1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.debtCrimson
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (officeName.trim().isEmpty()) {
                                errorMessage = "يرجى إدخال اسم المكتب أو الشركة أو التاجر للبدء"
                                return@Button
                            }
                            val parsedRate = exchangeRateText.replace(",", "").toDoubleOrNull()
                            if (parsedRate == null || parsedRate <= 0.0) {
                                errorMessage = "يرجى إدخال سعر صرف صحيح (مثلاً 1500)"
                                return@Button
                            }
                            if (optionalPinText.isNotEmpty() && optionalPinText.length != 4) {
                                errorMessage = "رمز الحماية يجب أن يتكون من 4 أرقام بالضبط أو يُترك فارغاً"
                                return@Button
                            }
                            onComplete(
                                officeName.trim(),
                                parsedRate,
                                optionalPinText.takeIf { it.length == 4 }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ الإعدادات والبدء باستخدام السجل",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfficeAndExchangeSettingsDialog(
    currentOfficeName: String,
    currentRate: Double,
    showUnifiedEstimate: Boolean,
    onDismiss: () -> Unit,
    onSave: (officeName: String, rate: Double, showUnified: Boolean) -> Unit
) {
    val colors = LocalLedgerColors.current
    var officeNameText by remember { mutableStateOf(currentOfficeName) }
    var rateText by remember { mutableStateOf(currentRate.toLong().toString()) }
    var showUnified by remember { mutableStateOf(showUnifiedEstimate) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = colors.primaryInteractive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات اسم المكتب وسعر الصرف",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يمكنك تعديل اسم المكتب أو الشركة وسعر الصرف المعتمد في الترويسة والتقارير ووصولات القبض في أي وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = officeNameText,
                    onValueChange = {
                        officeNameText = it
                        errorMessage = null
                    },
                    label = { Text("اسم المكتب أو الشركة أو التاجر") },
                    placeholder = { Text("مثال: شركة النور للتجارة العامة") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_office_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        errorMessage = null
                    },
                    label = { Text("سعر صرف 1 دولار أمريكي ($) مقابل الدينار (د.ع)") },
                    placeholder = { Text("مثال: 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_exchange_rate_input"),
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

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (officeNameText.trim().isEmpty()) {
                                errorMessage = "يرجى إدخال اسم المكتب أو الشركة"
                                return@Button
                            }
                            val parsed = rateText.replace(",", "").toDoubleOrNull() ?: 1500.0
                            onSave(officeNameText.trim(), parsed, showUnified)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("save_office_and_rate_button"),
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

@Composable
fun DeleteDebtorConfirmationDialog(
    summary: DebtorSummary,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val colors = LocalLedgerColors.current
    val hasUnpaidDebt = summary.hasActiveDebt
    val unpaidBalanceFormatted = Formatters.formatDualBalanceText(
        summary.currentBalanceIqd,
        summary.currentBalanceUsd
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("delete_debtor_confirmation_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (hasUnpaidDebt) colors.debtCrimson else colors.subtleBorder
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.debtCrimsonSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = colors.debtCrimson,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تأكيد حذف سجل المدين",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = summary.debtor.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.primaryInteractive,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (hasUnpaidDebt) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.debtCrimsonSoft,
                        border = BorderStroke(1.dp, colors.debtCrimson)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تنبيه: هذا المدين لا يزال بذمته مبلغ ($unpaidBalanceFormatted)، هل أنت متأكد من حذفه نهائياً؟",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.debtCrimson,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "هل أنت متأكد من حذف هذا المدين وكافة العمليات المسجلة في كشف حسابه نهائياً؟ لا يمكن التراجع عن هذه العملية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
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
                        Text("تراجع وإلغاء")
                    }
                    Button(
                        onClick = {
                            onConfirmDelete()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("confirm_delete_debtor_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.debtCrimson)
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نعم، احذف نهائياً", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteTransactionConfirmationDialog(
    transaction: LedgerTransactionEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val colors = LocalLedgerColors.current
    val isPayment = transaction.type == TransactionType.PAYMENT
    val typeText = if (isPayment) "دفعة تسديد" else "إضافة دين"
    val amountFormatted = Formatters.formatByCurrency(transaction.amount, transaction.currency)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("delete_transaction_confirmation_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.debtCrimson)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = colors.debtCrimson,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "تأكيد حذف العملية المالية",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "هل أنت متأكد من حذف عملية ($typeText) بقيمة $amountFormatted؟ سيتم إعادة احتساب الأرصدة التراكمية تلقائياً.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
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
                            onConfirmDelete()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("confirm_delete_tx_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.debtCrimson)
                    ) {
                        Text("حذف العملية", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ResetAllDataConfirmationDialog(
    debtorsCount: Int,
    transactionsCount: Int,
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    val colors = LocalLedgerColors.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_all_data_confirmation_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.5.dp, colors.debtCrimson)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.debtCrimsonSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = colors.debtCrimson,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "تصفير ومسح جميع بيانات التطبيق",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.debtCrimson,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.debtCrimsonSoft,
                    border = BorderStroke(1.dp, colors.debtCrimson)
                ) {
                    Text(
                        text = "تحذير هام: سيتم حذف جميع المدينين ($debtorsCount عميل) وجميع العمليات المالية ($transactionsCount عملية) وتصفير السجل بالكامل لبدء سجل جديد تماماً.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.debtCrimson,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
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
                            onConfirmReset()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_reset_all_data_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.debtCrimson)
                    ) {
                        Text("تصفير ومسح الكل نهائياً", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PinSettingsDialog(
    currentPin: String?,
    onDismiss: () -> Unit,
    onSavePin: (String?) -> Unit
) {
    val colors = LocalLedgerColors.current
    var newPin by remember { mutableStateOf(currentPin ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.primaryInteractive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات قفل التطبيق (PIN Lock)",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "عيّن رمز مرور من 4 أرقام لحماية الدخول إلى سجل الديون عند فتح التطبيق.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { input ->
                        newPin = input.filter { it.isDigit() }.take(4)
                        errorMessage = null
                    },
                    label = { Text("رمز المرور (4 أرقام)") },
                    placeholder = { Text("مثال: 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_code_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.debtCrimson
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentPin != null) {
                        OutlinedButton(
                            onClick = {
                                onSavePin(null)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.debtCrimson)
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = colors.debtCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إلغاء القفل", color = colors.debtCrimson)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إلغاء")
                        }
                    }

                    Button(
                        onClick = {
                            if (newPin.length != 4) {
                                errorMessage = "يرجى إدخال 4 أرقام بالضبط لتفعيل رمز القفل"
                                return@Button
                            }
                            onSavePin(newPin)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("save_pin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive)
                    ) {
                        Text("حفظ رمز PIN", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockOverlay(
    officeName: String,
    onUnlockAttempt: (String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    fun handleDigit(digit: String) {
        if (enteredPin.length < 4) {
            val next = enteredPin + digit
            enteredPin = next
            hasError = false
            if (next.length == 4) {
                val success = onUnlockAttempt(next)
                if (!success) {
                    hasError = true
                    enteredPin = ""
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
            .testTag("pin_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = officeName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "أدخل رمز المرور المكون من 4 أرقام لفتح سجل الديون",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasError -> Color(0xFFEF4444)
                                        filled -> Color(0xFF10B981)
                                        else -> Color(0xFF334155)
                                    }
                                )
                        )
                    }
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "رمز المرور غير صحيح، يرجى المحاولة مرة أخرى",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { key ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            when (key) {
                                                "C" -> {
                                                    enteredPin = ""
                                                    hasError = false
                                                }
                                                "DEL" -> {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                }
                                                else -> handleDigit(key)
                                            }
                                        }
                                        .testTag("pin_key_$key"),
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "DEL") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "مسح",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = if (key == "C") "مسح" else key,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = if (key == "C") Color(0xFFF87171) else Color.White,
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
        }
    }
}
