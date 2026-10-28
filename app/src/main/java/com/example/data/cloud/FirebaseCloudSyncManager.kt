package com.example.data.cloud

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.CurrencyCode
import com.example.data.local.DebtorEntity
import com.example.data.local.LedgerTransactionEntity
import com.example.data.local.TransactionType
import com.example.util.ExportAndBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class CloudUserSession(
    val uid: String,
    val email: String,
    val idToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
    val isOfflineLocalSession: Boolean = false
)

data class CloudUserSettings(
    val officeName: String,
    val usdToIqdRate: Double,
    val showUnifiedEstimate: Boolean = true,
    val pinCode: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CloudAccountBundle(
    val settings: CloudUserSettings?,
    val debtors: List<DebtorEntity>,
    val transactions: List<LedgerTransactionEntity>
)

object FirebaseCloudSyncManager {

    // Exact Firebase Project Configuration (mizan-debt)
    const val API_KEY = "AIzaSyANInqHh_7whX_qbQeuJYTfNduSxHIqGtc"
    const val AUTH_DOMAIN = "mizan-debt.firebaseapp.com"
    const val PROJECT_ID = "mizan-debt"
    const val STORAGE_BUCKET = "mizan-debt.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "117431611267"
    const val APP_ID = "1:117431611267:web:1bdaa3d15290991462d0b6"

    private const val AUTH_BASE_URL = "https://identitytoolkit.googleapis.com/v1"
    private const val TOKEN_REFRESH_URL = "https://securetoken.googleapis.com/v1/token?key=$API_KEY"
    private const val FIRESTORE_BASE_URL =
        "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    // =========================================================================
    // 1. CLOUD ACCOUNT AUTHENTICATION (Sign Up, Sign In, Password Reset)
    // =========================================================================

    suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Result<CloudUserSession> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val (code, body) = executeJsonRequest(
                urlStr = "$AUTH_BASE_URL/accounts:signUp?key=$API_KEY",
                method = "POST",
                body = payload.toString()
            )
            if (code in 200..299) {
                val json = JSONObject(body)
                val expiresInSec = json.optString("expiresIn", "3600").toLongOrNull() ?: 3600L
                Result.success(
                    CloudUserSession(
                        uid = json.getString("localId"),
                        email = json.optString("email", cleanEmail),
                        idToken = json.getString("idToken"),
                        refreshToken = json.optString("refreshToken", ""),
                        expiresAtMillis = System.currentTimeMillis() + (expiresInSec * 1000L)
                    )
                )
            } else {
                Result.failure(Exception(parseFriendlyArabicAuthError(body)))
            }
        } catch (_: Exception) {
            Result.failure(Exception("يرجى التأكد من اتصالك بالإنترنت والمحاولة مرة أخرى"))
        }
    }

    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<CloudUserSession> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val (code, body) = executeJsonRequest(
                urlStr = "$AUTH_BASE_URL/accounts:signInWithPassword?key=$API_KEY",
                method = "POST",
                body = payload.toString()
            )
            if (code in 200..299) {
                val json = JSONObject(body)
                val expiresInSec = json.optString("expiresIn", "3600").toLongOrNull() ?: 3600L
                Result.success(
                    CloudUserSession(
                        uid = json.getString("localId"),
                        email = json.optString("email", cleanEmail),
                        idToken = json.getString("idToken"),
                        refreshToken = json.optString("refreshToken", ""),
                        expiresAtMillis = System.currentTimeMillis() + (expiresInSec * 1000L)
                    )
                )
            } else {
                Result.failure(Exception(parseFriendlyArabicAuthError(body)))
            }
        } catch (_: Exception) {
            Result.failure(Exception("يرجى التأكد من اتصالك بالإنترنت والمحاولة مرة أخرى"))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("requestType", "PASSWORD_RESET")
                put("email", email.trim())
            }
            val (code, body) = executeJsonRequest(
                urlStr = "$AUTH_BASE_URL/accounts:sendOobCode?key=$API_KEY",
                method = "POST",
                body = payload.toString()
            )
            if (code in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseFriendlyArabicAuthError(body)))
            }
        } catch (_: Exception) {
            Result.failure(Exception("يرجى التأكد من اتصالك بالإنترنت لإرسال رابط استعادة كلمة المرور"))
        }
    }

    suspend fun ensureFreshSession(session: CloudUserSession): CloudUserSession =
        withContext(Dispatchers.IO) {
            if (session.isOfflineLocalSession || session.refreshToken.isBlank()) return@withContext session
            if (System.currentTimeMillis() < session.expiresAtMillis - 120_000L) {
                return@withContext session
            }
            try {
                val payload = JSONObject().apply {
                    put("grant_type", "refresh_token")
                    put("refresh_token", session.refreshToken)
                }
                val (code, body) = executeJsonRequest(
                    urlStr = TOKEN_REFRESH_URL,
                    method = "POST",
                    body = payload.toString()
                )
                if (code in 200..299) {
                    val json = JSONObject(body)
                    val expiresInSec = json.optString("expires_in", "3600").toLongOrNull() ?: 3600L
                    session.copy(
                        idToken = json.optString("id_token", session.idToken),
                        refreshToken = json.optString("refresh_token", session.refreshToken),
                        expiresAtMillis = System.currentTimeMillis() + (expiresInSec * 1000L)
                    )
                } else {
                    session
                }
            } catch (_: Exception) {
                session
            }
        }

    // =========================================================================
    // 2. CLOUD DATABASE OPERATIONS (users/{uid}/settings, debtors, transactions)
    // =========================================================================

    suspend fun pushSettingsToFirestore(
        session: CloudUserSession,
        settings: CloudUserSettings
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) return@withContext false
        val active = ensureFreshSession(session)
        val fields = JSONObject().apply {
            put("officeName", fsString(settings.officeName))
            put("usdToIqdRate", fsDouble(settings.usdToIqdRate))
            put("showUnifiedEstimate", fsBoolean(settings.showUnifiedEstimate))
            put("pinCode", fsString(settings.pinCode ?: ""))
            put("updatedAt", fsInteger(settings.updatedAt))
        }
        val docBody = JSONObject().apply { put("fields", fields) }
        val url = "$FIRESTORE_BASE_URL/users/${active.uid}/settings/profile"
        val (code, _) = executeJsonRequest(
            urlStr = url,
            method = "PATCH",
            body = docBody.toString(),
            bearerToken = active.idToken
        )
        code in 200..299
    }

    suspend fun pushDebtorToFirestore(
        session: CloudUserSession,
        debtor: DebtorEntity
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession || debtor.id <= 0L) return@withContext false
        val active = ensureFreshSession(session)
        val fields = JSONObject().apply {
            put("id", fsInteger(debtor.id))
            put("name", fsString(debtor.name))
            put("phone", fsString(debtor.phone))
            put("address", fsString(debtor.address))
            put("creditLimitIqd", fsDouble(debtor.creditLimitIqd))
            put("creditLimitUsd", fsDouble(debtor.creditLimitUsd))
            if (debtor.dueDateMillis != null) {
                put("dueDateMillis", fsInteger(debtor.dueDateMillis))
            } else {
                put("dueDateMillis", fsNull())
            }
            put("notes", fsString(debtor.notes))
            put("createdAt", fsInteger(debtor.createdAt))
        }
        val docBody = JSONObject().apply { put("fields", fields) }
        val url = "$FIRESTORE_BASE_URL/users/${active.uid}/debtors/${debtor.id}"
        val (code, _) = executeJsonRequest(
            urlStr = url,
            method = "PATCH",
            body = docBody.toString(),
            bearerToken = active.idToken
        )
        code in 200..299
    }

    suspend fun deleteDebtorFromFirestore(
        session: CloudUserSession,
        debtorId: Long,
        txIds: List<Long>
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) return@withContext false
        val active = ensureFreshSession(session)
        txIds.forEach { txId ->
            executeJsonRequest(
                urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/transactions/$txId",
                method = "DELETE",
                bearerToken = active.idToken
            )
        }
        val (code, _) = executeJsonRequest(
            urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/debtors/$debtorId",
            method = "DELETE",
            bearerToken = active.idToken
        )
        code in 200..299 || code == 404
    }

    suspend fun pushTransactionToFirestore(
        session: CloudUserSession,
        tx: LedgerTransactionEntity
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession || tx.id <= 0L) return@withContext false
        val active = ensureFreshSession(session)
        val fields = JSONObject().apply {
            put("id", fsInteger(tx.id))
            put("debtorId", fsInteger(tx.debtorId))
            put("type", fsString(tx.type))
            put("currency", fsString(tx.currency))
            put("amount", fsDouble(tx.amount))
            put("timestamp", fsInteger(tx.timestamp))
            put("note", fsString(tx.note))
        }
        val docBody = JSONObject().apply { put("fields", fields) }
        val url = "$FIRESTORE_BASE_URL/users/${active.uid}/transactions/${tx.id}"
        val (code, _) = executeJsonRequest(
            urlStr = url,
            method = "PATCH",
            body = docBody.toString(),
            bearerToken = active.idToken
        )
        code in 200..299
    }

    suspend fun deleteTransactionFromFirestore(
        session: CloudUserSession,
        txId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) return@withContext false
        val active = ensureFreshSession(session)
        val (code, _) = executeJsonRequest(
            urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/transactions/$txId",
            method = "DELETE",
            bearerToken = active.idToken
        )
        code in 200..299 || code == 404
    }

    suspend fun pushFullSnapshotToFirestore(
        session: CloudUserSession,
        settings: CloudUserSettings,
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) return@withContext false
        val active = ensureFreshSession(session)
        var ok = pushSettingsToFirestore(active, settings)
        debtors.forEach { d ->
            if (!pushDebtorToFirestore(active, d)) ok = false
        }
        transactions.forEach { t ->
            if (!pushTransactionToFirestore(active, t)) ok = false
        }
        ok
    }

    suspend fun wipeAllUserCloudDocuments(
        session: CloudUserSession
    ): Boolean = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) return@withContext false
        val active = ensureFreshSession(session)
        val remote = fetchAllUserDataFromFirestore(active).getOrNull() ?: return@withContext false
        remote.transactions.forEach { tx ->
            deleteTransactionFromFirestore(active, tx.id)
        }
        remote.debtors.forEach { d ->
            deleteDebtorFromFirestore(active, d.id, emptyList())
        }
        true
    }

    suspend fun fetchAllUserDataFromFirestore(
        session: CloudUserSession
    ): Result<CloudAccountBundle> = withContext(Dispatchers.IO) {
        if (session.isOfflineLocalSession) {
            return@withContext Result.failure(Exception("الجلسة محلية فقط"))
        }
        try {
            val active = ensureFreshSession(session)

            // 1. Fetch users/{uid}/settings/profile
            var cloudSettings: CloudUserSettings? = null
            val (settingsCode, settingsBody) = executeJsonRequest(
                urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/settings/profile",
                method = "GET",
                bearerToken = active.idToken
            )
            if (settingsCode in 200..299) {
                val doc = JSONObject(settingsBody)
                doc.optJSONObject("fields")?.let { f ->
                    cloudSettings = parseSettingsFromFields(f)
                }
            } else {
                val (listCode, listBody) = executeJsonRequest(
                    urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/settings?pageSize=10",
                    method = "GET",
                    bearerToken = active.idToken
                )
                if (listCode in 200..299) {
                    val docs = JSONObject(listBody).optJSONArray("documents")
                    if (docs != null && docs.length() > 0) {
                        docs.getJSONObject(0).optJSONObject("fields")?.let { f ->
                            cloudSettings = parseSettingsFromFields(f)
                        }
                    }
                }
            }

            // 2. Fetch users/{uid}/debtors
            val debtors = mutableListOf<DebtorEntity>()
            val (debtorsCode, debtorsBody) = executeJsonRequest(
                urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/debtors?pageSize=500",
                method = "GET",
                bearerToken = active.idToken
            )
            if (debtorsCode in 200..299) {
                val docs = JSONObject(debtorsBody).optJSONArray("documents") ?: JSONArray()
                for (i in 0 until docs.length()) {
                    val doc = docs.getJSONObject(i)
                    val docName = doc.optString("name", "")
                    val fallbackId = docName.substringAfterLast("/").toLongOrNull() ?: (i + 1L)
                    doc.optJSONObject("fields")?.let { f ->
                        debtors.add(parseDebtorFromFields(f, fallbackId))
                    }
                }
            }

            // 3. Fetch users/{uid}/transactions
            val transactions = mutableListOf<LedgerTransactionEntity>()
            val (txCode, txBody) = executeJsonRequest(
                urlStr = "$FIRESTORE_BASE_URL/users/${active.uid}/transactions?pageSize=1000",
                method = "GET",
                bearerToken = active.idToken
            )
            if (txCode in 200..299) {
                val docs = JSONObject(txBody).optJSONArray("documents") ?: JSONArray()
                for (i in 0 until docs.length()) {
                    val doc = docs.getJSONObject(i)
                    val docName = doc.optString("name", "")
                    val fallbackId = docName.substringAfterLast("/").toLongOrNull() ?: (i + 1L)
                    doc.optJSONObject("fields")?.let { f ->
                        transactions.add(parseTransactionFromFields(f, fallbackId))
                    }
                }
            }

            Result.success(
                CloudAccountBundle(
                    settings = cloudSettings,
                    debtors = debtors.sortedBy { it.id },
                    transactions = transactions.sortedBy { it.timestamp }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 3. PER-USER LOCAL CACHE
    // =========================================================================

    fun saveUserOfflineSnapshot(
        context: Context,
        uid: String,
        settings: CloudUserSettings,
        debtors: List<DebtorEntity>,
        transactions: List<LedgerTransactionEntity>
    ) {
        try {
            val jsonStr = ExportAndBackupManager.exportDatabaseToJson(
                debtors = debtors,
                transactions = transactions,
                officeName = settings.officeName
            )
            val root = JSONObject(jsonStr).apply {
                put("uid", uid)
                put("usdToIqdRate", settings.usdToIqdRate)
                put("showUnifiedEstimate", settings.showUnifiedEstimate)
                put("pinCode", settings.pinCode ?: "")
                put("updatedAt", settings.updatedAt)
            }
            val file = File(context.filesDir, "offline_user_${uid.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.json")
            file.writeText(root.toString(), Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }

    fun loadUserOfflineSnapshot(
        context: Context,
        uid: String
    ): CloudAccountBundle? {
        return try {
            val file = File(context.filesDir, "offline_user_${uid.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.json")
            if (!file.exists()) return null
            val content = file.readText(Charsets.UTF_8)
            val root = JSONObject(content)
            val (debtors, transactions) = ExportAndBackupManager.parseDatabaseFromJson(content)
            val settings = CloudUserSettings(
                officeName = root.optString("officeName", "مكتب المحاسبة والتجارة العامة"),
                usdToIqdRate = root.optDouble("usdToIqdRate", 1500.0),
                showUnifiedEstimate = root.optBoolean("showUnifiedEstimate", true),
                pinCode = root.optString("pinCode", "").ifBlank { null },
                updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
            )
            CloudAccountBundle(settings = settings, debtors = debtors, transactions = transactions)
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // INTERNAL HELPERS & PURE ARABIC ERROR TRANSLATION
    // =========================================================================

    private fun fsString(value: String): JSONObject =
        JSONObject().put("stringValue", value)

    private fun fsInteger(value: Long): JSONObject =
        JSONObject().put("integerValue", value.toString())

    private fun fsDouble(value: Double): JSONObject =
        JSONObject().put("doubleValue", value)

    private fun fsBoolean(value: Boolean): JSONObject =
        JSONObject().put("booleanValue", value)

    private fun fsNull(): JSONObject =
        JSONObject().put("nullValue", JSONObject.NULL)

    private fun readFsString(fields: JSONObject, key: String, default: String = ""): String =
        fields.optJSONObject(key)?.optString("stringValue", default) ?: default

    private fun readFsLong(fields: JSONObject, key: String, default: Long = 0L): Long {
        val obj = fields.optJSONObject(key) ?: return default
        return obj.optString("integerValue", "").toLongOrNull()
            ?: obj.optDouble("doubleValue", default.toDouble()).toLong()
    }

    private fun readFsNullableLong(fields: JSONObject, key: String): Long? {
        val obj = fields.optJSONObject(key) ?: return null
        if (obj.has("nullValue")) return null
        return obj.optString("integerValue", "").toLongOrNull()
            ?: if (obj.has("doubleValue")) obj.optDouble("doubleValue").toLong() else null
    }

    private fun readFsDouble(fields: JSONObject, key: String, default: Double = 0.0): Double {
        val obj = fields.optJSONObject(key) ?: return default
        if (obj.has("doubleValue")) return obj.optDouble("doubleValue", default)
        if (obj.has("integerValue")) return obj.optString("integerValue", "0").toDoubleOrNull() ?: default
        return default
    }

    private fun readFsBool(fields: JSONObject, key: String, default: Boolean = true): Boolean =
        fields.optJSONObject(key)?.optBoolean("booleanValue", default) ?: default

    private fun parseSettingsFromFields(f: JSONObject): CloudUserSettings {
        val office = readFsString(f, "officeName", "مكتب المحاسبة والتجارة العامة")
        val rate = readFsDouble(f, "usdToIqdRate", 1500.0)
        val unified = readFsBool(f, "showUnifiedEstimate", true)
        val pin = readFsString(f, "pinCode", "").ifBlank { null }
        val updatedAt = readFsLong(f, "updatedAt", System.currentTimeMillis())
        return CloudUserSettings(
            officeName = office,
            usdToIqdRate = if (rate > 0.0) rate else 1500.0,
            showUnifiedEstimate = unified,
            pinCode = pin,
            updatedAt = updatedAt
        )
    }

    private fun parseDebtorFromFields(f: JSONObject, fallbackId: Long): DebtorEntity {
        val id = readFsLong(f, "id", fallbackId).let { if (it > 0L) it else fallbackId }
        return DebtorEntity(
            id = id,
            name = readFsString(f, "name", "عميل #$id"),
            phone = readFsString(f, "phone", ""),
            address = readFsString(f, "address", ""),
            creditLimitIqd = readFsDouble(f, "creditLimitIqd", 0.0),
            creditLimitUsd = readFsDouble(f, "creditLimitUsd", 0.0),
            dueDateMillis = readFsNullableLong(f, "dueDateMillis"),
            notes = readFsString(f, "notes", ""),
            createdAt = readFsLong(f, "createdAt", System.currentTimeMillis())
        )
    }

    private fun parseTransactionFromFields(f: JSONObject, fallbackId: Long): LedgerTransactionEntity {
        val id = readFsLong(f, "id", fallbackId).let { if (it > 0L) it else fallbackId }
        return LedgerTransactionEntity(
            id = id,
            debtorId = readFsLong(f, "debtorId", 0L),
            type = readFsString(f, "type", TransactionType.DEBT),
            currency = readFsString(f, "currency", CurrencyCode.IQD),
            amount = readFsDouble(f, "amount", 0.0),
            timestamp = readFsLong(f, "timestamp", System.currentTimeMillis()),
            note = readFsString(f, "note", "")
        )
    }

    private fun parseFriendlyArabicAuthError(errorBody: String): String {
        return try {
            val code = JSONObject(errorBody)
                .optJSONObject("error")
                ?.optString("message", "")
                .orEmpty()
            when {
                code.contains("EMAIL_EXISTS") ->
                    "البريد الإلكتروني مسجل مسبقاً، يرجى اختيار تسجيل الدخول."
                code.contains("INVALID_LOGIN_CREDENTIALS") ||
                    code.contains("INVALID_PASSWORD") ||
                    code.contains("EMAIL_NOT_FOUND") ||
                    code.contains("USER_NOT_FOUND") ->
                    "البريد الإلكتروني أو كلمة المرور غير صحيحة."
                code.contains("WEAK_PASSWORD") ->
                    "كلمة المرور ضعيفة، يرجى إدخال 6 أحرف أو أرقام على الأقل."
                code.contains("INVALID_EMAIL") ->
                    "صيغة البريد الإلكتروني غير صحيحة."
                code.contains("TOO_MANY_ATTEMPTS_TRY_LATER") ->
                    "تم إجراء محاولات كثيرة، يرجى الانتظار قليلاً ثم المحاولة مرة أخرى."
                code.contains("USER_DISABLED") ->
                    "تم إيقاف هذا الحساب، يرجى التواصل مع الدعم الفني."
                code.contains("NETWORK") ->
                    "يرجى التأكد من اتصالك بالإنترنت."
                else ->
                    "يرجى التأكد من صحة البريد الإلكتروني وكلمة المرور أو اتصالك بالإنترنت."
            }
        } catch (_: Exception) {
            "يرجى التأكد من صحة البريد الإلكتروني وكلمة المرور أو اتصالك بالإنترنت."
        }
    }

    private fun executeJsonRequest(
        urlStr: String,
        method: String,
        body: String? = null,
        bearerToken: String? = null
    ): Pair<Int, String> {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 15_000
            requestMethod = if (method == "PATCH") "POST" else method
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (method == "PATCH") {
                setRequestProperty("X-HTTP-Method-Override", "PATCH")
            }
            if (!bearerToken.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            if (body != null) {
                doOutput = true
            }
        }
        return try {
            if (body != null) {
                conn.outputStream.use { os ->
                    os.write(body.toByteArray(Charsets.UTF_8))
                }
            }
            val statusCode = conn.responseCode
            val stream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            statusCode to responseText
        } finally {
            conn.disconnect()
        }
    }
}
