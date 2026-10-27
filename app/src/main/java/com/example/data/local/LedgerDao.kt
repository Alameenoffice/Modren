package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    @Query("SELECT * FROM debtors ORDER BY createdAt DESC")
    fun observeAllDebtors(): Flow<List<DebtorEntity>>

    @Query("SELECT * FROM debtors ORDER BY id ASC")
    suspend fun getAllDebtorsOnce(): List<DebtorEntity>

    @Query("SELECT * FROM debtors WHERE id = :id LIMIT 1")
    suspend fun getDebtorById(id: Long): DebtorEntity?

    @Query("SELECT COUNT(*) FROM debtors")
    suspend fun getDebtorCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtor(debtor: DebtorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtors(debtors: List<DebtorEntity>)

    @Update
    suspend fun updateDebtor(debtor: DebtorEntity)

    @Delete
    suspend fun deleteDebtor(debtor: DebtorEntity)

    @Query("DELETE FROM debtors WHERE id = :id")
    suspend fun deleteDebtorById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun observeAllTransactions(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp ASC, id ASC")
    suspend fun getAllTransactionsOnce(): List<LedgerTransactionEntity>

    @Query("SELECT * FROM transactions WHERE debtorId = :debtorId ORDER BY timestamp ASC, id ASC")
    fun observeTransactionsForDebtor(debtorId: Long): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM transactions WHERE debtorId = :debtorId ORDER BY timestamp ASC, id ASC")
    suspend fun getTransactionsForDebtorOnce(debtorId: Long): List<LedgerTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<LedgerTransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: LedgerTransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM debtors")
    suspend fun clearAllDebtors()

    @Transaction
    suspend fun replaceEntireDatabase(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ) {
        clearAllTransactions()
        clearAllDebtors()
        insertDebtors(debtors)
        insertTransactions(transactions)
    }
}
