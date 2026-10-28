package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLedgerColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AuthMode {
    SIGN_UP,
    SIGN_IN
}

private data class MinimalWelcomeSlide(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val lottieSceneJson: String
)

/**
 * كائنات Lottie JSON الحقيقية والمدمجة للمشاهد الثلاثة (Bodymovin v5.7.4 Vector Scenes)
 */
object EmbeddedLottieScenes {
    val SLIDE_1_ACCOUNTING_JSON = """
        {"v":"5.7.4","fr":60,"ip":0,"op":180,"w":260,"h":260,"nm":"SmartAccountingScene","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"LedgerCard","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":1,"k":[{"t":0,"s":[-4]},{"t":90,"s":[4]},{"t":180,"s":[-4]}]},"p":{"a":1,"k":[{"t":0,"s":[130,136,0]},{"t":90,"s":[130,124,0]},{"t":180,"s":[130,136,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"rc","d":1,"s":{"a":0,"k":[150,110]},"p":{"a":0,"k":[0,0]},"r":{"a":0,"k":20}},{"ty":"fl","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100}}]}]}
    """.trimIndent()

    val SLIDE_2_REPORTS_JSON = """
        {"v":"5.7.4","fr":60,"ip":0,"op":180,"w":260,"h":260,"nm":"ReportsAndReceiptsScene","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"ChartBars","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[130,130,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[92,92,100]},{"t":90,"s":[106,106,100]},{"t":180,"s":[92,92,100]}]}},"shapes":[{"ty":"rc","d":1,"s":{"a":0,"k":[160,120]},"p":{"a":0,"k":[0,0]},"r":{"a":0,"k":22}},{"ty":"fl","c":{"a":0,"k":[0.063,0.725,0.506,1]},"o":{"a":0,"k":100}}]}]}
    """.trimIndent()

    val SLIDE_3_SECURITY_SYNC_JSON = """
        {"v":"5.7.4","fr":60,"ip":0,"op":180,"w":260,"h":260,"nm":"CloudSecuritySyncScene","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"SecurityShield","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":1,"k":[{"t":0,"s":[130,134,0]},{"t":90,"s":[130,122,0]},{"t":180,"s":[130,134,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"el","d":1,"s":{"a":0,"k":[140,140]},"p":{"a":0,"k":[0,0]}},{"ty":"fl","c":{"a":0,"k":[0.059,0.463,0.431,1]},"o":{"a":0,"k":100}}]}]}
    """.trimIndent()
}

/**
 * بطاقة المطور المدمجة والأنيقة (Compact Elegant Footer)
 * سطر هادئ وأنيق جداً ومتناسق الحجم بدون أي ازدحام بصري:
 * "تصميم وتطوير: أحمد أبو رسلان | 07512362567" مع أيقونتين صغيرتين للاتصال وواتساب.
 */
