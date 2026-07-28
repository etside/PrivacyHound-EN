package com.privacyhound.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.util.PrefsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }
    val app = context.applicationContext as XiaotianquanApp
    val repo = app.repository
    val scope = rememberCoroutineScope()

    var monitoringEnabled by remember { mutableStateOf(prefs.monitoringEnabled) }
    var overlayEnabled by remember { mutableStateOf(prefs.overlayEnabled) }
    var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }
    var pollingIntervalMs by remember { mutableFloatStateOf(prefs.pollingIntervalMs.toFloat()) }
    var dataRetentionDays by remember { mutableFloatStateOf(prefs.dataRetentionDays.toFloat()) }
    val darkMode by prefs.darkModeFlow.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_data_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    scope.launch { repo.deleteAll() }
                }) {
                    Text(stringResource(R.string.settings_clear_data_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.settings_clear_data_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Monitoring Section ───────────────────────────────
            SettingsSectionHeader(stringResource(R.string.settings_section_monitoring))

            SettingsToggle(
                title = stringResource(R.string.settings_monitoring_enabled),
                subtitle = stringResource(R.string.settings_monitoring_enabled_subtitle),
                checked = monitoringEnabled,
                onCheckedChange = {
                    monitoringEnabled = it
                    prefs.monitoringEnabled = it
                }
            )

            SettingsToggle(
                title = stringResource(R.string.settings_overlay_enabled),
                subtitle = stringResource(R.string.settings_overlay_enabled_subtitle),
                checked = overlayEnabled,
                onCheckedChange = {
                    overlayEnabled = it
                    prefs.overlayEnabled = it
                }
            )

            SettingsToggle(
                title = stringResource(R.string.settings_notifications_enabled),
                subtitle = stringResource(R.string.settings_notifications_enabled_subtitle),
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    prefs.notificationsEnabled = it
                }
            )

            // ── Notifications Section ────────────────────────────
            SettingsSectionHeader(stringResource(R.string.settings_section_notifications))

            val pollingOptions = listOf(15f, 30f, 60f, 120f)
            val pollingLabels = listOf("15s", "30s", "60s", "120s")
            val closestPolling = pollingOptions.minByOrNull { kotlin.math.abs(it - pollingIntervalMs) } ?: 30f

            SettingsDropdown(
                title = stringResource(R.string.settings_polling_interval),
                subtitle = stringResource(R.string.settings_polling_interval_subtitle),
                options = pollingLabels,
                selectedIndex = pollingOptions.indexOf(closestPolling).coerceAtLeast(0),
                onSelected = { index ->
                    pollingIntervalMs = pollingOptions[index]
                    prefs.pollingIntervalMs = pollingOptions[index].toLong() * 1000
                }
            )

            // ── Data Section ─────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.settings_section_data))

            val retentionOptions = listOf(7f, 15f, 30f, 60f, 90f)
            val retentionLabels = listOf("7 days", "15 days", "30 days", "60 days", "90 days")
            val closestRetention = retentionOptions.minByOrNull { kotlin.math.abs(it - dataRetentionDays) } ?: 30f

            SettingsDropdown(
                title = stringResource(R.string.settings_data_retention),
                subtitle = stringResource(R.string.settings_data_retention_subtitle),
                options = retentionLabels,
                selectedIndex = retentionOptions.indexOf(closestRetention).coerceAtLeast(0),
                onSelected = { index ->
                    dataRetentionDays = retentionOptions[index]
                    prefs.dataRetentionDays = retentionOptions[index].toInt()
                }
            )

            // Export section
            SettingsSectionHeader(stringResource(R.string.settings_section_export))

            SettingsNavigationRow(
                title = stringResource(R.string.settings_export_data),
                subtitle = stringResource(R.string.settings_export_data_subtitle),
                onClick = onOpenExport
            )

            // ── Clear Data ───────────────────────────────────────
            SettingsNavigationRow(
                title = stringResource(R.string.settings_clear_data),
                subtitle = stringResource(R.string.settings_clear_data_subtitle),
                onClick = { showClearDialog = true }
            )

            // ── Appearance Section ───────────────────────────────
            SettingsSectionHeader(stringResource(R.string.settings_section_appearance))

            val darkModeOptions = listOf("system", "light", "dark")
            val darkModeLabels = listOf(
                stringResource(R.string.settings_dark_mode_system),
                stringResource(R.string.settings_dark_mode_light),
                stringResource(R.string.settings_dark_mode_dark)
            )
            val currentDarkIndex = darkModeOptions.indexOf(darkMode).coerceAtLeast(0)

            SettingsDropdown(
                title = stringResource(R.string.settings_dark_mode),
                subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                options = darkModeLabels,
                selectedIndex = currentDarkIndex,
                onSelected = { index ->
                    prefs.darkMode = darkModeOptions[index]
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = options[selectedIndex],
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        TextButton(onClick = onClick) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
