package com.privacyhound.android.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.data.PrivacyEvent
import com.privacyhound.android.monitor.HardwareOp
import com.privacyhound.android.service.MonitorService
import com.privacyhound.android.ui.components.GoldDivider
import com.privacyhound.android.ui.components.PremiumButton
import com.privacyhound.android.ui.components.PremiumCard
import com.privacyhound.android.ui.theme.GoldDark
import com.privacyhound.android.ui.theme.GoldLight
import com.privacyhound.android.ui.theme.GoldPrimary
import com.privacyhound.android.ui.theme.GoldSubtle
import com.privacyhound.android.ui.theme.PitchBlack
import com.privacyhound.android.ui.theme.SurfaceCard
import com.privacyhound.android.ui.theme.TextAmber
import com.privacyhound.android.ui.theme.TextMuted
import com.privacyhound.android.ui.theme.TextWhite
import com.privacyhound.android.util.LicenseManager
import com.privacyhound.android.util.PermissionUtils
import com.privacyhound.android.util.ServiceRunning
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class SensorTileSpec(
    val opType: Int,
    val titleRes: Int,
    val icon: @Composable (Modifier, Color) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenHistory: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as XiaotianquanApp
    val repo = app.repository

    val active by repo.observeActive().collectAsStateWithLifecycle(emptyList())
    val recent by repo.observeRecent(5).collectAsStateWithLifecycle(emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var running by remember {
        mutableStateOf(ServiceRunning.isRunning(context, MonitorService::class.java))
    }

    val isPremium = LicenseManager.isActive(context)
    val premiumTier = LicenseManager.getTier(context)

    LaunchedEffect(Unit) {
        running = ServiceRunning.isRunning(context, MonitorService::class.java)
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            if (!granted) {
                snackbarHostState.showSnackbar(context.getString(R.string.permission_notifications))
            }
        }
    }

    fun ensureNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        if (PermissionUtils.notificationsEnabled(context)) return true
        notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return false
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun toggleMonitoring() {
        if (!PermissionUtils.canDrawOverlays(context)) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.permission_overlay))
            }
            openOverlaySettings()
            return
        }
        if (!ensureNotifications()) return

        if (running) {
            MonitorService.stop(context)
            running = false
        } else {
            MonitorService.start(context)
            running = true
        }
    }

    val sensorTiles = remember {
        listOf(
            SensorTileSpec(HardwareOp.TYPE_CAMERA, R.string.hardware_camera) { m, t ->
                Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = m, tint = t)
            },
            SensorTileSpec(HardwareOp.TYPE_MIC, R.string.hardware_mic) { m, t ->
                Icon(Icons.Outlined.Mic, contentDescription = null, modifier = m, tint = t)
            },
            SensorTileSpec(HardwareOp.TYPE_GPS, R.string.hardware_gps) { m, t ->
                Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = m, tint = t)
            },
            SensorTileSpec(HardwareOp.TYPE_CONTACTS_READ, R.string.hardware_contacts_read_short) { m, t ->
                Icon(Icons.Outlined.Contacts, contentDescription = null, modifier = m, tint = t)
            },
            SensorTileSpec(HardwareOp.TYPE_SMS_READ, R.string.hardware_sms_read_short) { m, t ->
                Icon(Icons.Outlined.Sms, contentDescription = null, modifier = m, tint = t)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_xtq_mascot),
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextWhite
                            )
                            if (isPremium) {
                                Text(
                                    text = premiumTier.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPremium) {
                        Icon(
                            Icons.Outlined.WorkspacePremium,
                            contentDescription = "Premium",
                            tint = if (isPremium) GoldPrimary else GoldSubtle
                        )
                    }
                    IconButton(onClick = onOpenGuide) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.nav_guide), tint = TextMuted)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_settings), tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PitchBlack
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // Premium Banner (if not activated)
            if (!isPremium) {
                PremiumCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unlock Premium",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Get location, SMS, contacts & more",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        PremiumButton(
                            text = "Activate",
                            onClick = onOpenPremium,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.dashboard_header_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sensorTiles, key = { it.opType }) { spec ->
                    CompactSensorTile(
                        title = stringResource(spec.titleRes),
                        active = active.firstOrNull { it.opType == spec.opType },
                        icon = { mod, tint -> spec.icon(mod, tint) },
                        modifier = Modifier.width(72.dp)
                    )
                }
            }

            Text(
                text = if (PermissionUtils.hasAppOpsStats(context)) {
                    stringResource(R.string.dashboard_mode_precise_hint)
                } else {
                    stringResource(R.string.dashboard_mode_hint)
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )

            PremiumButton(
                text = if (running) stringResource(R.string.monitoring_stop)
                else stringResource(R.string.monitoring_start),
                onClick = { toggleMonitoring() }
            )

            if (running) {
                Text(
                    stringResource(R.string.monitoring_running),
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                stringResource(R.string.recent_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite
            )
            Spacer(Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(recent, key = { it.id }) { row ->
                    RecentRow(row)
                }
            }

            PremiumButton(
                text = stringResource(R.string.dashboard_open_stats),
                onClick = onOpenStats
            )
            Spacer(Modifier.height(6.dp))
            PremiumButton(
                text = stringResource(R.string.open_history),
                onClick = onOpenHistory
            )
        }
    }
}

@Composable
private fun CompactSensorTile(
    title: String,
    icon: @Composable (Modifier, Color) -> Unit,
    active: PrivacyEvent?,
    modifier: Modifier = Modifier
) {
    val iconTint = if (active != null) GoldPrimary else GoldSubtle

    val scale by animateFloatAsState(
        targetValue = if (active != null) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tileScale"
    )

    Box(
        modifier = modifier
            .height(86.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .then(
                if (active != null) {
                    Modifier.border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GoldDark.copy(alpha = 0.5f), GoldPrimary.copy(alpha = 0.8f), GoldLight.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = GoldSubtle.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon(Modifier.size(22.dp), iconTint)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (active == null) "—" else active.appName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (active != null) GoldPrimary else GoldSubtle
            )
        }
    }
}

@Composable
private fun RecentRow(event: PrivacyEvent) {
    val fmt = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()) }
    val hw = when (event.opType) {
        HardwareOp.TYPE_MIC -> stringResource(R.string.hardware_mic)
        HardwareOp.TYPE_GPS -> stringResource(R.string.hardware_gps)
        HardwareOp.TYPE_CONTACTS_READ -> stringResource(R.string.hardware_contacts_read_short)
        HardwareOp.TYPE_SMS_READ -> stringResource(R.string.hardware_sms_read_short)
        else -> stringResource(R.string.hardware_camera)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.appName, style = MaterialTheme.typography.titleSmall, color = TextWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(hw, style = MaterialTheme.typography.labelMedium, color = GoldPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt.format(Date(event.startTime)), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                val end = event.endTime
                Text(
                    if (end == null) "Ongoing" else fmt.format(Date(end)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
