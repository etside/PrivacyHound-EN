package com.privacyhound.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.privacyhound.android.BuildConfig
import com.privacyhound.android.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cmd = remember {
        "adb shell pm grant ${BuildConfig.APPLICATION_ID} android.permission.GET_APP_OPS_STATS"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.guide_about_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.guide_about_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.guide_about_official_sites_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
                Text(
                    stringResource(R.string.guide_about_site_cn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
                Text(
                    stringResource(R.string.guide_about_site_en),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.guide_hackinggroup_wechat_qr),
                    contentDescription = stringResource(R.string.guide_about_wechat_qr_cd),
                    modifier = Modifier
                        .width(156.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
                Text(
                    stringResource(R.string.guide_about_wechat_qr_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                stringResource(R.string.guide_about_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                stringResource(R.string.guide_section_easy_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(stringResource(R.string.guide_easy_intro), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.guide_easy_step_overlay), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.guide_easy_step_notifications), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.guide_easy_step_limits), style = MaterialTheme.typography.bodyMedium)

            Text(
                stringResource(R.string.guide_section_advanced_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(stringResource(R.string.guide_advanced_intro), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.guide_step_dev), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.guide_step_adb), style = MaterialTheme.typography.bodyMedium)
            Text(
                cmd,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("adb", cmd))
                    scope.launch {
                        snackbar.showSnackbar(context.getString(R.string.guide_copied))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.guide_copy))
            }

            Text(
                stringResource(R.string.guide_usage_stats_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
