package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ClearanceEvent
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.DebtorSummary
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.PaymentReceiptEvent
import com.example.data.local.StatementEntry
import com.example.data.local.TransactionType
import com.example.data.repository.LedgerRepository
import com.example.util.ExportAndBackupManager
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    DEBTORS,
    DEBTOR_DETAIL,
    REPORTS,
    BACKUP
}

enum class DebtorFilter(val labelAr: String) {
    ALL("الكل"),
    ACTIVE("المدينون النشطون"),
    OVERDUE("المتأخرون بالسداد"),
    HIGHEST_IQD("الأعلى ديناً (د.ع)"),
    HIGHEST_USD("الأعلى ديناً ($)"),
    SETTLED("الحسابات المسددة")
}

enum class ReportTab(val labelAr: String) {
    DAILY("الجرد اليومي"),
    MONTHLY("الجرد الشهري"),
    ANNUAL("الجرد السنوي"),
    CUSTOM("فلترة مخصصة")
}

data class MonthlyStatPoint(
    val monthIndex: Int,
    val monthName: String,
    val totalDebtIqd: Double,
    val totalPaidIqd: Double,
    val collectionRateIqd: Double,
    val totalDebtUsd: Double,
    val totalPaidUsd: Double,
    val collectionRateUsd: Double
)

data class CommittedCustomerRank(
    val debtor: DebtorEntity,
    val paidIqd: Double,
    val debtIqd: Double,
    val paidUsd: Double,
    val debtUsd: Double,
    val commitmentPercentage: Double
)

data class LedgerUiState(
    val isLoading: Boolean = true,
    val debtors: List<DebtorEntity> = emptyList(),
    val transactions: List<LedgerTransactionEntity> = emptyList(),
    val debtorSummaries: List<DebtorSummary> = emptyList(),
    val filteredSummaries: List<DebtorSummary> = emptyList(),
    val debtorMap: Map<Long, DebtorEntity> = emptyMap(),
    // Strict Dual-Currency Totals (IQD)
    val totalOutstandingDebtIqd: Double = 0.0,
    val totalGrossDebtIqd: Double = 0.0,
    val totalCollectedPaymentsIqd: Double = 0.0,
    val collectionRatioIqd: Double = 0.0,
    // Strict Dual-Currency Totals (USD)
    val totalOutstandingDebtUsd: Double = 0.0,
    val totalGrossDebtUsd: Double = 0.0,
    val totalCollectedPaymentsUsd: Double = 0.0,
    val collectionRatioUsd: Double = 0.0,
    // Debtor Counts
    val activeDebtorsCount: Int = 0,
    val overdueDebtorsCount: Int = 0,
    val settledDebtorsCount: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: DebtorFilter = DebtorFilter.ALL,
    val selectedDebtorId: Long? = null
)

