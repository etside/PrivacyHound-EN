package com.privacyhound.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    onActivated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var passphrase by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val isActive = LicenseManager.isActive(context)
    val currentTier = LicenseManager.getTier(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PitchBlack
                )
            )
        },
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Current Status
            if (isActive) {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currentTier.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = LicenseManager.getExpiryFormatted(context),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                GoldDivider()
                Spacer(Modifier.height(16.dp))
            }

            // Activate Section
            Text(
                text = if (isActive) "Upgrade Plan" else "Activate Premium",
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter your passphrase to unlock premium features",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Identifier Input
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Email or Phone", color = TextMuted) },
                placeholder = { Text("user@email.com", color = GoldSubtle) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldSubtle,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = GoldPrimary
                ),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // Passphrase Input
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text("Passphrase", color = TextMuted) },
                placeholder = { Text("ETG-XXXX-XXXX-XXXX", color = GoldSubtle) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldSubtle,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = GoldPrimary
                ),
                singleLine = true
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = error!!,
                        color = Color(0xFFEF5350),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            PremiumButton(
                text = if (success) "Activated!" else "Activate",
                onClick = {
                    if (identifier.isBlank()) {
                        error = "Enter your email or phone"
                        return@PremiumButton
                    }
                    if (passphrase.isBlank()) {
                        error = "Enter your passphrase"
                        return@PremiumButton
                    }
                    val verified = LicenseManager.verifyPassphrase(context, passphrase, identifier)
                    if (verified) {
                        error = null
                        success = true
                        onActivated()
                    } else {
                        error = "Invalid passphrase. Check with admin."
                    }
                },
                enabled = !success && passphrase.isNotBlank() && identifier.isNotBlank()
            )

            Spacer(Modifier.height(16.dp))

            // WhatsApp Contact
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
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
                            text = "Need a passphrase?",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite
                        )
                        Text(
                            text = "Contact us on WhatsApp",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    PremiumButton(
                        text = "WhatsApp",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/+8801873722228?text=Hi, I want to activate premium for eT_Saftey_Manager_Premium")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tier Comparison
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Premium Tiers",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary
                    )
                    GoldDivider()
                    TierRow("Gold", "Camera, Mic, Location, Stats, 30-day history")
                    TierRow("Platinum", "All features + SMS, Contacts, Export, Unlimited")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TierRow(name: String, features: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Star,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = GoldLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = features,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
