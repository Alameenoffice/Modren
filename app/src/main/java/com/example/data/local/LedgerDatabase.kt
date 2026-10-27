package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LedgerDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "debt_ledger_native_v2.db", null, 2),
    LedgerDao {

    private val debtorsFlow = MutableStateFlow<List<DebtorEntity>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<LedgerTransactionEntity>>(emptyList())

    init {
        refreshFlowsSync()
    }

    fun ledgerDao(): LedgerDao = this

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS debtors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL,
                creditLimitIqd REAL NOT NULL DEFAULT 0.0,
                creditLimitUsd REAL NOT NULL DEFAULT 0.0,
                dueDateMillis INTEGER,
                notes TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                debtorId INTEGER NOT NULL,
                type TEXT NOT NULL,
                currency TEXT NOT NULL DEFAULT 'IQD',
                amount REAL NOT NULL,
                timestamp INTEGER NOT NULL,
                note TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(debtorId) REFERENCES debtors(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tx_debtor ON transactions(debtorId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tx_timestamp ON transactions(timestamp)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS debtors")
        onCreate(db)
    }

    private fun refreshFlowsSync() {
        debtorsFlow.value = queryDebtorsSync("ORDER BY createdAt DESC, id DESC")
        transactionsFlow.value = queryTransactionsSync("ORDER BY timestamp DESC, id DESC")
    }

    private fun queryDebtorsSync(orderClause: String): List<DebtorEntity> {
        val list = mutableListOf<DebtorEntity>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM debtors $orderClause", null)
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val nameIdx = c.getColumnIndexOrThrow("name")
            val phoneIdx = c.getColumnIndexOrThrow("phone")
            val addressIdx = c.getColumnIndexOrThrow("address")
            val limitIqdIdx = c.getColumnIndexOrThrow("creditLimitIqd")
            val limitUsdIdx = c.getColumnIndexOrThrow("creditLimitUsd")
            val dueIdx = c.getColumnIndexOrThrow("dueDateMillis")
            val notesIdx = c.getColumnIndexOrThrow("notes")
            val createdIdx = c.getColumnIndexOrThrow("createdAt")

            while (c.moveToNext()) {
                val due = if (c.isNull(dueIdx)) null else c.getLong(dueIdx)
                list.add(
                    DebtorEntity(
                        id = c.getLong(idIdx),
                        name = c.getString(nameIdx),
                        phone = c.getString(phoneIdx),
                        address = c.getString(addressIdx),
                        creditLimitIqd = c.getDouble(limitIqdIdx),
                        creditLimitUsd = c.getDouble(limitUsdIdx),
                        dueDateMillis = due,
                        notes = c.getString(notesIdx),
                        createdAt = c.getLong(createdIdx)
                    )
                )
            }
        }
        return list
    }

    private fun queryTransactionsSync(
        orderClause: String,
        whereClause: String = "",
        whereArgs: Array<String>? = null
    ): List<LedgerTransactionEntity> {
        val list = mutableListOf<LedgerTransactionEntity>()
        val db = readableDatabase
        val sql = buildString {
            append("SELECT * FROM transactions ")
            if (whereClause.isNotBlank()) append("WHERE $whereClause ")
            append(orderClause)
        }
        val cursor: Cursor = db.rawQuery(sql, whereArgs)
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val debtorIdIdx = c.getColumnIndexOrThrow("debtorId")
            val typeIdx = c.getColumnIndexOrThrow("type")
            val currencyIdx = c.getColumnIndexOrThrow("currency")
            val amountIdx = c.getColumnIndexOrThrow("amount")
            val tsIdx = c.getColumnIndexOrThrow("timestamp")
            val noteIdx = c.getColumnIndexOrThrow("note")

            while (c.moveToNext()) {
                list.add(
                    LedgerTransactionEntity(
                        id = c.getLong(idIdx),
                        debtorId = c.getLong(debtorIdIdx),
                        type = c.getString(typeIdx),
                        currency = c.getString(currencyIdx),
                        amount = c.getDouble(amountIdx),
                        timestamp = c.getLong(tsIdx),
                        note = c.getString(noteIdx)
                    )
                )
            }
        }
        return list
    }

    override fun observeAllDebtors(): Flow<List<DebtorEntity>> = debtorsFlow.asStateFlow()

    override suspend fun getAllDebtorsOnce(): List<DebtorEntity> = withContext(Dispatchers.IO) {
        queryDebtorsSync("ORDER BY id ASC")
    }

    override suspend fun getDebtorById(id: Long): DebtorEntity? = withContext(Dispatchers.IO) {
        debtorsFlow.value.find { it.id == id }
            ?: queryDebtorsSync("WHERE id = $id LIMIT 1").firstOrNull()
    }

    override suspend fun getDebtorCount(): Int = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM debtors", null)
        cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    private fun debtorToContentValues(debtor: DebtorEntity, includeId: Boolean): ContentValues {
        return ContentValues().apply {
            if (includeId && debtor.id > 0L) put("id", debtor.id)
            put("name", debtor.name)
            put("phone", debtor.phone)
            put("address", debtor.address)
            put("creditLimitIqd", debtor.creditLimitIqd)
            put("creditLimitUsd", debtor.creditLimitUsd)
            if (debtor.dueDateMillis != null) {
                put("dueDateMillis", debtor.dueDateMillis)
            } else {
                putNull("dueDateMillis")
            }
            put("notes", debtor.notes)
            put("createdAt", debtor.createdAt)
        }
    }

    private fun txToContentValues(tx: LedgerTransactionEntity, includeId: Boolean): ContentValues {
        return ContentValues().apply {
            if (includeId && tx.id > 0L) put("id", tx.id)
            put("debtorId", tx.debtorId)
            put("type", tx.type)
            put("currency", tx.currency)
            put("amount", tx.amount)
            put("timestamp", tx.timestamp)
            put("note", tx.note)
        }
    }

    override suspend fun insertDebtor(debtor: DebtorEntity): Long = withContext(Dispatchers.IO) {
        val id = writableDatabase.insertWithOnConflict(
            "debtors",
            null,
            debtorToContentValues(debtor, includeId = debtor.id > 0L),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        refreshFlowsSync()
        id
    }

    override suspend fun insertDebtors(debtors: List<DebtorEntity>) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            debtors.forEach { d ->
                db.insertWithOnConflict(
                    "debtors",
                    null,
                    debtorToContentValues(d, includeId = d.id > 0L),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refreshFlowsSync()
    }

    override suspend fun updateDebtor(debtor: DebtorEntity) = withContext(Dispatchers.IO) {
        writableDatabase.update(
            "debtors",
            debtorToContentValues(debtor, includeId = false),
            "id = ?",
            arrayOf(debtor.id.toString())
        )
        refreshFlowsSync()
    }

    override suspend fun deleteDebtor(debtor: DebtorEntity) {
        deleteDebtorById(debtor.id)
    }

    override suspend fun deleteDebtorById(id: Long) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete("transactions", "debtorId = ?", arrayOf(id.toString()))
        db.delete("debtors", "id = ?", arrayOf(id.toString()))
        refreshFlowsSync()
    }

    override fun observeAllTransactions(): Flow<List<LedgerTransactionEntity>> =
        transactionsFlow.asStateFlow()

    override suspend fun getAllTransactionsOnce(): List<LedgerTransactionEntity> =
        withContext(Dispatchers.IO) {
            queryTransactionsSync("ORDER BY timestamp ASC, id ASC")
        }

    override fun observeTransactionsForDebtor(debtorId: Long): Flow<List<LedgerTransactionEntity>> =
        transactionsFlow.map { list ->
            list.filter { it.debtorId == debtorId }
                .sortedWith(compareBy<LedgerTransactionEntity> { it.timestamp }.thenBy { it.id })
        }

    override suspend fun getTransactionsForDebtorOnce(debtorId: Long): List<LedgerTransactionEntity> =
        withContext(Dispatchers.IO) {
            queryTransactionsSync(
                orderClause = "ORDER BY timestamp ASC, id ASC",
                whereClause = "debtorId = ?",
                whereArgs = arrayOf(debtorId.toString())
            )
        }

    override suspend fun insertTransaction(transaction: LedgerTransactionEntity): Long =
        withContext(Dispatchers.IO) {
            val id = writableDatabase.insertWithOnConflict(
                "transactions",
                null,
                txToContentValues(transaction, includeId = transaction.id > 0L),
                SQLiteDatabase.CONFLICT_REPLACE
            )
            refreshFlowsSync()
            id
        }

    override suspend fun insertTransactions(transactions: List<LedgerTransactionEntity>) =
        withContext(Dispatchers.IO) {
            val db = writableDatabase
            db.beginTransaction()
            try {
                transactions.forEach { tx ->
                    db.insertWithOnConflict(
                        "transactions",
                        null,
                        txToContentValues(tx, includeId = tx.id > 0L),
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            refreshFlowsSync()
        }

    override suspend fun deleteTransaction(transaction: LedgerTransactionEntity) {
        deleteTransactionById(transaction.id)
    }

    override suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("transactions", "id = ?", arrayOf(id.toString()))
        refreshFlowsSync()
    }

    override suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        writableDatabase.delete("transactions", null, null)
        refreshFlowsSync()
    }

    override suspend fun clearAllDebtors() = withContext(Dispatchers.IO) {
        writableDatabase.delete("debtors", null, null)
        refreshFlowsSync()
    }

    override suspend fun replaceEntireDatabase(
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("transactions", null, null)
            db.delete("debtors", null, null)
            debtors.forEach { d ->
                db.insertWithOnConflict(
                    "debtors",
                    null,
                    debtorToContentValues(d, includeId = d.id > 0L),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            transactions.forEach { tx ->
                db.insertWithOnConflict(
                    "transactions",
                    null,
                    txToContentValues(tx, includeId = tx.id > 0L),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refreshFlowsSync()
    }

    companion object {
        @Volatile
        private var INSTANCE: LedgerDatabase? = null

        fun getInstance(context: Context): LedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = LedgerDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
