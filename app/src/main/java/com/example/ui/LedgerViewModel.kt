package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.CloudUserSession
import com.example.data.cloud.CloudUserSettings
import com.example.data.cloud.FirebaseCloudSyncManager
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
import kotlin.math.round

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

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ledger_cloud_prefs_v4", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Cloud Authentication Session State (Firebase Auth)
    private val _authSession = MutableStateFlow<CloudUserSession?>(loadSavedSession())
    val authSession: StateFlow<CloudUserSession?> = _authSession.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authInfoMessage = MutableStateFlow<String?>(null)
    val authInfoMessage: StateFlow<String?> = _authInfoMessage.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow("جاهز للمزامنة السحابية والعمل بدون إنترنت")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    // Onboarding & Office Name
    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", true)
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
        refreshLocalSnapshotInfo(appContext)
        _authSession.value?.let { existingSession ->
            syncWithCloudNow(silent = true)
        }
    }

    private fun loadSavedSession(): CloudUserSession? {
        val uid = prefs.getString("auth_uid", null) ?: return null
        val email = prefs.getString("auth_email", "") ?: ""
        val idToken = prefs.getString("auth_id_token", "") ?: ""
        val refreshToken = prefs.getString("auth_refresh_token", "") ?: ""
        val expiresAt = prefs.getLong("auth_expires_at", 0L)
        val isOffline = prefs.getBoolean("auth_is_offline", false)
        return CloudUserSession(
            uid = uid,
            email = email,
            idToken = idToken,
            refreshToken = refreshToken,
            expiresAtMillis = expiresAt,
            isOfflineLocalSession = isOffline
        )
    }

    private fun saveSessionToPrefs(session: CloudUserSession?) {
        if (session == null) {
            prefs.edit()
                .remove("auth_uid")
                .remove("auth_email")
                .remove("auth_id_token")
                .remove("auth_refresh_token")
                .remove("auth_expires_at")
                .remove("auth_is_offline")
                .apply()
        } else {
            prefs.edit()
                .putString("auth_uid", session.uid)
                .putString("auth_email", session.email)
                .putString("auth_id_token", session.idToken)
                .putString("auth_refresh_token", session.refreshToken)
                .putLong("auth_expires_at", session.expiresAtMillis)
                .putBoolean("auth_is_offline", session.isOfflineLocalSession)
                .apply()
        }
    }

    fun clearAuthMessages() {
        _authErrorMessage.value = null
        _authInfoMessage.value = null
    }

    fun registerNewOfficeAccount(
        officeNameInput: String,
        exchangeRateInput: Double,
        emailInput: String,
        passwordInput: String
    ) {
        val cleanOffice = officeNameInput.trim().ifBlank { "مكتب المحاسبة والتجارة العامة" }
        val validRate = if (exchangeRateInput > 0.0) exchangeRateInput else 1500.0
        val cleanEmail = emailInput.trim()

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authInfoMessage.value = null

            val signUpResult = FirebaseCloudSyncManager.signUpWithEmailPassword(cleanEmail, passwordInput)
            if (signUpResult.isSuccess) {
                val session = signUpResult.getOrThrow()
                // Start clean state (0 debtors, 0 transactions) for the new account
                repository.clearAllData()

                _officeName.value = cleanOffice
                _usdToIqdRate.value = validRate
                _isOnboardingCompleted.value = true
                _pinCode.value = null
                _isAppLocked.value = false
                _currentScreen.value = AppScreen.DASHBOARD

                prefs.edit()
                    .putString("office_name", cleanOffice)
                    .putFloat("usd_to_iqd_rate", validRate.toFloat())
                    .putBoolean("onboarding_completed", true)
                    .remove("app_pin_code")
                    .putString("local_pw_${cleanEmail.lowercase()}", passwordInput)
                    .putString("local_uid_${cleanEmail.lowercase()}", session.uid)
                    .apply()

                _authSession.value = session
                saveSessionToPrefs(session)

                val newSettings = CloudUserSettings(
                    officeName = cleanOffice,
                    usdToIqdRate = validRate,
                    showUnifiedEstimate = _showUnifiedEstimate.value,
                    pinCode = null
                )
                FirebaseCloudSyncManager.saveUserOfflineSnapshot(
                    context = appContext,
                    uid = session.uid,
                    settings = newSettings,
                    debtors = emptyList(),
                    transactions = emptyList()
                )
                val pushed = FirebaseCloudSyncManager.pushSettingsToFirestore(session, newSettings)
                _cloudSyncStatus.value = if (pushed) {
                    "متصل ومتزامن سحابياً (users/${session.uid.take(6)}...)"
                } else {
                    "محفوظ محلياً • سيتم الرفع للسحابة تلقائياً عند توفر الشبكة"
                }
                _statusBannerMessage.value = "تم إنشاء حساب «$cleanOffice» بنجاح والبدء بسجل نظيف تماماً"
            } else {
                // If network is completely unavailable during signup, allow offline local account creation & queue sync
                if (!FirebaseCloudSyncManager.isNetworkAvailable(appContext)) {
                    val offlineUid = "offline_${cleanEmail.lowercase().hashCode().toUInt()}"
                    val offlineSession = CloudUserSession(
                        uid = offlineUid,
                        email = cleanEmail,
                        idToken = "",
                        refreshToken = "",
                        expiresAtMillis = Long.MAX_VALUE,
                        isOfflineLocalSession = true
                    )
                    repository.clearAllData()
                    _officeName.value = cleanOffice
                    _usdToIqdRate.value = validRate
                    _isOnboardingCompleted.value = true
                    _authSession.value = offlineSession
                    saveSessionToPrefs(offlineSession)
                    prefs.edit()
                        .putString("office_name", cleanOffice)
                        .putFloat("usd_to_iqd_rate", validRate.toFloat())
                        .putBoolean("onboarding_completed", true)
                        .putString("local_pw_${cleanEmail.lowercase()}", passwordInput)
                        .putString("local_uid_${cleanEmail.lowercase()}", offlineUid)
                        .apply()
                    FirebaseCloudSyncManager.saveUserOfflineSnapshot(
                        context = appContext,
                        uid = offlineUid,
                        settings = CloudUserSettings(cleanOffice, validRate, true, null),
                        debtors = emptyList(),
                        transactions = emptyList()
                    )
                    _cloudSyncStatus.value = "وضع عدم الاتصال (Offline Persistence مفعل)"
                    _statusBannerMessage.value = "تم إنشاء حساب «$cleanOffice» محلياً (سيتم المزامنة فور الاتصال بالإنترنت)"
                } else {
                    _authErrorMessage.value = signUpResult.exceptionOrNull()?.message
                        ?: "تعذر إنشاء الحساب، يرجى التحقق من البريد الإلكتروني وكلمة المرور."
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun loginWithEmailPassword(
        emailInput: String,
        passwordInput: String
    ) {
        val cleanEmail = emailInput.trim()
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authInfoMessage.value = null

            val signInResult = FirebaseCloudSyncManager.signInWithEmailPassword(cleanEmail, passwordInput)
            if (signInResult.isSuccess) {
                val session = signInResult.getOrThrow()
                _authSession.value = session
                saveSessionToPrefs(session)
                prefs.edit()
                    .putString("local_pw_${cleanEmail.lowercase()}", passwordInput)
                    .putString("local_uid_${cleanEmail.lowercase()}", session.uid)
                    .apply()

                // Fetch user's isolated data from Firestore: users/{uid}/settings, debtors, transactions
                val remoteResult = FirebaseCloudSyncManager.fetchAllUserDataFromFirestore(session)
                val cachedBundle = FirebaseCloudSyncManager.loadUserOfflineSnapshot(appContext, session.uid)

                if (remoteResult.isSuccess) {
                    val bundle = remoteResult.getOrThrow()
                    val resolvedDebtors = if (bundle.debtors.isNotEmpty() || cachedBundle == null) {
                        bundle.debtors
                    } else {
                        cachedBundle.debtors
                    }
                    val resolvedTxs = if (bundle.transactions.isNotEmpty() || cachedBundle == null) {
                        bundle.transactions
                    } else {
                        cachedBundle.transactions
                    }
                    val resolvedSettings = bundle.settings ?: cachedBundle?.settings

                    repository.replaceEntireDatabase(resolvedDebtors, resolvedTxs)
                    if (resolvedSettings != null) {
                        applySettingsLocally(resolvedSettings)
                    }
                    FirebaseCloudSyncManager.saveUserOfflineSnapshot(
                        context = appContext,
                        uid = session.uid,
                        settings = currentCloudSettings(),
                        debtors = resolvedDebtors,
                        transactions = resolvedTxs
                    )
                    _cloudSyncStatus.value = "متصل ومتزامن سحابياً ✓ (${resolvedDebtors.size} مدين • ${resolvedTxs.size} عملية)"
                    _statusBannerMessage.value = "مرحباً بعودتك إلى «${_officeName.value}» • تم استرجاع بياناتك السحابية بنجاح"
                } else if (cachedBundle != null) {
                    repository.replaceEntireDatabase(cachedBundle.debtors, cachedBundle.transactions)
                    cachedBundle.settings?.let { applySettingsLocally(it) }
                    _cloudSyncStatus.value = "تم استرجاع النسخة المحلية المحفوظة (Offline Persistence)"
                    _statusBannerMessage.value = "تم فتح الحساب واسترجاع بياناتك المحفوظة"
                } else {
                    repository.clearAllData()
                    _cloudSyncStatus.value = "متصل بالسحابة • سجل جديد جاهز للعمل"
                    _statusBannerMessage.value = "تم تسجيل الدخول بنجاح"
                }
                _currentScreen.value = AppScreen.DASHBOARD
            } else {
                // Offline fallback if user previously logged in on this device
                val savedPw = prefs.getString("local_pw_${cleanEmail.lowercase()}", null)
                val savedUid = prefs.getString("local_uid_${cleanEmail.lowercase()}", null)
                if (!FirebaseCloudSyncManager.isNetworkAvailable(appContext) &&
                    savedPw == passwordInput &&
                    !savedUid.isNullOrBlank()
                ) {
                    val offlineSession = CloudUserSession(
                        uid = savedUid,
                        email = cleanEmail,
                        idToken = "",
                        refreshToken = "",
                        expiresAtMillis = Long.MAX_VALUE,
                        isOfflineLocalSession = true
                    )
                    _authSession.value = offlineSession
                    saveSessionToPrefs(offlineSession)
                    FirebaseCloudSyncManager.loadUserOfflineSnapshot(appContext, savedUid)?.let { bundle ->
                        repository.replaceEntireDatabase(bundle.debtors, bundle.transactions)
                        bundle.settings?.let { applySettingsLocally(it) }
                    }
                    _cloudSyncStatus.value = "وضع عدم الاتصال (Offline Persistence)"
                    _statusBannerMessage.value = "تم تسجيل الدخول بدون إنترنت واسترجاع بياناتك المحلية"
                } else {
                    _authErrorMessage.value = signInResult.exceptionOrNull()?.message
                        ?: "البريد الإلكتروني أو كلمة المرور غير صحيحة."
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun sendPasswordReset(emailInput: String) {
        val cleanEmail = emailInput.trim()
        if (cleanEmail.isBlank()) return
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authInfoMessage.value = null
            val res = FirebaseCloudSyncManager.sendPasswordResetEmail(cleanEmail)
            if (res.isSuccess) {
                _authInfoMessage.value = "تم إرسال رابط استعادة وتغيير كلمة المرور إلى بريدك الإلكتروني ($cleanEmail) بنجاح."
            } else {
                _authErrorMessage.value = res.exceptionOrNull()?.message
                    ?: "تعذر إرسال بريد استعادة كلمة المرور."
            }
            _isAuthLoading.value = false
        }
    }

    fun logoutAccount() {
        viewModelScope.launch {
            // Save snapshot for current user before clearing local working DB
            val currentSession = _authSession.value
            if (currentSession != null) {
                val debtors = repository.getAllDebtorsOnce()
                val txs = repository.getAllTransactionsOnce()
                FirebaseCloudSyncManager.saveUserOfflineSnapshot(
                    context = appContext,
                    uid = currentSession.uid,
                    settings = currentCloudSettings(),
                    debtors = debtors,
                    transactions = txs
                )
            }
            repository.clearAllData()
            _authSession.value = null
            saveSessionToPrefs(null)
            _selectedDebtorId.value = null
            _currentScreen.value = AppScreen.DASHBOARD
            _isAppLocked.value = false
            _authErrorMessage.value = null
            _authInfoMessage.value = "تم تسجيل الخروج من الحساب بنجاح."
        }
    }

    fun syncWithCloudNow(silent: Boolean = false) {
        val session = _authSession.value ?: return
        viewModelScope.launch {
            val localDebtors = repository.getAllDebtorsOnce()
            val localTxs = repository.getAllTransactionsOnce()
            val settings = currentCloudSettings()

            // Always save local per-user offline cache first
            FirebaseCloudSyncManager.saveUserOfflineSnapshot(
                context = appContext,
                uid = session.uid,
                settings = settings,
                debtors = localDebtors,
                transactions = localTxs
            )

            if (!FirebaseCloudSyncManager.isNetworkAvailable(appContext)) {
                _cloudSyncStatus.value = "يعمل بدون إنترنت • محفوظ محلياً (سيتم الرفع تلقائياً عند الاتصال)"
                if (!silent) {
                    _statusBannerMessage.value = "لا يوجد اتصال حالياً • جميع بياناتك محفوظة محلياً بأمان"
                }
                return@launch
            }

            if (localDebtors.isEmpty() && localTxs.isEmpty()) {
                // Pull from cloud if local is empty
                val remoteRes = FirebaseCloudSyncManager.fetchAllUserDataFromFirestore(session)
                if (remoteRes.isSuccess) {
                    val bundle = remoteRes.getOrThrow()
                    if (bundle.debtors.isNotEmpty() || bundle.transactions.isNotEmpty()) {
                        repository.replaceEntireDatabase(bundle.debtors, bundle.transactions)
                    }
                    bundle.settings?.let { applySettingsLocally(it) }
                    _cloudSyncStatus.value = "متصل ومتزامن سحابياً ✓ (users/${session.uid.take(6)}...)"
                    if (!silent) {
                        _statusBannerMessage.value = "تمت المزامنة السحابية بنجاح"
                    }
                }
            } else {
                val ok = FirebaseCloudSyncManager.pushFullSnapshotToFirestore(
                    session = session,
                    settings = settings,
                    debtors = localDebtors,
                    transactions = localTxs
                )
                _cloudSyncStatus.value = if (ok) {
                    "متصل ومتزامن سحابياً ✓ (${localDebtors.size} مدين • ${localTxs.size} عملية)"
                } else {
                    "محفوظ محلياً • بانتظار المزامنة السحابية"
                }
                if (!silent) {
                    _statusBannerMessage.value = if (ok) {
                        "تم رفع ومزامنة جميع البيانات مع السحابة بنجاح"
                    } else {
                        "تم حفظ البيانات محلياً وسيتم رفعها تلقائياً"
                    }
                }
            }
        }
    }

    private fun currentCloudSettings(): CloudUserSettings = CloudUserSettings(
        officeName = _officeName.value,
        usdToIqdRate = _usdToIqdRate.value,
        showUnifiedEstimate = _showUnifiedEstimate.value,
        pinCode = _pinCode.value,
        updatedAt = System.currentTimeMillis()
    )

    private fun applySettingsLocally(settings: CloudUserSettings) {
        _officeName.value = settings.officeName
        _usdToIqdRate.value = settings.usdToIqdRate
        _showUnifiedEstimate.value = settings.showUnifiedEstimate
        _pinCode.value = settings.pinCode
        prefs.edit()
            .putString("office_name", settings.officeName)
            .putFloat("usd_to_iqd_rate", settings.usdToIqdRate.toFloat())
            .putBoolean("show_unified_estimate", settings.showUnifiedEstimate)
            .apply {
                if (!settings.pinCode.isNullOrBlank()) {
                    putString("app_pin_code", settings.pinCode)
                } else {
                    remove("app_pin_code")
                }
            }
            .apply()
    }

    private suspend fun persistOfflineCacheForCurrentUser() {
        val session = _authSession.value ?: return
        val debtors = repository.getAllDebtorsOnce()
        val txs = repository.getAllTransactionsOnce()
        FirebaseCloudSyncManager.saveUserOfflineSnapshot(
            context = appContext,
            uid = session.uid,
            settings = currentCloudSettings(),
            debtors = debtors,
            transactions = txs
        )
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

        viewModelScope.launch {
            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.pushSettingsToFirestore(session, currentCloudSettings())
            }
        }
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

        viewModelScope.launch {
            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.pushSettingsToFirestore(session, currentCloudSettings())
            }
        }
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
        viewModelScope.launch {
            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.pushSettingsToFirestore(session, currentCloudSettings())
            }
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
                val entity = DebtorEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    creditLimitIqd = creditLimitIqd.coerceAtLeast(0.0),
                    creditLimitUsd = creditLimitUsd.coerceAtLeast(0.0),
                    dueDateMillis = dueDateMillis,
                    notes = notes.trim()
                )
                val newId = repository.insertDebtor(entity)
                val savedDebtor = entity.copy(id = newId)
                var createdInitTx: LedgerTransactionEntity? = null

                if (initialDebtAmount > 0.0) {
                    val initTx = LedgerTransactionEntity(
                        debtorId = newId,
                        type = TransactionType.DEBT,
                        currency = initialDebtCurrency,
                        amount = initialDebtAmount,
                        timestamp = System.currentTimeMillis(),
                        note = initialDebtNote.ifBlank { "رصيد دين أولي" }
                    )
                    val txId = repository.insertTransaction(initTx)
                    createdInitTx = initTx.copy(id = txId)
                }
                _statusBannerMessage.value = "تمت إضافة ملف المدين «${name.trim()}» بنجاح"

                persistOfflineCacheForCurrentUser()
                _authSession.value?.let { session ->
                    FirebaseCloudSyncManager.pushDebtorToFirestore(session, savedDebtor)
                    createdInitTx?.let { tx ->
                        FirebaseCloudSyncManager.pushTransactionToFirestore(session, tx)
                    }
                }
            } else {
                val current = repository.getDebtorById(existingId) ?: return@launch
                val updated = current.copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    creditLimitIqd = creditLimitIqd.coerceAtLeast(0.0),
                    creditLimitUsd = creditLimitUsd.coerceAtLeast(0.0),
                    dueDateMillis = dueDateMillis,
                    notes = notes.trim()
                )
                repository.updateDebtor(updated)
                _statusBannerMessage.value = "تم تحديث بيانات «${name.trim()}» بنجاح"

                persistOfflineCacheForCurrentUser()
                _authSession.value?.let { session ->
                    FirebaseCloudSyncManager.pushDebtorToFirestore(session, updated)
                }
            }
        }
    }

    fun deleteDebtor(debtorId: Long) {
        viewModelScope.launch {
            val debtorName = repository.getDebtorById(debtorId)?.name ?: ""
            val debtorTxs = repository.getTransactionsForDebtorOnce(debtorId).map { it.id }
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

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.deleteDebtorFromFirestore(session, debtorId, debtorTxs)
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
            val safeRate = if (_usdToIqdRate.value > 0.0) _usdToIqdRate.value else 1500.0
            val cleanNote = note.trim().ifBlank {
                if (type == TransactionType.DEBT) "تسجيل دين جديد" else "دفعة تسديد"
            }

            val insertedTxs = mutableListOf<LedgerTransactionEntity>()

            // Automatic cross-currency payment conversion when paying in one currency while owing in the other
            if (type == TransactionType.PAYMENT && currency == CurrencyCode.USD && amount > prevBalUsd + 0.009 && prevBalIqd > 0.009) {
                val directUsdPart = prevBalUsd
                val excessUsd = amount - directUsdPart
                val convertedIqd = round(excessUsd * safeRate)
                if (directUsdPart > 0.009) {
                    val tx1 = LedgerTransactionEntity(
                        debtorId = debtorId,
                        type = TransactionType.PAYMENT,
                        currency = CurrencyCode.USD,
                        amount = directUsdPart,
                        timestamp = timestamp,
                        note = "$cleanNote (تصفية رصيد الدولار)"
                    )
                    val id1 = repository.insertTransaction(tx1)
                    insertedTxs.add(tx1.copy(id = id1))
                }
                val tx2 = LedgerTransactionEntity(
                    debtorId = debtorId,
                    type = TransactionType.PAYMENT,
                    currency = CurrencyCode.IQD,
                    amount = convertedIqd,
                    timestamp = timestamp + 1,
                    note = "$cleanNote • تصريف تلقائي (${Formatters.formatUsd(excessUsd)} × ${Formatters.formatNumber(safeRate)} = ${Formatters.formatIqd(convertedIqd)})"
                )
                val id2 = repository.insertTransaction(tx2)
                insertedTxs.add(tx2.copy(id = id2))
            } else if (type == TransactionType.PAYMENT && currency == CurrencyCode.IQD && amount > prevBalIqd + 0.009 && prevBalUsd > 0.009) {
                val directIqdPart = prevBalIqd
                val excessIqd = amount - directIqdPart
                val convertedUsd = round((excessIqd / safeRate) * 100.0) / 100.0
                if (directIqdPart > 0.009) {
                    val tx1 = LedgerTransactionEntity(
                        debtorId = debtorId,
                        type = TransactionType.PAYMENT,
                        currency = CurrencyCode.IQD,
                        amount = directIqdPart,
                        timestamp = timestamp,
                        note = "$cleanNote (تصفية رصيد الدينار)"
                    )
                    val id1 = repository.insertTransaction(tx1)
                    insertedTxs.add(tx1.copy(id = id1))
                }
                val tx2 = LedgerTransactionEntity(
                    debtorId = debtorId,
                    type = TransactionType.PAYMENT,
                    currency = CurrencyCode.USD,
                    amount = convertedUsd,
                    timestamp = timestamp + 1,
                    note = "$cleanNote • تصريف تلقائي (${Formatters.formatIqd(excessIqd)} ÷ ${Formatters.formatNumber(safeRate)} = ${Formatters.formatUsd(convertedUsd)})"
                )
                val id2 = repository.insertTransaction(tx2)
                insertedTxs.add(tx2.copy(id = id2))
            } else {
                val rawTx = LedgerTransactionEntity(
                    debtorId = debtorId,
                    type = type,
                    currency = currency,
                    amount = amount,
                    timestamp = timestamp,
                    note = cleanNote
                )
                val txId = repository.insertTransaction(rawTx)
                insertedTxs.add(rawTx.copy(id = txId))
            }

            val updatedTxs = repository.getTransactionsForDebtorOnce(debtorId)
            val newDebtIqd = updatedTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val newPaidIqd = updatedTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
            val newBalIqd = (newDebtIqd - newPaidIqd).coerceAtLeast(0.0)

            val newDebtUsd = updatedTxs.filter { it.type == TransactionType.DEBT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val newPaidUsd = updatedTxs.filter { it.type == TransactionType.PAYMENT && it.currency == CurrencyCode.USD }.sumOf { it.amount }
            val newBalUsd = (newDebtUsd - newPaidUsd).coerceAtLeast(0.0)

            val formattedAmount = Formatters.formatByCurrency(amount, currency)
            val lastInsertedId = insertedTxs.lastOrNull()?.id ?: 0L

            if (type == TransactionType.PAYMENT) {
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
                        note = insertedTxs.lastOrNull()?.note ?: cleanNote,
                        timestamp = timestamp,
                        remainingBalanceIqd = newBalIqd,
                        remainingBalanceUsd = newBalUsd,
                        receiptNumber = "RCP-$lastInsertedId-${timestamp % 10000}"
                    )
                    _statusBannerMessage.value = "تم تسجيل دفعة تسديد بقيمة $formattedAmount للعميل «${debtor.name}»"
                }
            } else {
                _statusBannerMessage.value = "تم تسجيل دين جديد بقيمة $formattedAmount على «${debtor.name}»"
            }

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                insertedTxs.forEach { tx ->
                    FirebaseCloudSyncManager.pushTransactionToFirestore(session, tx)
                }
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
            val updated = existingTransaction.copy(
                type = newType,
                currency = newCurrency,
                amount = newAmount,
                note = newNote.trim().ifBlank {
                    if (newType == TransactionType.DEBT) "تسجيل دين" else "دفعة تسديد"
                }
            )
            repository.updateTransaction(updated)
            _statusBannerMessage.value = "تم تعديل العملية المالية وتحديث الأرصدة التراكمية بنجاح"

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.pushTransactionToFirestore(session, updated)
            }
        }
    }

    fun settleAllRemainingBalancesForDebtor(summary: DebtorSummary) {
        if (!summary.hasActiveDebt) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var lastCurrency = CurrencyCode.IQD
            var lastAmount = 0.0
            val createdTxs = mutableListOf<LedgerTransactionEntity>()

            if (summary.currentBalanceIqd > 0.009) {
                val txIqd = LedgerTransactionEntity(
                    debtorId = summary.debtor.id,
                    type = TransactionType.PAYMENT,
                    currency = CurrencyCode.IQD,
                    amount = summary.currentBalanceIqd,
                    timestamp = now,
                    note = "تسديد كامل المتبقي بالدينار العراقي وتصفير الحساب"
                )
                val id1 = repository.insertTransaction(txIqd)
                createdTxs.add(txIqd.copy(id = id1))
                lastCurrency = CurrencyCode.IQD
                lastAmount = summary.currentBalanceIqd
            }
            if (summary.currentBalanceUsd > 0.009) {
                val txUsd = LedgerTransactionEntity(
                    debtorId = summary.debtor.id,
                    type = TransactionType.PAYMENT,
                    currency = CurrencyCode.USD,
                    amount = summary.currentBalanceUsd,
                    timestamp = now + 1,
                    note = "تسديد كامل المتبقي بالدولار الأمريكي وتصفير الحساب"
                )
                val id2 = repository.insertTransaction(txUsd)
                createdTxs.add(txUsd.copy(id = id2))
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

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                createdTxs.forEach { tx ->
                    FirebaseCloudSyncManager.pushTransactionToFirestore(session, tx)
                }
            }
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            _statusBannerMessage.value = "تم حذف العملية وتحديث الأرصدة التراكمية تلقائياً"

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.deleteTransactionFromFirestore(session, transactionId)
            }
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

            persistOfflineCacheForCurrentUser()
            _authSession.value?.let { session ->
                FirebaseCloudSyncManager.wipeAllUserCloudDocuments(session)
            }
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

                persistOfflineCacheForCurrentUser()
                _authSession.value?.let { session ->
                    FirebaseCloudSyncManager.pushFullSnapshotToFirestore(
                        session = session,
                        settings = currentCloudSettings(),
                        debtors = debtors,
                        transactions = transactions
                    )
                }
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
