package com.privacyhound.android.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.data.PrivacyEvent
import com.privacyhound.android.monitor.HardwareOp
import com.privacyhound.android.monitor.shouldShowCallerPackageInReports
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val app = context.applicationContext as XiaotianquanApp
    val repo = app.repository
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var opFilter by remember { mutableIntStateOf(-1) }
    var exportMenu by remember { mutableStateOf(false) }

    LaunchedEffect(activity.intent) {
        val pkg = activity.intent.getStringExtra(com.privacyhound.android.MainActivity.EXTRA_FOCUS_PACKAGE)
        if (!pkg.isNullOrBlank()) query = pkg
    }

    val filteredFlow = remember(query, opFilter) {
        repo.observeFiltered(query.takeIf { it.isNotBlank() }, opFilter.takeIf { it >= 0 })
    }
    val rows by filteredFlow.collectAsStateWithLifecycle(emptyList())

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch { repo.exportCsv(context, uri) }
    }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch { repo.exportJson(context, uri) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { exportMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.export_csv))
                    }
                    DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_csv)) },
                            onClick = {
                                exportMenu = false
                                csvLauncher.launch("privacy_report.csv")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_json)) },
                            onClick = {
                                exportMenu = false
                                jsonLauncher.launch("privacy_report.json")
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_search_hint)) },
                singleLine = true
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HardwareFilterChip(stringResource(R.string.filter_all), selected = opFilter < 0) {
                    opFilter = -1
                }
                HardwareFilterChip(stringResource(R.string.hardware_camera), selected = opFilter == HardwareOp.TYPE_CAMERA) {
                    opFilter = HardwareOp.TYPE_CAMERA
                }
                HardwareFilterChip(stringResource(R.string.hardware_mic), selected = opFilter == HardwareOp.TYPE_MIC) {
                    opFilter = HardwareOp.TYPE_MIC
                }
                HardwareFilterChip(stringResource(R.string.hardware_gps), selected = opFilter == HardwareOp.TYPE_GPS) {
                    opFilter = HardwareOp.TYPE_GPS
                }
                HardwareFilterChip(
                    stringResource(R.string.hardware_contacts_read_short),
                    selected = opFilter == HardwareOp.TYPE_CONTACTS_READ
                ) {
                    opFilter = HardwareOp.TYPE_CONTACTS_READ
                }
                HardwareFilterChip(
                    stringResource(R.string.hardware_sms_read_short),
                    selected = opFilter == HardwareOp.TYPE_SMS_READ
                ) {
                    opFilter = HardwareOp.TYPE_SMS_READ
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rows, key = { it.id }) { HistoryRow(it) }
            }
        }
    }
}

@Composable
private fun HardwareFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun HistoryRow(event: PrivacyEvent) {
    val context = LocalContext.current
    val selfPkg = context.packageName
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val ongoing = stringResource(R.string.duration_ongoing)
    val hw = when (event.opType) {
        HardwareOp.TYPE_MIC -> stringResource(R.string.hardware_mic)
        HardwareOp.TYPE_GPS -> stringResource(R.string.hardware_gps)
        HardwareOp.TYPE_CONTACTS_READ -> stringResource(R.string.hardware_contacts_read_short)
        HardwareOp.TYPE_SMS_READ -> stringResource(R.string.hardware_sms_read_short)
        else -> stringResource(R.string.hardware_camera)
    }
    val rangeText = remember(event.startTime, event.endTime, ongoing) {
        val start = fmt.format(Date(event.startTime))
        val end = event.endTime?.let { fmt.format(Date(it)) } ?: ongoing
        "$start → $end"
    }
    val detailLine = if (shouldShowCallerPackageInReports(event.packageName, selfPkg)) {
        "${event.packageName} · $hw"
    } else {
        hw
    }
    val titleLine = if (event.packageName == selfPkg) {
        stringResource(R.string.report_unknown_caller_title)
    } else {
        event.appName
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titleLine, style = MaterialTheme.typography.titleMedium)
            Text(detailLine, style = MaterialTheme.typography.bodyMedium)
            Text(
                rangeText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                if (event.isForeground) stringResource(R.string.foreground_badge)
                else stringResource(R.string.background_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
