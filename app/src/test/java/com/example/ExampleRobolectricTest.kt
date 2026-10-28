package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.cloud.CloudUserSettings
import com.example.data.cloud.FirebaseCloudSyncManager
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `verify firebase project configuration and app strings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val defaultOfficeName = context.getString(R.string.default_office_name)
        assertEquals("سجل الديون", appName)
        assertEquals("مكتب المحاسبة والتجارة العامة", defaultOfficeName)
        assertEquals("mizan-debt", FirebaseCloudSyncManager.PROJECT_ID)
        assertEquals("AIzaSyANINqHh_7whX_qbQeuJYTfNduSxHIqStc", FirebaseCloudSyncManager.API_KEY)
    }

    @Test
    fun `dual currency formatting, per-user offline cache, and json backup round-trip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("250,000 د.ع", Formatters.formatIqd(250000.0))
        assertEquals("1,500 $", Formatters.formatUsd(1500.0))

        val officeName = "شركة الميزان للتجارة العامة"
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
        assertEquals(2, restoredTxs.size)

        // Verify per-UID offline persistence cache
        val testUid = "test_uid_mizan_123"
        FirebaseCloudSyncManager.saveUserOfflineSnapshot(
            context = context,
            uid = testUid,
            settings = CloudUserSettings(officeName = officeName, usdToIqdRate = 1510.0),
            debtors = debtors,
            transactions = transactions
        )
        val loadedBundle = FirebaseCloudSyncManager.loadUserOfflineSnapshot(context, testUid)
        assertNotNull(loadedBundle)
        assertEquals(officeName, loadedBundle?.settings?.officeName)
        assertEquals(1510.0, loadedBundle?.settings?.usdToIqdRate ?: 0.0, 0.01)
        assertEquals(1, loadedBundle?.debtors?.size)
        assertEquals(2, loadedBundle?.transactions?.size)
    }
}
