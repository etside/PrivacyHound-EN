package com.privacyhound.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.data.AppUsageCount
import com.privacyhound.android.data.DailyCount
import com.privacyhound.android.data.UsageSummary
import com.privacyhound.android.monitor.HardwareOp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ── Sensor colours ────────────────────────────────────────────────

private val SensorColors = mapOf(
    HardwareOp.TYPE_CAMERA to Color(0xFFE53935),
    HardwareOp.TYPE_MIC to Color(0xFFFB8C00),
    HardwareOp.TYPE_GPS to Color(0xFF43A047),
    HardwareOp.TYPE_CONTACTS_READ to Color(0xFF1E88E5),
    HardwareOp.TYPE_SMS_READ to Color(0xFF8E24AA)
)

private fun sensorLabelFromType(type: Int): String = when (type) {
    HardwareOp.TYPE_CAMERA -> "Camera"
    HardwareOp.TYPE_MIC -> "Mic"
    HardwareOp.TYPE_GPS -> "Location"
    HardwareOp.TYPE_CONTACTS_READ -> "Contacts"
    HardwareOp.TYPE_SMS_READ -> "SMS"
    else -> "Other"
}

// ── Screen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as XiaotianquanApp).repository

    val usageByApp by repo.observeUsageByApp().collectAsStateWithLifecycle(emptyList())
    val dailyStats by repo.observeDailyStats().collectAsStateWithLifecycle(emptyList())

    val summaryState = remember { mutableStateOf<UsageSummary?>(null) }
    LaunchedEffect(Unit) {
        summaryState.value = repo.getUsageSummary()
    }
    val summarySnap = summaryState.value

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.nav_dashboard)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (summarySnap == null && usageByApp.isEmpty()) {
            // Loading / empty state
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.stats_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                // ── 1. Top Offenders ──────────────────────────────
                item {
                    TopOffendersCard(usageByApp)
                }

                // ── 2. Today's Activity ───────────────────────────
                item {
                    TodayActivityCard(summarySnap)
                }

                // ── 3. Sensor Breakdown ───────────────────────────
                item {
                    SensorBreakdownCard(summarySnap)
                }

                // ── 4. Weekly Trend ───────────────────────────────
                item {
                    WeeklyTrendCard(dailyStats)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Section 1 — Top Offenders
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TopOffendersCard(usageByApp: List<AppUsageCount>) {
    // Flatten to per-app totals, then take top 5
    val topApps = remember(usageByApp) {
        usageByApp
            .groupBy { it.appName }
            .map { (name, rows) ->
                val totalCount = rows.sumOf { it.count }
                val sensors = rows.map { it.opType }.distinct()
                Triple(name, totalCount, sensors)
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    StatsCard(title = stringResource(R.string.stats_top_offenders)) {
        if (topApps.isEmpty()) {
            Text(
                text = stringResource(R.string.stats_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            topApps.forEachIndexed { idx, (name, count, sensors) ->
                if (idx > 0) {
                    Spacer(Modifier.height(10.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.stats_sensors_used,
                                sensors.joinToString(", ") { sensorLabelFromType(it) }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.stats_times_format, count),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Section 2 — Today's Activity
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TodayActivityCard(summary: UsageSummary?) {
    val today = summary?.todayCount ?: 0
    val yesterday = summary?.yesterdayCount ?: 0
    val diff = today - yesterday

    StatsCard(title = stringResource(R.string.stats_today_vs_yesterday)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.stats_today_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$today",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.stats_events_format, today),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Divider line
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            )

            // Yesterday
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.stats_yesterday_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$yesterday",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.stats_events_format, yesterday),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        if (summary != null) {
            Spacer(Modifier.height(10.dp))
            val changeText = when {
                diff > 0 -> "+$diff vs yesterday"
                diff < 0 -> "${diff} vs yesterday"
                else -> "Same as yesterday"
            }
            val changeColor = when {
                diff > 0 -> Color(0xFFE53935)
                diff < 0 -> Color(0xFF43A047)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
            Text(
                text = changeText,
                style = MaterialTheme.typography.labelSmall,
                color = changeColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Section 3 — Sensor Breakdown (pie chart via Canvas)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SensorBreakdownCard(summary: UsageSummary?) {
    val bySensor = summary?.bySensor ?: emptyMap()
    val total = bySensor.values.sum().coerceAtLeast(1)

    StatsCard(title = stringResource(R.string.stats_sensor_breakdown)) {
        if (bySensor.isEmpty()) {
            Text(
                text = stringResource(R.string.stats_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            return@StatsCard
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pie chart
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                bySensor.forEach { (type, count) ->
                    val sweep = (count.toFloat() / total) * 360f
                    drawArc(
                        color = SensorColors[type] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweep
                }
            }

            Spacer(Modifier.width(20.dp))

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                bySensor.entries
                    .sortedByDescending { it.value }
                    .forEach { (type, count) ->
                        val pct = (count * 100f / total).toInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SensorColors[type] ?: Color.Gray)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${sensorLabelFromType(type)}  $pct%",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Section 4 — Weekly Trend (bar chart via Canvas)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WeeklyTrendCard(dailyStats: List<DailyCount>) {
    val dayLabels = remember {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    // Build a map of day-of-week index -> count for the last 7 days
    val chartData = remember(dailyStats) {
        val cal = Calendar.getInstance()
        val result = IntArray(7) // index 0 = 6 days ago ... 6 = today

        // Map epoch day to result index
        val nowEpochDay = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        dailyStats.forEach { dc ->
            val offset = (nowEpochDay - dc.dayEpoch).toInt()
            if (offset in 0..6) {
                result[6 - offset] = dc.count
            }
        }
        result
    }

    val maxCount = chartData.max().coerceAtLeast(1)

    StatsCard(title = stringResource(R.string.stats_weekly_trend)) {
        val barColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val leftPad = 4.dp.toPx()
            val barAreaHeight = size.height - 24.dp.toPx() // leave room for labels
            val totalBars = 7
            val barWidth = (size.width - leftPad * 2) / (totalBars * 2f)
            val gap = barWidth

            // Horizontal grid lines
            for (i in 0..3) {
                val y = barAreaHeight * (1f - i / 3f)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            chartData.forEachIndexed { idx, value ->
                val x = leftPad + idx * (barWidth + gap)
                val barH = if (maxCount > 0) (value.toFloat() / maxCount) * barAreaHeight else 0f
                val top = barAreaHeight - barH

                // Bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barH.coerceAtLeast(2.dp.toPx())),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

            }
        }

        // Day labels below chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.stats_total_format, chartData.sum()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// Reusable card wrapper
// ══════════════════════════════════════════════════════════════════

@Composable
private fun StatsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
