package com.example.util

import com.example.data.local.CurrencyCode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val englishSymbols = DecimalFormatSymbols(Locale.US)
    private val currencyNumberFormat = DecimalFormat("#,##0.##", englishSymbols)
    private val iqdNumberFormat = DecimalFormat("#,##0", englishSymbols)
    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale("ar"))
    private val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))

    val arabicMonths = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun formatIqd(amount: Double): String {
        return "${iqdNumberFormat.format(amount)} د.ع"
    }

    fun formatUsd(amount: Double): String {
        return "${currencyNumberFormat.format(amount)} $"
    }

    fun formatByCurrency(amount: Double, currency: String): String {
        return if (currency == CurrencyCode.USD) {
            formatUsd(amount)
        } else {
            formatIqd(amount)
        }
    }

    fun formatDualBalanceText(balanceIqd: Double, balanceUsd: Double): String {
        val parts = mutableListOf<String>()
        if (balanceIqd > 0.009) parts.add(formatIqd(balanceIqd))
        if (balanceUsd > 0.009) parts.add(formatUsd(balanceUsd))
        return if (parts.isEmpty()) "0 د.ع | 0 $" else parts.joinToString(" و ")
    }

    fun formatMoney(amount: Double, currencySymbol: String = "د.ع"): String {
        val formatted = currencyNumberFormat.format(amount)
        return "$formatted $currencySymbol"
    }

    fun currencyShortSymbol(currency: String): String {
        return if (currency == CurrencyCode.USD) "$" else "د.ع"
    }

    fun currencyFullName(currency: String): String {
        return if (currency == CurrencyCode.USD) "دولار أمريكي (USD)" else "دينار عراقي (IQD)"
    }

    fun formatNumber(amount: Double): String {
        return currencyNumberFormat.format(amount)
    }

    fun formatDate(millis: Long?): String {
        if (millis == null || millis <= 0L) return "غير محدد"
        return dateFormatter.format(Date(millis))
    }

    fun formatInputDate(millis: Long?): String {
        if (millis == null || millis <= 0L) return ""
        return inputDateFormatter.format(Date(millis))
    }

    fun parseInputDateToMillis(dateStr: String): Long? {
        val clean = dateStr.trim().replace("/", "-")
        if (clean.isBlank()) return null
        return try {
            inputDateFormatter.parse(clean)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun calculateDaysOverdue(dueDateMillis: Long?, nowMillis: Long = System.currentTimeMillis()): Int {
        if (dueDateMillis == null || dueDateMillis >= nowMillis) return 0
        val diff = nowMillis - dueDateMillis
        return ((diff / (24L * 60L * 60L * 1000L)).toInt()).coerceAtLeast(1)
    }

    fun normalizePhoneForWhatsApp(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return when {
            digits.startsWith("00") -> digits.substring(2)
            digits.startsWith("07") && digits.length == 11 -> "964" + digits.substring(1)
            digits.startsWith("7") && digits.length == 10 -> "964$digits"
            else -> digits
        }
    }

    fun formatTime(millis: Long): String {
        return timeFormatter.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        return dateTimeFormatter.format(Date(millis))
    }

    fun getMonthName(monthIndexZeroBased: Int): String {
        return arabicMonths.getOrElse(monthIndexZeroBased.coerceIn(0, 11)) { "" }
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameMonth(millis: Long, year: Int, monthZeroBased: Int): Boolean {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == monthZeroBased
    }

    fun isSameYear(millis: Long, year: Int): Boolean {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) == year
    }
}
