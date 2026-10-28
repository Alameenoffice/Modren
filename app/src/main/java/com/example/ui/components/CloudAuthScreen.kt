package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLedgerColors

enum class AuthMode {
    SIGN_UP,
    SIGN_IN
}

@Composable
fun CloudAuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onClearMessages: () -> Unit,
    onSignUpNewOffice: (
        officeName: String,
        exchangeRate: Double,
        email: String,
        password: String
    ) -> Unit,
    onSignInExistingOffice: (
        email: String,
        password: String
    ) -> Unit,
    onRequestPasswordReset: (email: String) -> Unit
) {
    val colors = LocalLedgerColors.current

    var authMode by remember { mutableStateOf(AuthMode.SIGN_UP) }
    var officeNameText by remember { mutableStateOf("") }
    var exchangeRateText by remember { mutableStateOf("1500") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        colors.background
                    )
                )
            )
            .padding(16.dp)
            .testTag("cloud_auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Brand Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                            )
                        )
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "نظام سجل الديون والمحاسبة السحابي",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مزامنة سحابية فورية (Firebase) + عمل كامل بدون إنترنت",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Mode Switch Tabs (Create New Office vs Sign In)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.elevatedSurface)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isSignUp = authMode == AuthMode.SIGN_UP
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    authMode = AuthMode.SIGN_UP
                                    localValidationError = null
                                    onClearMessages()
                                }
                                .testTag("auth_tab_signup"),
                            color = if (isSignUp) colors.primaryInteractive else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = if (isSignUp) Color.White else colors.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إنشاء حساب مكتب جديد",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSignUp) Color.White else colors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val isSignIn = authMode == AuthMode.SIGN_IN
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    authMode = AuthMode.SIGN_IN
                                    localValidationError = null
                                    onClearMessages()
                                }
                                .testTag("auth_tab_signin"),
                            color = if (isSignIn) colors.primaryInteractive else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = if (isSignIn) Color.White else colors.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تسجيل الدخول",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSignIn) Color.White else colors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign-Up Exclusive Fields: Office Name & USD Exchange Rate
                    AnimatedVisibility(visible = authMode == AuthMode.SIGN_UP) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = officeNameText,
                                onValueChange = {
                                    officeNameText = it
                                    localValidationError = null
                                    onClearMessages()
                                },
                                label = { Text("اسم المكتب أو الشركة أو التاجر *") },
                                placeholder = { Text("مثال: شركة الميزان للتجارة العامة") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = colors.primaryInteractive
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_office_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = exchangeRateText,
                                onValueChange = {
                                    exchangeRateText = it
                                    localValidationError = null
                                    onClearMessages()
                                },
                                label = { Text("سعر صرف الدولار مقابل الدينار العراقي (1$ = ? د.ع) *") },
                                placeholder = { Text("مثال: 1500") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = colors.paymentEmerald
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_exchange_rate_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = {
                            emailText = it
                            localValidationError = null
                            onClearMessages()
                        },
                        label = { Text("البريد الإلكتروني *") },
                        placeholder = { Text("example@company.com") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = colors.primaryInteractive
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = {
                            passwordText = it
                            localValidationError = null
                            onClearMessages()
                        },
                        label = { Text("كلمة المرور (6 أحرف أو أرقام فأكثر) *") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.primaryInteractive
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = "إظهار/إخفاء كلمة المرور"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Forgot Password Action (in Sign-In mode)
                    AnimatedVisibility(visible = authMode == AuthMode.SIGN_IN) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val cleanEmail = emailText.trim()
                                    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                        localValidationError = "يرجى كتابة بريدك الإلكتروني في الحقل أعلاه أولاً لإرسال رابط استعادة كلمة المرور."
                                    } else {
                                        localValidationError = null
                                        onRequestPasswordReset(cleanEmail)
                                    }
                                },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسيت كلمة المرور؟ استعادة عبر البريد")
                            }
                        }
                    }

                    // Error / Info Feedback Banners
                    val activeError = localValidationError ?: errorMessage
                    if (!activeError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.debtCrimsonSoft,
                            border = BorderStroke(1.dp, colors.debtCrimson)
                        ) {
                            Text(
                                text = activeError,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.debtCrimson,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (!infoMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.paymentEmeraldSoft,
                            border = BorderStroke(1.dp, colors.paymentEmerald)
                        ) {
                            Text(
                                text = infoMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.paymentEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            val cleanEmail = emailText.trim()
                            val cleanPass = passwordText
                            if (authMode == AuthMode.SIGN_UP) {
                                val cleanOffice = officeNameText.trim()
                                val parsedRate = exchangeRateText.replace(",", "").toDoubleOrNull()
                                if (cleanOffice.isBlank()) {
                                    localValidationError = "يرجى إدخال اسم المكتب أو الشركة أو التاجر"
                                    return@Button
                                }
                                if (parsedRate == null || parsedRate <= 0.0) {
                                    localValidationError = "يرجى إدخال سعر صرف صحيح للدولار (مثلاً 1500)"
                                    return@Button
                                }
                                if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                    localValidationError = "يرجى إدخال بريد إلكتروني صحيح"
                                    return@Button
                                }
                                if (cleanPass.length < 6) {
                                    localValidationError = "يرجى إدخال كلمة مرور مكونة من 6 أحرف أو أرقام على الأقل"
                                    return@Button
                                }
                                localValidationError = null
                                onSignUpNewOffice(cleanOffice, parsedRate, cleanEmail, cleanPass)
                            } else {
                                if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                    localValidationError = "يرجى إدخال البريد الإلكتروني المسجل"
                                    return@Button
                                }
                                if (cleanPass.isBlank()) {
                                    localValidationError = "يرجى إدخال كلمة المرور"
                                    return@Button
                                }
                                localValidationError = null
                                onSignInExistingOffice(cleanEmail, cleanPass)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (authMode == AuthMode.SIGN_UP)
                                colors.paymentEmerald
                            else
                                colors.primaryInteractive
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "جارٍ الاتصال بالسحابة ومزامنة الحساب...",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        } else {
                            Icon(
                                imageVector = if (authMode == AuthMode.SIGN_UP)
                                    Icons.Default.PersonAdd
                                else
                                    Icons.Default.Login,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (authMode == AuthMode.SIGN_UP)
                                    "إنشاء حساب مكتب جديد والبدء بسجل نظيف"
                                else
                                    "تسجيل الدخول واسترجاع بيانات المكتب",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.subtleBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "جميع بياناتك معزولة ومحمية سحابياً تحت حسابك الخاص (users/{uid}) مع حفظ تلقائي للعمل بدون إنترنت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
