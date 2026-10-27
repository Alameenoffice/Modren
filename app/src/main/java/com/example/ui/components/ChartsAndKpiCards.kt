package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyCode
import com.example.ui.MonthlyStatPoint
import com.example.ui.theme.LocalLedgerColors
import com.example.util.Formatters

@Composable
fun DualCurrencyKpiCard(
    title: String,
    iqdValue: String,
    usdValue: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    softBackgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalLedgerColors.current
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(softBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // IQD Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "د.ع:",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted
                )
                Text(
                    text = iqdValue,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = colors.subtleBorder.copy(alpha = 0.6f)
            )

            // USD Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "USD:",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted
                )
                Text(
                    text = usdValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primaryInteractive,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = softBackgroundColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FinancialKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    softBackgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalLedgerColors.current
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(softBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = softBackgroundColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DebtVsPaymentInteractiveChart(
    title: String,
    subtitle: String,
    points: List<MonthlyStatPoint>,
    modifier: Modifier = Modifier
) {
    val colors = LocalLedgerColors.current
    var chartCurrency by remember { mutableStateOf(CurrencyCode.IQD) }
    var selectedIdx by remember(points, chartCurrency) {
        mutableIntStateOf(
            points.indexOfLast {
                if (chartCurrency == CurrencyCode.USD) (it.totalDebtUsd > 0 || it.totalPaidUsd > 0)
                else (it.totalDebtIqd > 0 || it.totalPaidIqd > 0)
            }.coerceAtLeast(0)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                // Currency Switcher (IQD | USD)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = chartCurrency == CurrencyCode.IQD,
                        onClick = { chartCurrency = CurrencyCode.IQD },
                        label = { Text("د.ع IQD") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryInteractive,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = chartCurrency == CurrencyCode.USD,
                        onClick = { chartCurrency = CurrencyCode.USD },
                        label = { Text("$ USD") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryInteractive,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val activePoint = points.getOrNull(selectedIdx)
            if (activePoint != null) {
                val activeDebt = if (chartCurrency == CurrencyCode.USD) activePoint.totalDebtUsd else activePoint.totalDebtIqd
                val activePaid = if (chartCurrency == CurrencyCode.USD) activePoint.totalPaidUsd else activePoint.totalPaidIqd
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.elevatedSurface,
                    border = BorderStroke(1.dp, colors.subtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شهر ${activePoint.monthName}:",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "ديون: ${Formatters.formatByCurrency(activeDebt, chartCurrency)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.debtCrimson
                        )
                        Text(
                            text = "تحصيل: ${Formatters.formatByCurrency(activePaid, chartCurrency)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.paymentEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxVal = points.maxOfOrNull {
                if (chartCurrency == CurrencyCode.USD) maxOf(it.totalDebtUsd, it.totalPaidUsd)
                else maxOf(it.totalDebtIqd, it.totalPaidIqd)
            }?.coerceAtLeast(100.0) ?: 100.0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points, chartCurrency) {
                            detectTapGestures { offset ->
                                if (points.isNotEmpty() && size.width > 0) {
                                    val slotW = size.width.toFloat() / points.size
                                    val tapped = (offset.x / slotW).toInt().coerceIn(0, points.lastIndex)
                                    selectedIdx = tapped
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val count = points.size.coerceAtLeast(1)
                    val slotWidth = w / count
                    val barWidth = (slotWidth * 0.30f).coerceAtMost(24f)

                    for (i in 0..3) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = colors.subtleBorder,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    points.forEachIndexed { idx, pt ->
                        val centerX = idx * slotWidth + (slotWidth / 2f)
                        if (idx == selectedIdx) {
                            drawRoundRect(
                                color = colors.primaryInteractive.copy(alpha = 0.08f),
                                topLeft = Offset(idx * slotWidth + 2f, 0f),
                                size = Size(slotWidth - 4f, h),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }

                        val debtVal = if (chartCurrency == CurrencyCode.USD) pt.totalDebtUsd else pt.totalDebtIqd
                        val paidVal = if (chartCurrency == CurrencyCode.USD) pt.totalPaidUsd else pt.totalPaidIqd

                        val debtH = ((debtVal / maxVal).toFloat() * (h * 0.88f)).coerceAtLeast(4f)
                        val paidH = ((paidVal / maxVal).toFloat() * (h * 0.88f)).coerceAtLeast(4f)

                        drawRoundRect(
                            color = colors.debtCrimson,
                            topLeft = Offset(centerX - barWidth - 2f, h - debtH),
                            size = Size(barWidth, debtH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = colors.paymentEmerald,
                            topLeft = Offset(centerX + 2f, h - paidH),
                            size = Size(barWidth, paidH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { idx, pt ->
                    Text(
                        text = pt.monthName.take(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (idx == selectedIdx) colors.primaryInteractive else colors.textMuted,
                        fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.clickable { selectedIdx = idx }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionGrowthCurveCard(
    title: String,
    subtitle: String,
    points: List<MonthlyStatPoint>,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLedgerColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.subtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedCurrency == CurrencyCode.IQD,
                        onClick = { onCurrencyChange(CurrencyCode.IQD) },
                        label = { Text("د.ع IQD") }
                    )
                    FilterChip(
                        selected = selectedCurrency == CurrencyCode.USD,
                        onClick = { onCurrencyChange(CurrencyCode.USD) },
                        label = { Text("$ USD") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val maxPaid = points.maxOfOrNull {
                if (selectedCurrency == CurrencyCode.USD) it.totalPaidUsd else it.totalPaidIqd
            }?.coerceAtLeast(100.0) ?: 100.0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (points.size < 2) return@Canvas

                    val stepX = w / (points.size - 1).coerceAtLeast(1)
                    val linePath = Path()
                    val fillPath = Path()

                    val coords = points.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        val v = if (selectedCurrency == CurrencyCode.USD) pt.totalPaidUsd else pt.totalPaidIqd
                        val normalized = (v / maxPaid).toFloat().coerceIn(0f, 1f)
                        val y = h - (normalized * (h * 0.82f)) - 10f
                        Offset(x, y)
                    }

                    coords.forEachIndexed { idx, offset ->
                        if (idx == 0) {
                            linePath.moveTo(offset.x, offset.y)
                            fillPath.moveTo(offset.x, h)
                            fillPath.lineTo(offset.x, offset.y)
                        } else {
                            linePath.lineTo(offset.x, offset.y)
                            fillPath.lineTo(offset.x, offset.y)
                        }
                    }
                    fillPath.lineTo(coords.last().x, h)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.paymentEmerald.copy(alpha = 0.30f),
                                colors.paymentEmerald.copy(alpha = 0.02f)
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = colors.paymentEmerald,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    coords.forEach { pt ->
                        drawCircle(
                            color = colors.cardSurface,
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = colors.paymentEmerald,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}
