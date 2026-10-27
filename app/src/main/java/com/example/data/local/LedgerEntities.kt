package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object CurrencyCode {
    const val IQD = "IQD"
    const val USD = "USD"
}

object TransactionType {
    const val DEBT = "DEBT"
    const val PAYMENT = "PAYMENT"
}

@Entity(tableName = "debtors")
data class DebtorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val creditLimitIqd: Double = 0.0,
    val creditLimitUsd: Double = 0.0,
    val dueDateMillis: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = DebtorEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["debtorId"]), Index(value = ["timestamp"]), Index(value = ["currency"])]
)
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtorId: Long,
    val type: String, // "DEBT" or "PAYMENT"
    val currency: String = CurrencyCode.IQD, // "IQD" or "USD"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class DebtorSummary(
    val debtor: DebtorEntity,
    val totalDebtIqd: Double,
    val totalPaidIqd: Double,
    val currentBalanceIqd: Double,
    val totalDebtUsd: Double,
    val totalPaidUsd: Double,
    val currentBalanceUsd: Double,
    val lastTransactionDate: Long?,
    val transactionCount: Int,
    val isOverdue: Boolean,
    val isOverLimit: Boolean,
    val isSettled: Boolean
) {
    val hasActiveDebt: Boolean
        get() = currentBalanceIqd > 0.009 || currentBalanceUsd > 0.009
}

data class StatementEntry(
    val transaction: LedgerTransactionEntity,
    val runningBalanceIqd: Double,
    val runningBalanceUsd: Double
)

data class ClearanceEvent(
    val debtor: DebtorEntity,
    val totalPaidIqd: Double,
    val totalPaidUsd: Double,
    val lastPaymentAmount: Double,
    val lastPaymentCurrency: String,
    val clearanceTimestamp: Long,
    val referenceNumber: String
)
