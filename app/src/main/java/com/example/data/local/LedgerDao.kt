package com.example.data.local

import kotlinx.coroutines.flow.Flow

interface LedgerDao {
    fun observeAllDebtors(): Flow<List<DebtorEntity>>
    suspend fun getAllDebtorsOnce(): List<DebtorEntity>
    suspend fun getDebtorById(id: Long): DebtorEntity?
    suspend fun getDebtorCount(): Int
    suspend fun insertDebtor(debtor: DebtorEntity): Long
    suspend fun insertDebtors(debtors: List<DebtorEntity>)
    suspend fun updateDebtor(debtor: DebtorEntity)
    suspend fun deleteDebtor(debtor: DebtorEntity)
    suspend fun deleteDebtorById(id: Long)

    fun observeAllTransactions(): Flow<List<LedgerTransactionEntity>>
    suspend fun getAllTransactionsOnce(): List<LedgerTransactionEntity>
    fun observeTransactionsForDebtor(debtorId: Long): Flow<List<LedgerTransactionEntity>>
    suspend fun getTransactionsForDebtorOnce(debtorId: Long): List<LedgerTransactionEntity>
    suspend fun insertTransaction(transaction: LedgerTransactionEntity): Long
    suspend fun insertTransactions(transactions: List<LedgerTransactionEntity>)
    suspend fun deleteTransaction(transaction: LedgerTransactionEntity)
    suspend fun deleteTransactionById(id: Long)
    suspend fun clearAllTransactions()
    suspend fun clearAllDebtors()
    suspend fun replaceEntireDatabase(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    )
}
