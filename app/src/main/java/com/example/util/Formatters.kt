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
