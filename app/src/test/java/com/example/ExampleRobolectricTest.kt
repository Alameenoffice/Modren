package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `read app name and default office name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val defaultOfficeName = context.getString(R.string.default_office_name)
        assertEquals("سجل الديون", appName)
        assertEquals("مكتب المحاسبة والتجارة العامة", defaultOfficeName)
    }

    @Test
    fun `dual currency formatting and json backup round-trip with office name`() {
        assertEquals("250,000 د.ع", Formatters.formatIqd(250000.0))
        assertEquals("1,500 $", Formatters.formatUsd(1500.0))

        val officeName = "شركة النور للتجارة العامة"
        val debtors = listOf(
            DebtorEntity(
                id = 10L,
                name = "شركة اختبارية",
                phone = "07700000000",
                address = "بغداد",
                creditLimitIqd = 5_000_000.0,
                creditLimitUsd = 3_000.0
            )
        )
        val transactions = listOf(
            LedgerTransactionEntity(
                id = 100L,
                debtorId = 10L,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 250000.0,
                note = "قائمة دينار"
            ),
            LedgerTransactionEntity(
                id = 101L,
                debtorId = 10L,
                type = TransactionType.DEBT,
                currency = CurrencyCode.USD,
                amount = 1500.0,
                note = "قائمة دولار"
            )
        )

        val json = ExportAndBackupManager.exportDatabaseToJson(
            debtors = debtors,
            transactions = transactions,
            officeName = officeName
        )
        assertTrue(json.contains(officeName))

        val (restoredDebtors, restoredTxs) = ExportAndBackupManager.parseDatabaseFromJson(json)
        assertEquals(1, restoredDebtors.size)
        assertEquals(5_000_000.0, restoredDebtors.first().creditLimitIqd, 0.01)
        assertEquals(3_000.0, restoredDebtors.first().creditLimitUsd, 0.01)
        assertEquals(2, restoredTxs.size)
        assertEquals(CurrencyCode.IQD, restoredTxs[0].currency)
        assertEquals(CurrencyCode.USD, restoredTxs[1].currency)
    }
}
