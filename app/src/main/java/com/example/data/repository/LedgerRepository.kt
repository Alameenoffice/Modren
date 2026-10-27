package com.example.data.repository

import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerDao
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LedgerRepository(private val dao: LedgerDao) {

    val allDebtors: Flow<List<DebtorEntity>> = dao.observeAllDebtors()
    val allTransactions: Flow<List<LedgerTransactionEntity>> = dao.observeAllTransactions()

    suspend fun getDebtorById(id: Long): DebtorEntity? = dao.getDebtorById(id)

    suspend fun getTransactionsForDebtorOnce(debtorId: Long): List<LedgerTransactionEntity> =
        dao.getTransactionsForDebtorOnce(debtorId)

    suspend fun getAllDebtorsOnce(): List<DebtorEntity> = dao.getAllDebtorsOnce()

    suspend fun getAllTransactionsOnce(): List<LedgerTransactionEntity> = dao.getAllTransactionsOnce()

    suspend fun insertDebtor(debtor: DebtorEntity): Long = dao.insertDebtor(debtor)

    suspend fun updateDebtor(debtor: DebtorEntity) = dao.updateDebtor(debtor)

    suspend fun deleteDebtor(id: Long) = dao.deleteDebtorById(id)

    suspend fun insertTransaction(transaction: LedgerTransactionEntity): Long =
        dao.insertTransaction(transaction)

    suspend fun deleteTransaction(id: Long) = dao.deleteTransactionById(id)

    suspend fun replaceEntireDatabase(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ) {
        dao.replaceEntireDatabase(debtors, transactions)
    }

    suspend fun ensureSeedDataIfNeeded() {
        if (dao.getDebtorCount() > 0) return

        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val d1Id = dao.insertDebtor(
            DebtorEntity(
                name = "شركة الرافدين للتجارة العامة والتوريدات",
                phone = "07701234567",
                address = "بغداد - الكرادة - شارع الصناعة",
                creditLimitIqd = 15_000_000.0,
                creditLimitUsd = 10_000.0,
                dueDateMillis = now + (12 * dayMs),
                notes = "عميل جملة رئيسي - يتعامل بالدينار العراقي والدولار الأمريكي",
                createdAt = now - (90 * dayMs)
            )
        )

        val d2Id = dao.insertDebtor(
            DebtorEntity(
                name = "حيدر عبد الكريم الكاظمي",
                phone = "07809876543",
                address = "البصرة - العشار - السوق التجاري",
                creditLimitIqd = 5_000_000.0,
                creditLimitUsd = 3_000.0,
                dueDateMillis = now - (4 * dayMs), // Overdue
                notes = "مستحقات تجهيز مواد كهربائية",
                createdAt = now - (65 * dayMs)
            )
        )

        val d3Id = dao.insertDebtor(
            DebtorEntity(
                name = "مؤسسة دجلة للمقاولات الحديثة",
                phone = "07903218765",
                address = "أربيل - شارع 60 متر",
                creditLimitIqd = 25_000_000.0,
                creditLimitUsd = 20_000.0,
                dueDateMillis = now + (20 * dayMs),
                notes = "عقود تجهيز بالدولار والدينار",
                createdAt = now - (120 * dayMs)
            )
        )

        val d4Id = dao.insertDebtor(
            DebtorEntity(
                name = "مصطفى محمود الدليمي",
                phone = "07711223344",
                address = "الموصل - المجموعة الثقافية",
                creditLimitIqd = 3_000_000.0,
                creditLimitUsd = 2_000.0,
                dueDateMillis = now + (5 * dayMs),
                notes = "متبقي دفعة أخيرة بسيطة لإغلاق الحساب وتصفير الذمة",
                createdAt = now - (45 * dayMs)
            )
        )

        val d5Id = dao.insertDebtor(
            DebtorEntity(
                name = "معرض النخيل للأجهزة الإلكترونية",
                phone = "07818877665",
                address = "النجف الأشرف - حي الأمير",
                creditLimitIqd = 8_000_000.0,
                creditLimitUsd = 5_000.0,
                dueDateMillis = now + (30 * dayMs),
                notes = "تم تصفية الحساب بالكامل بالعملتين",
                createdAt = now - (80 * dayMs)
            )
        )

        fun daysAgo(days: Int, hourOffset: Int = 10): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = now - (days * dayMs)
            cal.set(Calendar.HOUR_OF_DAY, hourOffset)
            cal.set(Calendar.MINUTE, (days * 7) % 60)
            return cal.timeInMillis
        }

        val sampleTransactions = listOf(
            // Debtor 1: شركة الرافدين (IQD & USD)
            LedgerTransactionEntity(
                debtorId = d1Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 8_500_000.0,
                timestamp = daysAgo(60, 9),
                note = "قائمة تجهيز بضاعة محلية #INV-1042"
            ),
            LedgerTransactionEntity(
                debtorId = d1Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.IQD,
                amount = 3_500_000.0,
                timestamp = daysAgo(35, 13),
                note = "تسديد نقدي بالدينار العراقي - وصل قبض #210"
            ),
            LedgerTransactionEntity(
                debtorId = d1Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.USD,
                amount = 4_200.0,
                timestamp = daysAgo(18, 11),
                note = "تجهيز بضاعة مستوردة بالدولار #USD-88"
            ),
            LedgerTransactionEntity(
                debtorId = d1Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.USD,
                amount = 1_700.0,
                timestamp = daysAgo(0, 10),
                note = "دفعة تسديد بالدولار الأمريكي نقداً"
            ),

            // Debtor 2: حيدر الكاظمي (Overdue IQD & USD)
            LedgerTransactionEntity(
                debtorId = d2Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 3_250_000.0,
                timestamp = daysAgo(45, 14),
                note = "ذمة مالية عن تجهيز مواد كهربائية"
            ),
            LedgerTransactionEntity(
                debtorId = d2Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.IQD,
                amount = 1_000_000.0,
                timestamp = daysAgo(20, 16),
                note = "تسديد جزئي بالدينار العراقي"
            ),
            LedgerTransactionEntity(
                debtorId = d2Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.USD,
                amount = 950.0,
                timestamp = daysAgo(0, 9),
                note = "طلبية كابلات مستوردة مضافة اليوم"
            ),

            // Debtor 3: مؤسسة دجلة للمقاولات
            LedgerTransactionEntity(
                debtorId = d3Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.USD,
                amount = 12_500.0,
                timestamp = daysAgo(90, 10),
                note = "عقد تجهيز معدات المرحلة الأولى ($)"
            ),
            LedgerTransactionEntity(
                debtorId = d3Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.USD,
                amount = 7_500.0,
                timestamp = daysAgo(50, 12),
                note = "دفعة أولى بالدولار الأمريكي"
            ),
            LedgerTransactionEntity(
                debtorId = d3Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 6_000_000.0,
                timestamp = daysAgo(15, 11),
                note = "مصاريف نقل وتوريد محلي بالدينار"
            ),
            LedgerTransactionEntity(
                debtorId = d3Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.IQD,
                amount = 4_000_000.0,
                timestamp = daysAgo(0, 13),
                note = "دفعة تسديد اليوم بالدينار العراقي"
            ),

            // Debtor 4: مصطفى محمود الدليمي (Only 250,000 IQD remaining - 1-click full settlement test)
            LedgerTransactionEntity(
                debtorId = d4Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 1_750_000.0,
                timestamp = daysAgo(25, 11),
                note = "قائمة مبيعات آجلة #INV-1089"
            ),
            LedgerTransactionEntity(
                debtorId = d4Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.IQD,
                amount = 1_500_000.0,
                timestamp = daysAgo(3, 17),
                note = "دفعة سداد أولى بالدينار العراقي"
            ),

            // Debtor 5: معرض النخيل (Fully settled = 0 IQD and 0 USD)
            LedgerTransactionEntity(
                debtorId = d5Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.IQD,
                amount = 4_500_000.0,
                timestamp = daysAgo(70, 9),
                note = "قائمة أجهزة منزلية بالدينار"
            ),
            LedgerTransactionEntity(
                debtorId = d5Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.IQD,
                amount = 4_500_000.0,
                timestamp = daysAgo(30, 14),
                note = "تسديد كامل رصيد الدينار العراقي"
            ),
            LedgerTransactionEntity(
                debtorId = d5Id,
                type = TransactionType.DEBT,
                currency = CurrencyCode.USD,
                amount = 2_400.0,
                timestamp = daysAgo(60, 11),
                note = "قائمة شاشات مستوردة بالدولار"
            ),
            LedgerTransactionEntity(
                debtorId = d5Id,
                type = TransactionType.PAYMENT,
                currency = CurrencyCode.USD,
                amount = 2_400.0,
                timestamp = daysAgo(10, 18),
                note = "تسديد كامل رصيد الدولار وإغلاق الذمة"
            )
        )

        dao.insertTransactions(sampleTransactions)
    }
}