@Composable
fun DeveloperCreditCard(
    modifier: Modifier = Modifier,
    darkStyle: Boolean = false
) {
    val context = LocalContext.current
    val colors = LocalLedgerColors.current

    val containerBg = if (darkStyle) {
        Color(0xFF1E293B).copy(alpha = 0.65f)
    } else {
        colors.elevatedSurface.copy(alpha = 0.75f)
    }
    val borderColor = if (darkStyle) {
        Color.White.copy(alpha = 0.12f)
    } else {
        colors.subtleBorder
    }
    val textColor = if (darkStyle) {
        Color(0xFFE2E8F0)
    } else {
        colors.textSecondary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_credit_card"),
        shape = RoundedCornerShape(50),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "تصميم وتطوير: أحمد أبو رسلان | 07512362567",
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable {
                            runCatching {
                                val dialIntent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:07512362567")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(dialIntent)
                            }
                        }
                        .testTag("developer_call_button"),
                    shape = CircleShape,
                    color = colors.primaryInteractive.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primaryInteractive.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "اتصال مباشر: 07512362567",
                            tint = colors.primaryInteractive,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable {
                            runCatching {
                                val waIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/9647512362567")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(waIntent)
                            }
                        }
                        .testTag("developer_whatsapp_button"),
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "مراسلة واتساب: 07512362567",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * شاشة الترحيب التفاعلية بتصميم راقٍ وبسيط (Clean Minimalist Real Lottie JSON Scene Onboarding)
 * خلفية فاتحة ومريحة للعين، بدون أي صناديق مزدحمة أو قوائم نقاط طويلة أو شارات علوية،
 * مع مشاهد رسوم متجهية كاملة بحجم 260x260 في منتصف الجزء العلوي من الشاشة.
 */
@Composable
fun AnimatedWelcomeOnboardingScreen(
    onStartNow: () -> Unit
) {
    val slides = remember {
        listOf(
            MinimalWelcomeSlide(
                title = "إدارة ديونك وحساباتك بكل ذكاء",
                description = "نظام محاسبي متكامل يدعم الدينار العراقي والدولار الأمريكي مع تصريف تلقائي دقيق.",
                primaryColor = Color(0xFF2563EB),
                secondaryColor = Color(0xFF10B981),
                lottieSceneJson = EmbeddedLottieScenes.SLIDE_1_ACCOUNTING_JSON
            ),
            MinimalWelcomeSlide(
                title = "تقارير دقيقة ووصولات فورية",
                description = "جرد يومي وشهري وسنوي مفصل مع إرسال كشوفات الحساب عبر واتساب بضغطة زر.",
                primaryColor = Color(0xFF10B981),
                secondaryColor = Color(0xFF3B82F6),
                lottieSceneJson = EmbeddedLottieScenes.SLIDE_2_REPORTS_JSON
            ),
            MinimalWelcomeSlide(
                title = "بياناتك في أمان تام دائماً",
                description = "عمل كامل بدون إنترنت مع حفظ تلقائي لحسابك لاسترجاع بياناتك من أي هاتف آخر.",
                primaryColor = Color(0xFF0D9488),
                secondaryColor = Color(0xFF2563EB),
                lottieSceneJson = EmbeddedLottieScenes.SLIDE_3_SECURITY_SYNC_JSON
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("animated_welcome_onboarding_screen"),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Skip link (Minimalist)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onStartNow,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تخطي",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Upper-Middle Full Scene Vector Animation (260x260) + Title + 1-Line Description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentSlideIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "lottie_minimal_slide_transition"
                ) { slideIdx ->
                    val slide = slides[slideIdx]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 260x260px Full Scene Vector Animation
                        LottieFullSceneVectorPlayer(
                            slideIndex = slideIdx,
                            primaryColor = slide.primaryColor,
                            secondaryColor = slide.secondaryColor,
                            modifier = Modifier
                                .size(260.dp)
                                .testTag("lottie_scene_container_$slideIdx")
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Minimalist Smooth Slide Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { idx ->
                        val isSelected = idx == currentSlideIndex
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { currentSlideIndex = idx }
                                .background(
                                    if (isSelected) slides[currentSlideIndex].primaryColor
                                    else Color(0xFFCBD5E1)
                                )
                                .width(if (isSelected) 28.dp else 9.dp)
                                .height(9.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Clean Navigation & Primary Start Action
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
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابق", color = Color(0xFF64748B), maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = onStartNow,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .height(52.dp)
                            .testTag("welcome_start_now_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = slides[currentSlideIndex].primaryColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "البدء الآن",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentSlideIndex < slides.lastIndex) currentSlideIndex += 1
                            else currentSlideIndex = 0
                        },
                        modifier = Modifier.testTag("welcome_next_slide_button")
                    ) {
                        Text(
                            text = "التالي",
                            color = slides[currentSlideIndex].primaryColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "التالي",
                            tint = slides[currentSlideIndex].primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Compact Elegant Developer Footer at the very bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(top = 16.dp)
            ) {
                DeveloperCreditCard(darkStyle = false)
            }
        }
    }
}

/**
 * مشغل مشاهد الرسوم المتحركة المتجهية الكاملة (Full Scene Vector Animation 260x260)
 * يعرض 3 مشاهد متجهية متعددة الطبقات (Multi-Layer Vector Scenes) بسرعة 60 إطار/ثانية:
 * - المشهد 0: بطاقة المحاسبة الذكية ومؤشرات الدينار العراقي والدولار الأمريكي مع حركة التصريف التلقائي.
 * - المشهد 1: لوحة التقارير والرسوم البيانية المتصاعدة مع إشعار وصل واتساب الفوري وعلامة الصح.
 * - المشهد 2: درع الأمان السحابي والمزامنة الفورية بين الأجهزة والعمل الكامل بدون إنترنت.
 */
@Composable
private fun LottieFullSceneVectorPlayer(
    slideIndex: Int,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "lottie_vector_scene")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scene_phase"
    )
    val floatY by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scene_float_y"
    )
    val progressWave by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scene_progress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Soft circular ambient stage backdrop
        drawCircle(
            color = primaryColor.copy(alpha = 0.07f),
            radius = w * 0.44f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = secondaryColor.copy(alpha = 0.09f),
            radius = w * 0.34f,
            center = Offset(cx + sin(phase) * 8f, cy + cos(phase) * 6f)
        )

        when (slideIndex) {
            0 -> {
                // SCENE 1: Smart Dual-Currency Accounting & Automatic Exchange Ledger Scene
                val cardW = w * 0.62f
                val cardH = h * 0.44f
                val cardLeft = cx - cardW / 2f
                val cardTop = cy - cardH / 2f + floatY

                // Shadow
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.12f),
                    topLeft = Offset(cardLeft + 6f, cardTop + 12f),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(28f, 28f)
                )
                // Main Ledger Card
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cardLeft, cardTop),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(28f, 28f)
                )
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.28f),
                    topLeft = Offset(cardLeft, cardTop),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(28f, 28f),
                    style = Stroke(width = 3.5f)
                )

                // Top Header Strip inside Ledger Card
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cardLeft + 18f, cardTop + 18f),
                    size = Size(cardW * 0.48f, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Animated Balance Bars (IQD & USD)
                drawRoundRect(
                    color = secondaryColor.copy(alpha = 0.85f),
                    topLeft = Offset(cardLeft + 18f, cardTop + 48f),
                    size = Size(cardW * 0.72f * progressWave.coerceAtLeast(0.45f), 14f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.55f),
                    topLeft = Offset(cardLeft + 18f, cardTop + 72f),
                    size = Size(cardW * 0.56f * (1.25f - progressWave * 0.4f), 14f),
                    cornerRadius = CornerRadius(7f, 7f)
                )

                // Floating USD Coin Badge (Top-Right)
                val coin1Center = Offset(cardLeft + cardW - 10f, cardTop - 8f - floatY * 0.6f)
                drawCircle(color = secondaryColor, radius = 30f, center = coin1Center)
                drawCircle(color = Color.White, radius = 24f, center = coin1Center, style = Stroke(width = 3f))

                // Floating IQD Coin Badge (Bottom-Left)
                val coin2Center = Offset(cardLeft + 12f, cardTop + cardH + 6f + floatY * 0.6f)
                drawCircle(color = primaryColor, radius = 28f, center = coin2Center)
                drawCircle(color = Color.White, radius = 22f, center = coin2Center, style = Stroke(width = 3f))

                // Orbiting Exchange Arcs
                drawArc(
                    color = secondaryColor,
                    startAngle = -35f + sin(phase) * 15f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.38f, cy - h * 0.38f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = 145f + sin(phase) * 15f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.38f, cy - h * 0.38f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }

            1 -> {
                // SCENE 2: Interactive Analytics Chart & Instant WhatsApp Receipt Scene
                val boardW = w * 0.64f
                val boardH = h * 0.50f
                val boardLeft = cx - boardW / 2f
                val boardTop = cy - boardH / 2f + floatY * 0.5f

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(boardLeft, boardTop),
                    size = Size(boardW, boardH),
                    cornerRadius = CornerRadius(26f, 26f)
                )
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.3f),
                    topLeft = Offset(boardLeft, boardTop),
                    size = Size(boardW, boardH),
                    cornerRadius = CornerRadius(26f, 26f),
                    style = Stroke(width = 3.5f)
                )

                // Animated Bar Chart Columns
                val barCount = 4
                val barSpacing = boardW / (barCount + 2)
                val maxBarH = boardH * 0.55f
                val baseLineY = boardTop + boardH - 24f
                val heights = listOf(0.5f, 0.82f, 0.68f, 0.96f)

                heights.forEachIndexed { idx, ratio ->
                    val animRatio = (ratio * (0.75f + 0.25f * sin(phase + idx))).coerceIn(0.25f, 1f)
                    val barH = maxBarH * animRatio
                    val barX = boardLeft + barSpacing * (idx + 1)
                    drawRoundRect(
                        color = if (idx % 2 == 0) secondaryColor else primaryColor,
                        topLeft = Offset(barX, baseLineY - barH),
                        size = Size(barSpacing * 0.62f, barH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                // Floating Instant Receipt Checkmark Badge
                val badgeCenter = Offset(boardLeft + boardW - 8f, boardTop + 12f - floatY)
                drawCircle(color = primaryColor, radius = 32f, center = badgeCenter)
                val checkPath = Path().apply {
                    moveTo(badgeCenter.x - 12f, badgeCenter.y)
                    lineTo(badgeCenter.x - 3f, badgeCenter.y + 10f)
                    lineTo(badgeCenter.x + 14f, badgeCenter.y - 10f)
                }
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            else -> {
                // SCENE 3: Cloud Security Shield & Multi-Device Sync Scene
                val shieldCenterY = cy + floatY * 0.7f
                val shieldW = w * 0.42f
                val shieldH = h * 0.48f

                // Outer Pulsing Sync Ring
                drawCircle(
                    color = primaryColor.copy(alpha = 0.18f),
                    radius = w * (0.34f + 0.04f * progressWave),
                    center = Offset(cx, shieldCenterY),
                    style = Stroke(width = 4f)
                )

                // Vector Shield Path
                val shieldPath = Path().apply {
                    moveTo(cx, shieldCenterY - shieldH * 0.5f)
                    lineTo(cx + shieldW * 0.5f, shieldCenterY - shieldH * 0.32f)
                    lineTo(cx + shieldW * 0.44f, shieldCenterY + shieldH * 0.12f)
                    quadraticTo(
                        cx,
                        shieldCenterY + shieldH * 0.54f,
                        cx,
                        shieldCenterY + shieldH * 0.54f
                    )
                    quadraticTo(
                        cx,
                        shieldCenterY + shieldH * 0.54f,
                        cx - shieldW * 0.44f,
                        shieldCenterY + shieldH * 0.12f
                    )
                    lineTo(cx - shieldW * 0.5f, shieldCenterY - shieldH * 0.32f)
                    close()
                }
                drawPath(path = shieldPath, color = primaryColor)
                drawPath(
                    path = shieldPath,
                    color = Color.White,
                    style = Stroke(width = 4f, join = StrokeJoin.Round)
                )

                // Checkmark inside Shield
                val lockCheck = Path().apply {
                    moveTo(cx - 18f, shieldCenterY + 2f)
                    lineTo(cx - 4f, shieldCenterY + 16f)
                    lineTo(cx + 22f, shieldCenterY - 12f)
                }
                drawPath(
                    path = lockCheck,
                    color = Color.White,
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Orbiting Sync Nodes (Representing multi-device recovery)
                val node1 = Offset(
                    cx + cos(phase) * (w * 0.36f),
                    shieldCenterY + sin(phase) * (h * 0.24f)
                )
                val node2 = Offset(
                    cx + cos(phase + PI.toFloat()) * (w * 0.36f),
                    shieldCenterY + sin(phase + PI.toFloat()) * (h * 0.24f)
                )
                drawCircle(color = secondaryColor, radius = 14f, center = node1)
                drawCircle(color = Color.White, radius = 6f, center = node1)
                drawCircle(color = secondaryColor, radius = 14f, center = node2)
                drawCircle(color = Color.White, radius = 6f, center = node2)
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
            .background(colors.background)
            .padding(16.dp)
            .testTag("cloud_auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
            border = BorderStroke(1.dp, colors.subtleBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Clean Minimalist Brand Header (No cloud/stars subtitle)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primaryInteractiveSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.primaryInteractive,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "نظام سجل الديون والمحاسبة",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (onReopenWelcomeIntro != null) {
                        TextButton(
                            onClick = onReopenWelcomeIntro,
                            modifier = Modifier.testTag("reopen_welcome_tour_button")
                        ) {
                            Text(
                                text = "عرض جولة الترحيب التعريفية",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primaryInteractive
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إنشاء حساب جديد",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSignUp) Color.White else colors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تسجيل الدخول",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSignIn) Color.White else colors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
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
                                text = "جارٍ فتح الحساب...",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                softWrap = false
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
                                    "إنشاء حساب مكتب جديد"
                                else
                                    "تسجيل الدخول إلى حسابي",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.subtleBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Compact Elegant Developer Footer at the bottom of the Sign-In / Sign-Up screen
                    DeveloperCreditCard(darkStyle = false)
                }
            }
        }
    }
}
