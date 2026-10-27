package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.local.ClearanceEvent
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.DebtorSummary
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.PaymentReceiptEvent
import com.example.data.local.StatementEntry
import com.example.data.local.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExportAndBackupManager {

    private const val UTF8_BOM = "\uFEFF"
    private const val DEFAULT_OFFICE = "مكتب المحاسبة والتجارة العامة"
    private const val SNAPSHOT_FILENAME = "ledger_local_backup_snapshot.json"

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    fun buildDebtorsCsv(
        summaries: List<DebtorSummary>,
        officeName: String = DEFAULT_OFFICE
    ): String {
        val sb = StringBuilder(UTF8_BOM)
        sb.appendLine("اسم المكتب / الشركة,${escapeCsv(officeName)},تاريخ التصدير,${escapeCsv(Formatters.formatDateTime(System.currentTimeMillis()))}")
        sb.appendLine("معرف العميل,اسم المدين,رقم الهاتف,العنوان,إجمالي الدين (IQD),إجمالي المسدد (IQD),الرصيد المتبقي (IQD),إجمالي الدين (USD),إجمالي المسدد (USD),الرصيد المتبقي (USD),تاريخ الاستحقاق,الحالة")
        summaries.forEach { s ->
            val status = when {
                s.isSettled -> "مسدد بالكامل"
                s.isOverdue -> "متأخر عن السداد"
                s.isOverLimit -> "تجاوز سقف الدين"
                else -> "نشط"
            }
            sb.appendLine(
                listOf(
                    s.debtor.id.toString(),
                    escapeCsv(s.debtor.name),
                    escapeCsv(s.debtor.phone),
                    escapeCsv(s.debtor.address),
                    Formatters.formatNumber(s.totalDebtIqd),
                    Formatters.formatNumber(s.totalPaidIqd),
                    Formatters.formatNumber(s.currentBalanceIqd),
                    Formatters.formatNumber(s.totalDebtUsd),
                    Formatters.formatNumber(s.totalPaidUsd),
                    Formatters.formatNumber(s.currentBalanceUsd),
                    escapeCsv(Formatters.formatDate(s.debtor.dueDateMillis)),
                    escapeCsv(status)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun buildStatementCsv(
        debtor: DebtorEntity,
        entries: List<StatementEntry>,
        officeName: String = DEFAULT_OFFICE
    ): String {
        val sb = StringBuilder(UTF8_BOM)
        sb.appendLine("اسم المكتب / الشركة,${escapeCsv(officeName)},كشف حساب العميل,${escapeCsv(debtor.name)},رقم الهاتف,${escapeCsv(debtor.phone)}")
        sb.appendLine("رقم العملية,التاريخ والوقت,نوع العملية,العملة (IQD/USD),المبلغ,الرصيد التراكمي (IQD د.ع),الرصيد التراكمي (USD $),البيان والملاحظات")
        entries.forEach { entry ->
            val tx = entry.transaction
            val typeAr = if (tx.type == TransactionType.DEBT) "إضافة دين" else "دفعة تسديد"
            sb.appendLine(
                listOf(
                    tx.id.toString(),
                    escapeCsv(Formatters.formatDateTime(tx.timestamp)),
                    escapeCsv(typeAr),
                    escapeCsv(tx.currency),
                    Formatters.formatNumber(tx.amount),
                    Formatters.formatNumber(entry.runningBalanceIqd),
                    Formatters.formatNumber(entry.runningBalanceUsd),
                    escapeCsv(tx.note)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun buildReportCsv(
        reportTitle: String,
        transactions: List<LedgerTransactionEntity>,
        debtorMap: Map<Long, DebtorEntity>,
        officeName: String = DEFAULT_OFFICE
    ): String {
        val sb = StringBuilder(UTF8_BOM)
        val totalDebtIqd = transactions.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
        val totalPaidIqd = transactions.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
        val totalDebtUsd = transactions.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
        val totalPaidUsd = transactions.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }

        sb.appendLine("${escapeCsv(reportTitle)},${escapeCsv(officeName)}")
        sb.appendLine("إجمالي ديون الدينار (IQD),${Formatters.formatNumber(totalDebtIqd)},إجمالي تحصيل الدينار (IQD),${Formatters.formatNumber(totalPaidIqd)}")
        sb.appendLine("إجمالي ديون الدولار (USD),${Formatters.formatNumber(totalDebtUsd)},إجمالي تحصيل الدولار (USD),${Formatters.formatNumber(totalPaidUsd)}")
        sb.appendLine("رقم العملية,التاريخ والوقت,اسم العميل,نوع العملية,العملة (IQD/USD),المبلغ,البيان والملاحظات")
        transactions.forEach { tx ->
            val debtorName = debtorMap[tx.debtorId]?.name ?: "عميل #${tx.debtorId}"
            val typeAr = if (tx.type == TransactionType.DEBT) "دين مستحق" else "تسديد دفعة"
            sb.appendLine(
                listOf(
                    tx.id.toString(),
                    escapeCsv(Formatters.formatDateTime(tx.timestamp)),
                    escapeCsv(debtorName),
                    escapeCsv(typeAr),
                    escapeCsv(tx.currency),
                    Formatters.formatNumber(tx.amount),
                    escapeCsv(tx.note)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    // JSON Backup & Restore (Dual Currency)
    fun exportDatabaseToJson(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>,
        officeName: String = DEFAULT_OFFICE
    ): String {
        val root = JSONObject()
        root.put("system", "سجل الديون - النظام المحاسبي المتكامل (IQD & USD)")
        root.put("officeName", officeName)
        root.put("version", 3)
        root.put("exportedAt", System.currentTimeMillis())

        val debtorsArray = JSONArray()
        debtors.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("phone", d.phone)
            obj.put("address", d.address)
            obj.put("creditLimitIqd", d.creditLimitIqd)
            obj.put("creditLimitUsd", d.creditLimitUsd)
            if (d.dueDateMillis != null) {
                obj.put("dueDateMillis", d.dueDateMillis)
            }
            obj.put("notes", d.notes)
            obj.put("createdAt", d.createdAt)
            debtorsArray.put(obj)
        }
        root.put("debtors", debtorsArray)

        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("debtorId", tx.debtorId)
            obj.put("type", tx.type)
            obj.put("currency", tx.currency)
            obj.put("amount", tx.amount)
            obj.put("timestamp", tx.timestamp)
            obj.put("note", tx.note)
            txArray.put(obj)
        }
        root.put("transactions", txArray)
        return root.toString(2)
    }

    fun parseDatabaseFromJson(jsonString: String): Pair<List<DebtorEntity>, List<LedgerTransactionEntity>> {
        val root = JSONObject(jsonString)
        val debtorsArray = root.getJSONArray("debtors")
        val transactionsArray = root.getJSONArray("transactions")

        val debtors = mutableListOf<DebtorEntity>()
        for (i in 0 until debtorsArray.length()) {
            val obj = debtorsArray.getJSONObject(i)
            val due = if (obj.has("dueDateMillis") && !obj.isNull("dueDateMillis")) {
                obj.getLong("dueDateMillis")
            } else {
                null
            }
            val limitIqd = if (obj.has("creditLimitIqd")) {
                obj.optDouble("creditLimitIqd", 0.0)
            } else {
                obj.optDouble("creditLimit", 0.0)
            }
            val limitUsd = obj.optDouble("creditLimitUsd", 0.0)
            debtors.add(
                DebtorEntity(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    creditLimitIqd = limitIqd,
                    creditLimitUsd = limitUsd,
                    dueDateMillis = due,
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val transactions = mutableListOf<LedgerTransactionEntity>()
        for (i in 0 until transactionsArray.length()) {
            val obj = transactionsArray.getJSONObject(i)
            transactions.add(
                LedgerTransactionEntity(
                    id = obj.getLong("id"),
                    debtorId = obj.getLong("debtorId"),
                    type = obj.getString("type"),
                    currency = obj.optString("currency", CurrencyCode.IQD),
                    amount = obj.getDouble("amount"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    note = obj.optString("note", "")
                )
            )
        }
        return debtors to transactions
    }

    fun saveInternalBackupSnapshot(context: Context, jsonContent: String): File {
        val file = File(context.filesDir, SNAPSHOT_FILENAME)
        file.writeText(jsonContent, Charsets.UTF_8)
        return file
    }

    fun getInternalBackupSnapshotInfo(context: Context): Pair<Boolean, Long> {
        val file = File(context.filesDir, SNAPSHOT_FILENAME)
        return if (file.exists() && file.length() > 0) {
            true to file.lastModified()
        } else {
            false to 0L
        }
    }

    fun readInternalBackupSnapshot(context: Context): String? {
        val file = File(context.filesDir, SNAPSHOT_FILENAME)
        return if (file.exists()) file.readText(Charsets.UTF_8) else null
    }

    fun deleteInternalBackupSnapshot(context: Context) {
        val file = File(context.filesDir, SNAPSHOT_FILENAME)
        if (file.exists()) file.delete()
    }

    fun saveAndShareFile(
        context: Context,
        fileName: String,
        mimeType: String,
        contentBytes: ByteArray,
        shareTitle: String
    ) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val targetFile = File(exportDir, fileName)
        FileOutputStream(targetFile).use { it.write(contentBytes) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, shareTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // Smart WhatsApp Messaging (Direct to Debtor Phone)
    fun openWhatsAppDirectOrShare(
        context: Context,
        phone: String,
        message: String,
        shareTitle: String
    ) {
        val normalizedPhone = Formatters.normalizePhoneForWhatsApp(phone)
        val encodedText = Uri.encode(message)
        val waUrl = if (normalizedPhone.isNotBlank()) {
            "https://wa.me/$normalizedPhone?text=$encodedText"
        } else {
            "https://wa.me/?text=$encodedText"
        }

        try {
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(sendIntent, shareTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun buildDebtReminderWhatsAppMessage(
        officeName: String,
        summary: DebtorSummary
    ): String {
        val header = officeName.ifBlank { DEFAULT_OFFICE }
        val dueLine = if (summary.debtor.dueDateMillis != null) {
            val statusStr = if (summary.isOverdue) " (متأخر عن السداد منذ ${summary.overdueDays} يوم)" else ""
            "\n• تاريخ الاستحقاق المجدول: *${Formatters.formatDate(summary.debtor.dueDateMillis)}*$statusStr"
        } else {
            ""
        }

        return """
            *$header*
            *إشعار تذكير ومطالبة مالية*
            ────────────────────
            تحية طيبة للسيد / السادة: *${summary.debtor.name}* المحترم،
            
            نود تذكيركم بالرصيد المالي المتبقي في ذمتكم لدى *$header*:
            • الرصيد المتبقي بالدينار العراقي: *${Formatters.formatIqd(summary.currentBalanceIqd)}*
            • الرصيد المتبقي بالدولار الأمريكي: *${Formatters.formatUsd(summary.currentBalanceUsd)}*$dueLine
            
            يرجى التفضل بمراجعة الحساب أو تسديد المستحقات في أقرب وقت، شاكرين حسن تعاونكم والتزامكم.
            ────────────────────
            التاريخ: ${Formatters.formatDateTime(System.currentTimeMillis())}
            صادر عن: *$header*
        """.trimIndent()
    }

    fun sendDebtReminderViaWhatsApp(
        context: Context,
        officeName: String,
        summary: DebtorSummary
    ) {
        val msg = buildDebtReminderWhatsAppMessage(officeName, summary)
        openWhatsAppDirectOrShare(
            context = context,
            phone = summary.debtor.phone,
            message = msg,
            shareTitle = "إرسال تذكير بالدين عبر واتساب - ${summary.debtor.name}"
        )
    }

    fun buildPaymentReceiptWhatsAppMessage(
        officeName: String,
        receipt: PaymentReceiptEvent
    ): String {
        val header = officeName.ifBlank { DEFAULT_OFFICE }
        val amountStr = Formatters.formatByCurrency(receipt.paymentAmount, receipt.currency)
        return """
            *$header*
            *وصل قبض دفعة مالية رسمي*
            ────────────────────
            رقم الوصل: ${receipt.receiptNumber}
            التاريخ: ${Formatters.formatDateTime(receipt.timestamp)}
            
            استلمنا من السيد / السادة: *${receipt.debtor.name}*
            مبلغاً وقدره: *$amountStr* (${Formatters.currencyFullName(receipt.currency)})
            البيان: ${receipt.note.ifBlank { "دفعة تسديد من الحساب" }}
            
            *الرصيد المتبقي بعد هذه الدفعة:*
            • بالدينار العراقي (IQD): *${Formatters.formatIqd(receipt.remainingBalanceIqd)}*
            • بالدولار الأمريكي (USD): *${Formatters.formatUsd(receipt.remainingBalanceUsd)}*
            ────────────────────
            شكراً لالتزامكم بالسداد • صادر عن: *$header*
        """.trimIndent()
    }

    fun sendPaymentReceiptViaWhatsApp(
        context: Context,
        officeName: String,
        receipt: PaymentReceiptEvent
    ) {
        val msg = buildPaymentReceiptWhatsAppMessage(officeName, receipt)
        openWhatsAppDirectOrShare(
            context = context,
            phone = receipt.debtor.phone,
            message = msg,
            shareTitle = "إرسال وصل قبض عبر واتساب - ${receipt.debtor.name}"
        )
    }

    fun buildClearanceWhatsAppMessage(
        officeName: String,
        event: ClearanceEvent
    ): String {
        val header = officeName.ifBlank { DEFAULT_OFFICE }
        val lastPaymentFormatted = Formatters.formatByCurrency(
            event.lastPaymentAmount,
            event.lastPaymentCurrency
        )
        return """
            *$header*
            *سند براءة ذمة ومخالصة مالية نهائية*
            ────────────────────
            رقم المرجع: ${event.referenceNumber}
            التاريخ: ${Formatters.formatDateTime(event.clearanceTimestamp)}
            
            نشهد في *$header* بأن السيد / السادة: *${event.debtor.name}*
            رقم الهاتف: ${event.debtor.phone.ifBlank { "غير مسجل" }}
            
            قد قام بتسديد كافة المستحقات والديون المسجلة عليه بالكامل، وآخرها دفعة بقيمة *$lastPaymentFormatted*.
            • إجمالي المسدد بالدينار العراقي: *${Formatters.formatIqd(event.totalPaidIqd)}*
            • إجمالي المسدد بالدولار الأمريكي: *${Formatters.formatUsd(event.totalPaidUsd)}*
            
            *الرصيد المتبقي الحالي: 0 د.ع | 0 $ (ذمة مالية خالية تماماً)*
            ────────────────────
            صادر عن: *$header*
        """.trimIndent()
    }

    fun shareClearanceCertificate(
        context: Context,
        officeName: String,
        event: ClearanceEvent
    ) {
        val message = buildClearanceWhatsAppMessage(officeName, event)
        if (event.debtor.phone.isNotBlank()) {
            openWhatsAppDirectOrShare(
                context = context,
                phone = event.debtor.phone,
                message = message,
                shareTitle = "سند براءة ذمة - ${event.debtor.name}"
            )
            return
        }
        val pdfFile = generateClearancePdfFile(context, officeName, event)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "سند براءة ذمة - ${event.debtor.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "مشاركة سند براءة الذمة عبر واتساب أو التطبيقات")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun generateClearancePdfFile(
        context: Context,
        officeName: String,
        event: ClearanceEvent
    ): File {
        val headerOffice = officeName.ifBlank { DEFAULT_OFFICE }
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val navyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
        val emeraldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#10B981") }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6EE7B7")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 14f
            textAlign = Paint.Align.RIGHT
        }

        // Top Navy Header
        canvas.drawRect(0f, 0f, 595f, 125f, navyPaint)
        canvas.drawText(headerOffice, 297.5f, 52f, titlePaint)
        canvas.drawText("سند براءة ذمة ومخالصة مالية نهائية", 297.5f, 88f, subHeaderPaint)

        // Outer Frame
        canvas.drawRoundRect(RectF(36f, 150f, 559f, 760f), 16f, 16f, borderPaint)

        // Emerald Status Pill
        val pillRect = RectF(150f, 175f, 445f, 215f)
        canvas.drawRoundRect(pillRect, 20f, 20f, emeraldPaint)
        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تم إغلاق الذمة المالية وتصفير الحساب (0 د.ع | 0 $)", 297.5f, 200f, pillTextPaint)

        var y = 265f
        val rightX = 525f
        canvas.drawText("الجهة المُصدِرة: $headerOffice", rightX, y, bodyBoldPaint)
        y += 30f
        canvas.drawText("رقم السند المرجعي: ${event.referenceNumber}", rightX, y, bodyBoldPaint)
        y += 30f
        canvas.drawText("تاريخ إصدار المخالصة: ${Formatters.formatDateTime(event.clearanceTimestamp)}", rightX, y, bodyPaint)
        y += 40f
        canvas.drawText("اسم العميل / المدين: ${event.debtor.name}", rightX, y, bodyBoldPaint)
        y += 28f
        canvas.drawText("رقم الهاتف: ${event.debtor.phone.ifBlank { "غير مسجل" }}", rightX, y, bodyPaint)
        y += 28f
        canvas.drawText("العنوان: ${event.debtor.address.ifBlank { "غير مسجل" }}", rightX, y, bodyPaint)

        y += 42f
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRoundRect(RectF(60f, y - 28f, 535f, y + 135f), 12f, 12f, boxPaint)
        canvas.drawRoundRect(RectF(60f, y - 28f, 535f, y + 135f), 12f, 12f, borderPaint)

        canvas.drawText("إجمالي المسدد بالدينار العراقي: ${Formatters.formatIqd(event.totalPaidIqd)}", 510f, y + 5f, bodyBoldPaint)
        canvas.drawText("إجمالي المسدد بالدولار الأمريكي: ${Formatters.formatUsd(event.totalPaidUsd)}", 510f, y + 36f, bodyBoldPaint)
        canvas.drawText(
            "قيمة الدفعة الأخيرة المسددة: ${Formatters.formatByCurrency(event.lastPaymentAmount, event.lastPaymentCurrency)}",
            510f,
            y + 68f,
            bodyPaint
        )
        val zeroPaint = Paint(bodyBoldPaint).apply { color = Color.parseColor("#059669") }
        canvas.drawText("الرصيد المتبقي بالعملتين حالياً: 0 د.ع | 0.00 $ (مسدد بالكامل)", 510f, y + 104f, zeroPaint)

        y += 180f
        canvas.drawText("يُعد هذا السند إقراراً رسمياً ببراءة ذمة العميل المذكور أعلاه من كافة الديون المسجلة", rightX, y, bodyPaint)
        y += 26f
        canvas.drawText("سواء بالدينار العراقي أو الدولار الأمريكي حتى تاريخه، ولا توجد عليه أي مطالبات متبقية.", rightX, y, bodyPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("صادر عن: $headerOffice • نظام سجل الديون والمحاسبة المتكامل", 297.5f, 805f, footerPaint)

        document.finishPage(page)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "clearance_${event.debtor.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun generateStatementOrReportPdfFile(
        context: Context,
        title: String,
        subtitle: String,
        summaryLines: List<String>,
        tableHeaders: List<String>,
        tableRows: List<List<String>>,
        fileNamePrefix: String,
        officeName: String = DEFAULT_OFFICE
    ): File {
        val headerOffice = officeName.ifBlank { DEFAULT_OFFICE }
        val document = PdfDocument()
        val rowsPerPage = 18
        val chunks = if (tableRows.isEmpty()) listOf(emptyList()) else tableRows.chunked(rowsPerPage)

        val navyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
        val headerRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
        val zebraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#93C5FD")
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val officeBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6EE7B7")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val cellBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        chunks.forEachIndexed { pageIndex, pageRows ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawRect(0f, 0f, 595f, 95f, navyPaint)
            canvas.drawText(title, 560f, 42f, titlePaint)
            canvas.drawText(subtitle, 560f, 68f, subPaint)
            canvas.drawText(headerOffice, 35f, 42f, officeBadgePaint)

            var y = 122f
            if (pageIndex == 0 && summaryLines.isNotEmpty()) {
                summaryLines.forEach { line ->
                    canvas.drawText(line, 560f, y, cellBoldPaint)
                    y += 19f
                }
                y += 8f
            }

            // Table Header
            canvas.drawRect(30f, y - 16f, 565f, y + 10f, headerRowPaint)
            val colCount = tableHeaders.size.coerceAtLeast(1)
            val colWidth = 535f / colCount
            tableHeaders.forEachIndexed { idx, header ->
                val x = 560f - (idx * colWidth)
                canvas.drawText(header, x, y, cellBoldPaint)
            }
            y += 28f

            pageRows.forEachIndexed { rowIdx, row ->
                if (rowIdx % 2 == 1) {
                    canvas.drawRect(30f, y - 15f, 565f, y + 9f, zebraPaint)
                }
                row.forEachIndexed { colIdx, text ->
                    val x = 560f - (colIdx * colWidth)
                    val trimmed = if (text.length > 24) text.take(22) + ".." else text
                    canvas.drawText(trimmed, x, y, cellPaint)
                }
                y += 24f
            }

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "صفحة ${pageIndex + 1} من ${chunks.size} • $headerOffice",
                297.5f,
                815f,
                footerPaint
            )
            document.finishPage(page)
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "${fileNamePrefix}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun printPdfFile(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}
