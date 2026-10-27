package com.example.data.repository

import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerDao
import com.example.data.local.LedgerTransactionEntity
import kotlinx.coroutines.flow.Flow

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

    suspend fun updateTransaction(transaction: LedgerTransactionEntity) =
        dao.updateTransaction(transaction)

    suspend fun deleteTransaction(id: Long) = dao.deleteTransactionById(id)

    suspend fun clearAllData() = dao.clearAllData()

    suspend fun replaceEntireDatabase(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ) {
        dao.replaceEntireDatabase(debtors, transactions)
    }
}
