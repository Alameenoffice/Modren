package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.LedgerUiState
import com.example.ui.components.ResetAllDataConfirmationDialog
import com.example.ui.theme.LocalLedgerColors
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import kotlinx.coroutines.launch

@Composable
fun BackupAndExportScreen(
    uiState: LedgerUiState,
    officeName: String,
    usdToIqdRate: Double,
    userEmail: String,
    userUid: String,
    cloudSyncStatus: String,
    hasPinCode: Boolean,
    hasLocalSnapshot: Boolean,
    localSnapshotTimestamp: Long,
    onOpenOfficeSettings: () -> Unit,
    onOpenPinSettings: () -> Unit,
    onLockAppNow: () -> Unit,
    onSyncCloudNow: () -> Unit,
    onLogoutAccount: () -> Unit,
    onResetAllApplicationData: () -> Unit,
    onCreateLocalSnapshot: () -> Unit,
    onRestoreLocalSnapshot: () -> Unit,
    onRestoreFromJsonString: (String) -> Unit,
    onGenerateBackupJson: suspend () -> String
) {
    val colors = LocalLedgerColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showResetConfirmationModal by remember { mutableStateOf(false) }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = onGenerateBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val text = input.bufferedReader(Charsets.UTF_8).readText()
                onRestoreFromJsonString(text)
            }
        }
    }

    val saveDebtorsCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val csv = ExportAndBackupManager.buildDebtorsCsv(
                summaries = uiState.debtorSummaries,
                officeName = officeName
            )
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(csv.toByteArray(Charsets.UTF_8))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("backup_and_export_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Firebase Cloud Account & Sync Card (users/{uid})
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.paymentEmerald.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(colors.paymentEmeraldSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = colors.paymentEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الحساب السحابي والمزامنة (Firebase Firestore)",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "البريد المسجل: ${userEmail.ifBlank { "حساب محلي" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "المسار السحابي المعزول: users/${userUid.take(10)}.../debtors & transactions",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.elevatedSurface,
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = colors.paymentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$cloudSyncStatus • محفوظ: ${uiState.debtors.size} مدين و ${uiState.transactions.size} عملية",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncCloudNow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_cloud_now_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مزامنة سحابية الآن", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onLogoutAccount,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("logout_account_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.debtCrimson)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = colors.debtCrimson,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل الخروج", color = colors.debtCrimson, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Office Name, Exchange Rate & PIN Security Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.primaryInteractive.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = colors.primaryInteractive
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات المكتب، سعر الصرف، والحماية",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.elevatedSurface,
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "اسم المكتب أو الشركة: $officeName",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "سعر الصرف المعتمد للتصريف التلقائي: 1 دولار ($) = ${Formatters.formatNumber(usdToIqdRate)} د.ع",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenOfficeSettings,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_edit_office_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تعديل اسم المكتب وسعر الصرف", color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenPinSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_pin_lock_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (hasPinCode) "تغيير / إلغاء رمز PIN (مفعل)" else "تفعيل قفل التطبيق برمز PIN")
                        }

                        if (hasPinCode) {
                            Button(
                                onClick = onLockAppNow,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.sidebarSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("قفل الآن", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 3. JSON Full Database Backup & Restore Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = colors.primaryInteractive
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "النسخ الاحتياطي الشامل والاستعادة (JSON)",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "احفظ نسخة احتياطية كاملة لقاعدة البيانات بملف (JSON) في هاتفك، أو استعد بياناتك بضغطة زر في أي وقت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.elevatedSurface,
                        border = BorderStroke(1.dp, colors.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "النسخة الاحتياطية السريعة داخل التطبيق:",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (hasLocalSnapshot)
                                    "آخر نسخة محفوظة: ${Formatters.formatDateTime(localSnapshotTimestamp)}"
                                else
                                    "لم يتم أخذ نسخة سريعة بعد",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasLocalSnapshot) colors.paymentEmerald else colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onCreateLocalSnapshot,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("create_local_snapshot_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ احتياطي فوري", color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = onRestoreLocalSnapshot,
                                    enabled = hasLocalSnapshot,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("restore_local_snapshot_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("استعادة فورية")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                saveJsonLauncher.launch("debt_ledger_backup_${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_external_json_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ ملف JSON بالهاتف", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                openJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_external_json_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة من ملف JSON")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = onGenerateBackupJson()
                                ExportAndBackupManager.saveAndShareFile(
                                    context = context,
                                    fileName = "debt_ledger_backup.json",
                                    mimeType = "application/json",
                                    contentBytes = json.toByteArray(Charsets.UTF_8),
                                    shareTitle = "مشاركة ملف النسخة الاحتياطية JSON"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة ملف النسخة الاحتياطية (JSON) عبر التطبيقات")
                    }
                }
            }
        }

        // 4. Excel (CSV UTF-8-BOM) & PDF Master Exports Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.subtleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.paymentEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصدير التقارير وكشوفات الحسابات (Excel & PDF)",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يدعم التصدير بصيغة Excel (CSV بترميز UTF-8-BOM لدعم العربية) مع ترويسة ($officeName) وفصل واضح لنوع العملة (IQD / USD).",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val csv = ExportAndBackupManager.buildDebtorsCsv(
                                summaries = uiState.debtorSummaries,
                                officeName = officeName
                            )
                            ExportAndBackupManager.saveAndShareFile(
                                context = context,
                                fileName = "debtors_dual_currency_utf8bom.csv",
                                mimeType = "text/csv",
                                contentBytes = csv.toByteArray(Charsets.UTF_8),
                                shareTitle = "تصدير سجل المدينين والأرصدة (Excel CSV)"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_all_debtors_csv_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.paymentEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير ومشاركة سجل المدينين (Excel CSV - IQD & USD)", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            saveDebtorsCsvLauncher.launch("سجل_المدينين_${System.currentTimeMillis()}.csv")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ ملف Excel CSV مباشرة في ذاكرة الهاتف")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.subtleBorder)

                    Button(
                        onClick = {
                            val pdf = ExportAndBackupManager.generateStatementOrReportPdfFile(
                                context = context,
                                officeName = officeName,
                                title = "التقرير المالي الشامل لسجل الديون (IQD | USD)",
                                subtitle = "إجمالي المدينين: ${uiState.debtors.size} • المدينون النشطون: ${uiState.activeDebtorsCount}",
                                summaryLines = listOf(
                                    "المستحق بالدينار (IQD): ${Formatters.formatIqd(uiState.totalOutstandingDebtIqd)} | المحصل: ${Formatters.formatIqd(uiState.totalCollectedPaymentsIqd)}",
                                    "المستحق بالدولار (USD): ${Formatters.formatUsd(uiState.totalOutstandingDebtUsd)} | المحصل: ${Formatters.formatUsd(uiState.totalCollectedPaymentsUsd)}"
                                ),
                                tableHeaders = listOf("اسم المدين", "الهاتف", "المتبقي (د.ع IQD)", "المتبقي ($ USD)", "الحالة"),
                                tableRows = uiState.debtorSummaries.map { s ->
                                    listOf(
                                        s.debtor.name,
                                        s.debtor.phone,
                                        Formatters.formatIqd(s.currentBalanceIqd),
                                        Formatters.formatUsd(s.currentBalanceUsd),
                                        if (s.isSettled) "مسدد" else "نشط"
                                    )
                                },
                                fileNamePrefix = "master_financial_report"
                            )
                            ExportAndBackupManager.printPdfFile(
                                context = context,
                                pdfFile = pdf,
                                jobName = "التقرير_المالي_الشامل_سجل_الديون"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("print_master_pdf_report_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryInteractive),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("طباعة / حفظ التقرير المالي الشامل كملف PDF", color = Color.White)
                    }
                }
            }
        }

        // 5. Factory Reset / Wipe All Data Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
                border = BorderStroke(1.dp, colors.debtCrimson.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = colors.debtCrimson
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصفير ومسح جميع بيانات التطبيق",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.debtCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكّنك هذا الخيار من حذف جميع المدينين والعمليات المسجلة نهائياً والبدء بسجل محاسبي جديد وفارغ تماماً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showResetConfirmationModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_reset_all_data_modal_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.debtCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصفير ومسح جميع بيانات التطبيق", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetConfirmationModal) {
        ResetAllDataConfirmationDialog(
            debtorsCount = uiState.debtors.size,
            transactionsCount = uiState.transactions.size,
            onDismiss = { showResetConfirmationModal = false },
            onConfirmReset = {
                onResetAllApplicationData()
                showResetConfirmationModal = false
            }
        )
    }
}
