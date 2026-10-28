package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLedgerColors
import kotlin.math.roundToInt

enum class AuthMode {
    SIGN_UP,
    SIGN_IN
}

private data class WelcomeSlideData(
    val badge: String,
    val title: String,
    val description: String,
    val highlights: List<String>,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * بطاقة تعريف المطور والتواصل المباشر (أحمد أبو رسلان - 07512362567)
 * تظهر في الشاشة الترحيبية، أسفل شاشة الدخول، وداخل شاشة الإعدادات.
 */
@Composable
fun DeveloperCreditCard(
    modifier: Modifier = Modifier,
    darkStyle: Boolean = false
) {
    val context = LocalContext.current
    val colors = LocalLedgerColors.current

    val containerBg = if (darkStyle) {
        Color(0xFF0F172A)
    } else {
        colors.elevatedSurface
    }
    val borderColor = if (darkStyle) {
        Color(0xFF10B981).copy(alpha = 0.55f)
    } else {
        colors.paymentEmerald.copy(alpha = 0.5f)
    }
    val primaryTextColor = if (darkStyle) Color.White else colors.textPrimary
    val secondaryTextColor = if (darkStyle) Color(0xFFCBD5E1) else colors.textSecondary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_credit_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تم التصميم والتطوير بواسطة: أحمد أبو رسلان",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "للتواصل المباشر والدعم الفني والاستفسارات",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (darkStyle) Color.White.copy(alpha = 0.08f) else colors.cardSurface,
                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "رقم الهاتف والدعم الفني: 07512362567",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                runCatching {
                                    val dialIntent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:07512362567")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(dialIntent)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("developer_call_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryInteractive
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "اتصال هاتفي مباشر",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اتصال مباشر",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                runCatching {
                                    val waIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/9647512362567")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(waIntent)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("developer_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "مراسلة واتساب مباشرة",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "واتساب مباشر",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * شاشة ترحيبية متحركة وتفاعلية (Interactive Animated Welcome Onboarding)
 * تعمل 100% بدون إنترنت بتأثيرات حركية ناعمة وتنتقل بسلاسة إلى شاشة الدخول / إنشاء الحساب.
 */
@Composable
fun AnimatedWelcomeOnboardingScreen(
    onStartNow: () -> Unit
) {
    val slides = remember {
        listOf(
            WelcomeSlideData(
                badge = "مرحباً بك في النظام المتكامل",
                title = "تطبيق سجل الديون والمحاسبة الاحترافي",
                description = "نظام محاسبي متكامل مصمم خصيصاً لأصحاب المكاتب والشركات والتجار لإدارة الحسابات والذمم المالية بأعلى درجات الدقة والوضوح.",
                highlights = listOf(
                    "حفظ سحابي آمن ومزامنة تلقائية + عمل كامل بدون إنترنت",
                    "واجهة عربية أنيقة وسريعة تدعم الوضع الليلي والنهاري",
                    "ترويسة رسمية باسم مكتبك أو شركتك في جميع التقارير والوصولات"
                ),
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = Color(0xFF10B981)
            ),
            WelcomeSlideData(
                badge = "إدارة مالية مزدوجة العملة",
                title = "إدارة الديون والتسديدات بالدينار العراقي والدولار الأمريكي",
                description = "سجّل الديون والدفعات المستلمة بالدينار العراقي (د.ع) أو الدولار الأمريكي ($) بشكل مستقل أو مع تصريف تلقائي فوري حسب سعر الصرف.",
                highlights = listOf(
                    "فصل تام ودقيق لأرصدة الدينار العراقي وأرصدة الدولار الأمريكي",
                    "إمكانية استلام دفعة بعملة وتصريفها تلقائياً لتسديد العملة الأخرى",
                    "إصدار وصولات قبض رسمية وسندات براءة ذمة ومشاركتها عبر واتساب"
                ),
                icon = Icons.Default.CurrencyExchange,
                accentColor = Color(0xFF3B82F6)
            ),
            WelcomeSlideData(
                badge = "جرد شامل وأمان تام للحسابات",
                title = "الجرد اليومي والشهري والسنوي وفتح حسابك من أي هاتف",
                description = "تابع حركة مكتبك المالي بتقارير يومية وشهرية وسنوية مفصلة، مع حفظ آمن لحساباتك يتيح لك فتحها واسترجاعها من أي هاتف آخر في أي وقت.",
                highlights = listOf(
                    "جرد يومي وشهري وسنوي دقيق مع تصدير ملف إكسل (Excel) وطباعة التقارير",
                    "حفظ الحسابات بأمان تام مع إمكانية فتحها من أي هاتف آخر ببريدك وكلمة مرورك",
                    "نسخ احتياطي شامل على الهاتف وحماية إضافية برمز قفل شخصي"
                ),
                icon = Icons.Default.Assessment,
                accentColor = Color(0xFFF59E0B)
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_ infinite_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset_y"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090E1A),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .testTag("animated_welcome_onboarding_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Offline Animated Decorative Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = glowAlpha),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
                color = Color(0xFF2563EB).copy(alpha = glowAlpha * 0.85f),
                radius = size.minDimension * 0.48f,
                center = Offset(size.width * 0.15f, size.height * 0.82f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111C33).copy(alpha = 0.96f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Animated Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF10B981).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حفظ سحابي آمن ومزامنة تلقائية + عمل كامل بدون إنترنت",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6EE7B7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Animated Slide Card
                AnimatedContent(
                    targetState = currentSlideIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(320)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "welcome_slide_transition"
                ) { slideIdx ->
                    val slide = slides[slideIdx]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(0, floatOffsetY.roundToInt()) }
                                .scale(pulseScale)
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            slide.accentColor.copy(alpha = 0.28f),
                                            slide.accentColor.copy(alpha = 0.10f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.accentColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.badge,
                            style = MaterialTheme.typography.labelLarge,
                            color = slide.accentColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                slide.highlights.forEach { point ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = slide.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFF1F5F9),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Slide Indicators & Next/Prev Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (currentSlideIndex > 0) currentSlideIndex -= 1
                            else currentSlideIndex = slides.lastIndex
                        },
                        modifier = Modifier.testTag("welcome_prev_slide_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "السابق",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابق", color = Color(0xFF94A3B8))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        slides.indices.forEach { idx ->
                            val isSelected = idx == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { currentSlideIndex = idx }
                                    .background(
                                        if (isSelected) Color(0xFF10B981)
                                        else Color.White.copy(alpha = 0.25f)
                                    )
                                    .width(if (isSelected) 26.dp else 9.dp)
                                    .height(9.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            if (currentSlideIndex < slides.lastIndex) currentSlideIndex += 1
                            else currentSlideIndex = 0
                        },
                        modifier = Modifier.testTag("welcome_next_slide_button")
                    ) {
                        Text("التالي", color = Color(0xFF6EE7B7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "التالي",
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Developer Credit Card inside the Welcome Screen
                DeveloperCreditCard(darkStyle = true)

                Spacer(modifier = Modifier.height(16.dp))

                // Primary "البدء الآن" Action Button
                Button(
                    onClick = onStartNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("welcome_start_now_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "البدء الآن",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
    onRequestPasswordReset: (email: String) -> Unit,
    onReopenWelcomeIntro: (() -> Unit)? = null
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
                // Top Brand Header (Zero Technical Jargon)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                            )
                        )
                        .padding(vertical = 22.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
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
                            text = "تطبيق سجل الديون والمحاسبة الاحترافي",
                            style = MaterialTheme.typography.headlineSmall,
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
                                text = "حفظ سحابي آمن ومزامنة تلقائية + عمل كامل بدون إنترنت",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (onReopenWelcomeIntro != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = onReopenWelcomeIntro,
                                modifier = Modifier.testTag("reopen_welcome_tour_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "عرض البطاقة التعريفية ومميزات التطبيق",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF6EE7B7)
                                )
                            }
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
                                text = "جارٍ فتح الحساب ومزامنة السجل...",
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
                        text = "جميع حساباتك وسجلاتك محفوظة بأمان تام وخاصة بمكتبك فقط، ويمكنك فتحها من أي هاتف آخر في أي وقت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Developer Credit Card at the bottom of the Sign-In / Sign-Up screen
                    DeveloperCreditCard(darkStyle = false)
                }
            }
        }
    }
}
