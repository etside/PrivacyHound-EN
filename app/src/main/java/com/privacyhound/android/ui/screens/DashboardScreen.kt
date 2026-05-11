package com.privacyhound.android.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.data.PrivacyEvent
import com.privacyhound.android.monitor.HardwareOp
import com.privacyhound.android.service.MonitorService
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
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGuide) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_guide))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.dashboard_header_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )

            Button(
                onClick = { toggleMonitoring() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (running) stringResource(R.string.monitoring_stop)
                    else stringResource(R.string.monitoring_start)
                )
            }

            if (running) {
                Text(
                    stringResource(R.string.monitoring_running),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(stringResource(R.string.recent_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(recent, key = { it.id }) { row ->
                    RecentRow(row)
                }
            }

            Button(
                onClick = onOpenHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(stringResource(R.string.open_history))
            }
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
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val iconTint = if (active != null) accent else muted
    Surface(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (active != null) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        tonalElevation = 0.dp
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (active == null) {
                    stringResource(R.string.sensor_idle_dash)
                } else {
                    active.appName
                },
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (active != null) accent else muted
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.appName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(hw, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt.format(Date(event.startTime)), style = MaterialTheme.typography.labelSmall)
                val end = event.endTime
                Text(
                    if (end == null) stringResource(R.string.duration_ongoing)
                    else fmt.format(Date(end)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