class LedgerViewModel(
    private val repository: LedgerRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("ledger_ui_prefs_v3", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Onboarding & Office Name
    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _officeName = MutableStateFlow(
        prefs.getString("office_name", "مكتب المحاسبة والتجارة العامة") ?: "مكتب المحاسبة والتجارة العامة"
    )
    val officeName: StateFlow<String> = _officeName.asStateFlow()

    // Exchange rate (1 USD = X IQD)
    private val _usdToIqdRate = MutableStateFlow(
        prefs.getFloat("usd_to_iqd_rate", 1500f).toDouble()
    )
    val usdToIqdRate: StateFlow<Double> = _usdToIqdRate.asStateFlow()

    private val _showUnifiedEstimate = MutableStateFlow(
        prefs.getBoolean("show_unified_estimate", true)
    )
    val showUnifiedEstimate: StateFlow<Boolean> = _showUnifiedEstimate.asStateFlow()

    // Optional 4-digit PIN Lock
    private val _pinCode = MutableStateFlow<String?>(
        prefs.getString("app_pin_code", null)?.takeIf { it.length == 4 }
    )
    val pinCode: StateFlow<String?> = _pinCode.asStateFlow()

    private val _isAppLocked = MutableStateFlow(
        !prefs.getString("app_pin_code", null).isNullOrBlank()
    )
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DebtorFilter.ALL)
    val selectedFilter: StateFlow<DebtorFilter> = _selectedFilter.asStateFlow()

    private val _selectedDebtorId = MutableStateFlow<Long?>(null)
    val selectedDebtorId: StateFlow<Long?> = _selectedDebtorId.asStateFlow()

    private val _activeClearanceEvent = MutableStateFlow<ClearanceEvent?>(null)
    val activeClearanceEvent: StateFlow<ClearanceEvent?> = _activeClearanceEvent.asStateFlow()

    private val _activePaymentReceiptEvent = MutableStateFlow<PaymentReceiptEvent?>(null)
    val activePaymentReceiptEvent: StateFlow<PaymentReceiptEvent?> = _activePaymentReceiptEvent.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _hasLocalSnapshot = MutableStateFlow(false)
    val hasLocalSnapshot: StateFlow<Boolean> = _hasLocalSnapshot.asStateFlow()

    private val _localSnapshotTimestamp = MutableStateFlow(0L)
    val localSnapshotTimestamp: StateFlow<Long> = _localSnapshotTimestamp.asStateFlow()

    init {
        refreshLocalSnapshotInfo(context)
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.allDebtors,
        repository.allTransactions,
        _searchQuery,
        _selectedFilter,
        _selectedDebtorId
    ) { debtors, transactions, query, filter, selectedId ->
        val now = System.currentTimeMillis()
        val txByDebtor = transactions.groupBy { it.debtorId }
        val debtorMap = debtors.associateBy { it.id }

        val summaries = debtors.map { debtor ->
            val dTxs = txByDebtor[debtor.id].orEmpty()

            val totalDebtIqd = dTxs
                .filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }
                .sumOf { it.amount }
            val totalPaidIqd = dTxs
                .filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }
                .sumOf { it.amount }
            val currentBalanceIqd = (totalDebtIqd - totalPaidIqd).coerceAtLeast(0.0)

            val totalDebtUsd = dTxs
                .filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }
                .sumOf { it.amount }
            val totalPaidUsd = dTxs
                .filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }
                .sumOf { it.amount }
            val currentBalanceUsd = (totalDebtUsd - totalPaidUsd).coerceAtLeast(0.0)

            val hasActive = currentBalanceIqd > 0.009 || currentBalanceUsd > 0.009
            val lastTx = dTxs.maxByOrNull { it.timestamp }?.timestamp
            val isOverdue = hasActive &&
                debtor.dueDateMillis != null &&
                debtor.dueDateMillis < now
            val overdueDays = if (isOverdue) {
                Formatters.calculateDaysOverdue(debtor.dueDateMillis, now)
            } else {
                0
            }
            val isOverLimit = (debtor.creditLimitIqd > 0.0 && currentBalanceIqd > debtor.creditLimitIqd) ||
                (debtor.creditLimitUsd > 0.0 && currentBalanceUsd > debtor.creditLimitUsd)
            val isSettled = (totalDebtIqd > 0.0 || totalDebtUsd > 0.0) && !hasActive

            DebtorSummary(
                debtor = debtor,
                totalDebtIqd = totalDebtIqd,
                totalPaidIqd = totalPaidIqd,
                currentBalanceIqd = currentBalanceIqd,
                totalDebtUsd = totalDebtUsd,
                totalPaidUsd = totalPaidUsd,
                currentBalanceUsd = currentBalanceUsd,
                lastTransactionDate = lastTx,
                transactionCount = dTxs.size,
                isOverdue = isOverdue,
                overdueDays = overdueDays,
                isOverLimit = isOverLimit,
                isSettled = isSettled
            )
        }

        val normalizedQuery = query.trim().lowercase()
        val searched = if (normalizedQuery.isEmpty()) {
            summaries
        } else {
            summaries.filter {
                it.debtor.name.lowercase().contains(normalizedQuery) ||
                    it.debtor.phone.lowercase().contains(normalizedQuery) ||
                    it.debtor.address.lowercase().contains(normalizedQuery)
            }
        }

        val filtered = when (filter) {
            DebtorFilter.ALL -> searched
            DebtorFilter.ACTIVE -> searched.filter { it.hasActiveDebt }
            DebtorFilter.OVERDUE -> searched.filter { it.isOverdue }
            DebtorFilter.HIGHEST_IQD -> searched.sortedByDescending { it.currentBalanceIqd }
            DebtorFilter.HIGHEST_USD -> searched.sortedByDescending { it.currentBalanceUsd }
            DebtorFilter.SETTLED -> searched.filter { it.isSettled }
        }

        val totalGrossIqd = summaries.sumOf { it.totalDebtIqd }
        val totalCollectedIqd = summaries.sumOf { it.totalPaidIqd }
        val totalOutstandingIqd = summaries.sumOf { it.currentBalanceIqd }
        val ratioIqd = if (totalGrossIqd > 0.0) {
            ((totalCollectedIqd / totalGrossIqd) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        val totalGrossUsd = summaries.sumOf { it.totalDebtUsd }
        val totalCollectedUsd = summaries.sumOf { it.totalPaidUsd }
        val totalOutstandingUsd = summaries.sumOf { it.currentBalanceUsd }
        val ratioUsd = if (totalGrossUsd > 0.0) {
            ((totalCollectedUsd / totalGrossUsd) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        LedgerUiState(
            isLoading = false,
            debtors = debtors,
            transactions = transactions,
            debtorSummaries = summaries,
            filteredSummaries = filtered,
            debtorMap = debtorMap,
            totalOutstandingDebtIqd = totalOutstandingIqd,
            totalGrossDebtIqd = totalGrossIqd,
            totalCollectedPaymentsIqd = totalCollectedIqd,
            collectionRatioIqd = ratioIqd,
            totalOutstandingDebtUsd = totalOutstandingUsd,
            totalGrossDebtUsd = totalGrossUsd,
            totalCollectedPaymentsUsd = totalCollectedUsd,
            collectionRatioUsd = ratioUsd,
            activeDebtorsCount = summaries.count { it.hasActiveDebt },
            overdueDebtorsCount = summaries.count { it.isOverdue },
            settledDebtorsCount = summaries.count { it.isSettled },
            searchQuery = query,
            selectedFilter = filter,
            selectedDebtorId = selectedId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState()
    )

    fun completeOnboardingSetup(
        officeNameInput: String,
        rateInput: Double,
        optionalPin: String? = null
    ) {
        val cleanOffice = officeNameInput.trim().ifBlank { "مكتب المحاسبة والتجارة العامة" }
        val validRate = if (rateInput > 0.0) rateInput else 1500.0
        val cleanPin = optionalPin?.trim()?.takeIf { it.length == 4 && it.all(Char::isDigit) }

        _officeName.value = cleanOffice
        _usdToIqdRate.value = validRate
        _isOnboardingCompleted.value = true

        val editor = prefs.edit()
            .putString("office_name", cleanOffice)
            .putFloat("usd_to_iqd_rate", validRate.toFloat())
            .putBoolean("onboarding_completed", true)

        if (cleanPin != null) {
            _pinCode.value = cleanPin
            editor.putString("app_pin_code", cleanPin)
        }
        editor.apply()
        _statusBannerMessage.value = "مرحباً بك في «$cleanOffice» • تم حفظ إعدادات النظام بنجاح"
    }

    fun updateOfficeAndExchangeSettings(
        officeNameInput: String,
        rate: Double,
        showUnified: Boolean
    ) {
        val cleanOffice = officeNameInput.trim().ifBlank { _officeName.value }
        val validRate = if (rate > 0.0) rate else 1500.0
        _officeName.value = cleanOffice
        _usdToIqdRate.value = validRate
        _showUnifiedEstimate.value = showUnified
        prefs.edit()
            .putString("office_name", cleanOffice)
            .putFloat("usd_to_iqd_rate", validRate.toFloat())
            .putBoolean("show_unified_estimate", showUnified)
            .putBoolean("onboarding_completed", true)
            .apply()
        _statusBannerMessage.value = "تم تحديث اسم المكتب «$cleanOffice» وسعر الصرف (1 $ = ${Formatters.formatIqd(validRate)})"
    }

    fun updatePinCode(newPin: String?) {
        val cleanPin = newPin?.trim()?.takeIf { it.length == 4 && it.all(Char::isDigit) }
        _pinCode.value = cleanPin
        if (cleanPin != null) {
            prefs.edit().putString("app_pin_code", cleanPin).apply()
            _statusBannerMessage.value = "تم تفعيل قفل التطبيق برمز الحماية (4 أرقام)"
        } else {
            _isAppLocked.value = false
            prefs.edit().remove("app_pin_code").apply()
            _statusBannerMessage.value = "تم إلغاء قفل التطبيق برمز PIN"
        }
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val current = _pinCode.value
        return if (current == null || enteredPin.trim() == current) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockAppNow() {
        if (_pinCode.value != null) {
            _isAppLocked.value = true
        }
    }

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("dark_mode", next).apply()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openDebtorProfile(debtorId: Long) {
        _selectedDebtorId.value = debtorId
        _currentScreen.value = AppScreen.DEBTOR_DETAIL
    }

    fun navigateBackFromDetail() {
        _currentScreen.value = AppScreen.DEBTORS
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: DebtorFilter) {
        _selectedFilter.value = filter
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun dismissClearanceModal() {
        _activeClearanceEvent.value = null
    }

    fun dismissPaymentReceiptModal() {
        _activePaymentReceiptEvent.value = null
    }

    fun triggerPaymentReceiptModal(
        debtor: DebtorEntity,
        entry: StatementEntry
    ) {
        _activePaymentReceiptEvent.value = PaymentReceiptEvent(
            debtor = debtor,
            paymentAmount = entry.transaction.amount,
            currency = entry.transaction.currency,
            note = entry.transaction.note,
            timestamp = entry.transaction.timestamp,
            remainingBalanceIqd = entry.runningBalanceIqd,
            remainingBalanceUsd = entry.runningBalanceUsd,
            receiptNumber = "RCP-${entry.transaction.id}-${entry.transaction.timestamp % 10000}"
        )
    }

    fun triggerClearanceModalForDebtor(summary: DebtorSummary) {
        val lastCurrency = if (summary.totalPaidUsd > 0.0 && summary.totalPaidIqd == 0.0) {
            CurrencyCode.USD
        } else {
            CurrencyCode.IQD
        }
        val lastAmount = if (lastCurrency == CurrencyCode.USD) summary.totalPaidUsd else summary.totalPaidIqd
        _activeClearanceEvent.value = ClearanceEvent(
            debtor = summary.debtor,
            totalPaidIqd = summary.totalPaidIqd,
            totalPaidUsd = summary.totalPaidUsd,
            lastPaymentAmount = lastAmount,
            lastPaymentCurrency = lastCurrency,
            clearanceTimestamp = System.currentTimeMillis(),
            referenceNumber = "CLR-${summary.debtor.id}-${System.currentTimeMillis() % 100000}"
        )
    }

    fun buildStatementEntries(
        debtorId: Long,
        allTransactions: List<LedgerTransactionEntity>
    ): List<StatementEntry> {
        val chronological = allTransactions
            .filter { it.debtorId == debtorId }
            .sortedWith(compareBy<LedgerTransactionEntity> { it.timestamp }.thenBy { it.id })

        var runningIqd = 0.0
        var runningUsd = 0.0
        return chronological.map { tx ->
            if (tx.currency == CurrencyCode.USD) {
                if (tx.type == TransactionType.DEBT) {
                    runningUsd += tx.amount
                } else {
                    runningUsd = (runningUsd - tx.amount).coerceAtLeast(0.0)
                }
            } else {
                if (tx.type == TransactionType.DEBT) {
                    runningIqd += tx.amount
                } else {
                    runningIqd = (runningIqd - tx.amount).coerceAtLeast(0.0)
                }
            }
            StatementEntry(
                transaction = tx,
                runningBalanceIqd = runningIqd,
                runningBalanceUsd = runningUsd
            )
        }
    }

    fun saveDebtor(
        existingId: Long? = null,
        name: String,
        phone: String,
        address: String,
        creditLimitIqd: Double,
        creditLimitUsd: Double,
        dueDateMillis: Long?,
        notes: String,
        initialDebtAmount: Double = 0.0,
        initialDebtCurrency: String = CurrencyCode.IQD,
        initialDebtNote: String = "رصيد افتتاحي مسجل عند فتح الحساب"
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (existingId == null || existingId == 0L) {
                val newId = repository.insertDebtor(
                    DebtorEntity(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        creditLimitIqd = creditLimitIqd.coerceAtLeast(0.0),
                        creditLimitUsd = creditLimitUsd.coerceAtLeast(0.0),
                        dueDateMillis = dueDateMillis,
                        notes = notes.trim()
                    )
                )
                if (initialDebtAmount > 0.0) {
                    repository.insertTransaction(
                        LedgerTransactionEntity(
                            debtorId = newId,
                            type = TransactionType.DEBT,
                            currency = initialDebtCurrency,
                            amount = initialDebtAmount,
                            timestamp = System.currentTimeMillis(),
                            note = initialDebtNote.ifBlank { "رصيد دين أولي" }
                        )
                    )
                }
                _statusBannerMessage.value = "تمت إضافة ملف المدين «${name.trim()}» بنجاح"
            } else {
                val current = repository.getDebtorById(existingId) ?: return@launch
                repository.updateDebtor(
                    current.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        creditLimitIqd = creditLimitIqd.coerceAtLeast(0.0),
                        creditLimitUsd = creditLimitUsd.coerceAtLeast(0.0),
                        dueDateMillis = dueDateMillis,
                        notes = notes.trim()
                    )
                )
                _statusBannerMessage.value = "تم تحديث بيانات «${name.trim()}» بنجاح"
            }
        }
    }

    fun deleteDebtor(debtorId: Long) {
        viewModelScope.launch {
            val debtorName = repository.getDebtorById(debtorId)?.name ?: ""
            repository.deleteDebtor(debtorId)
            if (_selectedDebtorId.value == debtorId) {
                _selectedDebtorId.value = null
                _currentScreen.value = AppScreen.DEBTORS
            }
            _statusBannerMessage.value = if (debtorName.isNotBlank()) {
                "تم حذف سجل المدين «$debtorName» وكافة عملياته نهائياً"
            } else {
                "تم حذف سجل المدين وكافة عملياته نهائياً"
            }
        }
    }

    fun recordTransaction(
        debtorId: Long,
        type: String,
        currency: String,
        amount: Double,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val debtor = repository.getDebtorById(debtorId) ?: return@launch
            val existingTxs = repository.getTransactionsForDebtorOnce(debtorId)

            val prevDebtIqd = existingTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val prevPaidIqd = existingTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val prevBalIqd = (prevDebtIqd - prevPaidIqd).coerceAtLeast(0.0)

            val prevDebtUsd = existingTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val prevPaidUsd = existingTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val prevBalUsd = (prevDebtUsd - prevPaidUsd).coerceAtLeast(0.0)

            val hadActiveDebt = prevBalIqd > 0.009 || prevBalUsd > 0.009
            val cleanNote = note.trim().ifBlank {
                if (type == TransactionType.DEBT) "تسجيل دين جديد" else "دفعة تسديد"
            }

            val txId = repository.insertTransaction(
                LedgerTransactionEntity(
                    debtorId = debtorId,
                    type = type,
                    currency = currency,
                    amount = amount,
                    timestamp = timestamp,
                    note = cleanNote
                )
            )

            val formattedAmount = Formatters.formatByCurrency(amount, currency)
            if (type == TransactionType.PAYMENT) {
                val newPaidIqd = if (currency == CurrencyCode.IQD) prevPaidIqd + amount else prevPaidIqd
                val newPaidUsd = if (currency == CurrencyCode.USD) prevPaidUsd + amount else prevPaidUsd
                val newBalIqd = (prevDebtIqd - newPaidIqd).coerceAtLeast(0.0)
                val newBalUsd = (prevDebtUsd - newPaidUsd).coerceAtLeast(0.0)

                if (hadActiveDebt && newBalIqd <= 0.009 && newBalUsd <= 0.009) {
                    _activeClearanceEvent.value = ClearanceEvent(
                        debtor = debtor,
                        totalPaidIqd = newPaidIqd,
                        totalPaidUsd = newPaidUsd,
                        lastPaymentAmount = amount,
                        lastPaymentCurrency = currency,
                        clearanceTimestamp = timestamp,
                        referenceNumber = "CLR-${debtor.id}-${timestamp % 100000}"
                    )
                } else {
                    _activePaymentReceiptEvent.value = PaymentReceiptEvent(
                        debtor = debtor,
                        paymentAmount = amount,
                        currency = currency,
                        note = cleanNote,
                        timestamp = timestamp,
                        remainingBalanceIqd = newBalIqd,
                        remainingBalanceUsd = newBalUsd,
                        receiptNumber = "RCP-$txId-${timestamp % 10000}"
                    )
                    _statusBannerMessage.value = "تم تسجيل دفعة تسديد بقيمة $formattedAmount للعميل «${debtor.name}»"
                }
            } else {
                _statusBannerMessage.value = "تم تسجيل دين جديد بقيمة $formattedAmount على «${debtor.name}»"
            }
        }
    }

    fun updateTransaction(
        existingTransaction: LedgerTransactionEntity,
        newType: String,
        newCurrency: String,
        newAmount: Double,
        newNote: String
    ) {
        if (newAmount <= 0.0) return
        viewModelScope.launch {
            repository.updateTransaction(
                existingTransaction.copy(
                    type = newType,
                    currency = newCurrency,
                    amount = newAmount,
                    note = newNote.trim().ifBlank {
                        if (newType == TransactionType.DEBT) "تسجيل دين" else "دفعة تسديد"
                    }
                )
            )
            _statusBannerMessage.value = "تم تعديل العملية المالية وتحديث الأرصدة التراكمية بنجاح"
        }
    }

    fun settleAllRemainingBalancesForDebtor(summary: DebtorSummary) {
        if (!summary.hasActiveDebt) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var lastCurrency = CurrencyCode.IQD
            var lastAmount = 0.0
            if (summary.currentBalanceIqd > 0.009) {
                repository.insertTransaction(
                    LedgerTransactionEntity(
                        debtorId = summary.debtor.id,
                        type = TransactionType.PAYMENT,
                        currency = CurrencyCode.IQD,
                        amount = summary.currentBalanceIqd,
                        timestamp = now,
                        note = "تسديد كامل المتبقي بالدينار العراقي وتصفير الحساب"
                    )
                )
                lastCurrency = CurrencyCode.IQD
                lastAmount = summary.currentBalanceIqd
            }
            if (summary.currentBalanceUsd > 0.009) {
                repository.insertTransaction(
                    LedgerTransactionEntity(
                        debtorId = summary.debtor.id,
                        type = TransactionType.PAYMENT,
                        currency = CurrencyCode.USD,
                        amount = summary.currentBalanceUsd,
                        timestamp = now + 1,
                        note = "تسديد كامل المتبقي بالدولار الأمريكي وتصفير الحساب"
                    )
                )
                lastCurrency = CurrencyCode.USD
                lastAmount = summary.currentBalanceUsd
            }

            _activeClearanceEvent.value = ClearanceEvent(
                debtor = summary.debtor,
                totalPaidIqd = summary.totalDebtIqd,
                totalPaidUsd = summary.totalDebtUsd,
                lastPaymentAmount = lastAmount,
                lastPaymentCurrency = lastCurrency,
                clearanceTimestamp = now,
                referenceNumber = "CLR-${summary.debtor.id}-${now % 100000}"
            )
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            _statusBannerMessage.value = "تم حذف العملية وتحديث الأرصدة التراكمية تلقائياً"
        }
    }

    fun resetAllApplicationData(context: Context) {
        viewModelScope.launch {
            repository.clearAllData()
            ExportAndBackupManager.deleteInternalBackupSnapshot(context)
            refreshLocalSnapshotInfo(context)
            _selectedDebtorId.value = null
            _currentScreen.value = AppScreen.DASHBOARD
            _statusBannerMessage.value = "تم تصفير ومسح جميع بيانات السجل بنجاح (الرصيد الآن 0)"
        }
    }

    fun refreshLocalSnapshotInfo(context: Context) {
        val (exists, timestamp) = ExportAndBackupManager.getInternalBackupSnapshotInfo(context)
        _hasLocalSnapshot.value = exists
        _localSnapshotTimestamp.value = timestamp
    }

    fun createLocalSnapshotBackup(context: Context) {
        viewModelScope.launch {
            val debtors = repository.getAllDebtorsOnce()
            val txs = repository.getAllTransactionsOnce()
            val json = ExportAndBackupManager.exportDatabaseToJson(
                debtors = debtors,
                transactions = txs,
                officeName = _officeName.value
            )
            ExportAndBackupManager.saveInternalBackupSnapshot(context, json)
            refreshLocalSnapshotInfo(context)
            _statusBannerMessage.value = "تم حفظ النسخة الاحتياطية المحلية الفورية بنجاح (${debtors.size} عميل، ${txs.size} عملية)"
        }
    }

    fun restoreFromLocalSnapshot(context: Context) {
        viewModelScope.launch {
            val json = ExportAndBackupManager.readInternalBackupSnapshot(context)
            if (json.isNullOrBlank()) {
                _statusBannerMessage.value = "لا توجد نسخة احتياطية محلية محفوظة للاستعادة"
                return@launch
            }
            restoreDatabaseFromJsonString(json)
        }
    }

    fun restoreDatabaseFromJsonString(jsonString: String) {
        viewModelScope.launch {
            try {
                val (debtors, transactions) = ExportAndBackupManager.parseDatabaseFromJson(jsonString)
                repository.replaceEntireDatabase(debtors, transactions)
                _statusBannerMessage.value = "تمت استعادة قاعدة البيانات بنجاح (${debtors.size} مدين و ${transactions.size} عملية مالية)"
            } catch (e: Exception) {
                _statusBannerMessage.value = "تعذر قراءة ملف النسخة الاحتياطية: تأكد من صحة ملف JSON"
            }
        }
    }

    suspend fun generateFullBackupJsonString(): String {
        val debtors = repository.getAllDebtorsOnce()
        val txs = repository.getAllTransactionsOnce()
        return ExportAndBackupManager.exportDatabaseToJson(
            debtors = debtors,
            transactions = txs,
            officeName = _officeName.value
        )
    }

    companion object {
        fun provideFactory(
            repository: LedgerRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LedgerViewModel(repository, context.applicationContext) as T
            }
        }
    }
}
