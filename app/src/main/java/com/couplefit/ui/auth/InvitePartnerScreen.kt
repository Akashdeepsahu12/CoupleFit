package com.couplefit.ui.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.data.model.AuthFlowStep
import com.couplefit.data.remote.CoupleLinkManager
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun InvitePartnerScreen(
    authViewModel: AuthViewModel,
    onNavigateToEnterCode: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    // Generate code automatically if not present in state or cache
    LaunchedEffect(Unit) {
        if (uiState.activeInvite == null) {
            authViewModel.generateInviteCode()
        }
    }

    // Active invite resolution: only display genuinely generated invite
    val invite = uiState.activeInvite
    val inviteCode = invite?.inviteCode?.takeIf { it.isNotBlank() && it.length == 6 }
    val inviteUrl = invite?.inviteUrl ?: inviteCode?.let { "https://couplefit.app/invite/$it" } ?: ""

    // Real-time link listener: automatically navigates to dashboard when partner redeems code
    val activeCoupleId = invite?.coupleId
    val activeCode = inviteCode
    LaunchedEffect(activeCoupleId, activeCode) {
        if (!activeCoupleId.isNullOrBlank() || !activeCode.isNullOrBlank()) {
            authViewModel.startListeningForPartnerLinking(
                coupleId = activeCoupleId ?: "",
                inviteCode = activeCode ?: ""
            )
            // Periodic backup query ensuring rapid transition even if socket drops
            while (isActive) {
                delay(3500L)
                authViewModel.checkManualPartnerLinkStatus(activeCoupleId, activeCode)
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        ExerciseAccent.copy(alpha = 0.08f),
                        StepsAccent.copy(alpha = 0.12f)
                    )
                )
            )
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        ) {
            // Header icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(StepsAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔗",
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Invite Your Partner",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share this 6-digit pairing code or invite link with your partner. The invite expires in 24 hours.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 6-Digit Code Display Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, StepsAccent.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading || inviteCode == null) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = StepsAccent
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Generating secure pairing code...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Code Generation Failed",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SystemRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { authViewModel.generateInviteCode() },
                                colors = ButtonDefaults.buttonColors(containerColor = StepsAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry Generating Code")
                            }
                        }
                    } else {
                        // Display 6 responsive boxes for digits
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            inviteCode.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(StepsAccent.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = StepsAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Explicit Plain text readout for clear accessibility
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pairing Code: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = inviteCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StepsAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { authViewModel.regenerateInviteCode() },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("🔄 Generate New Code", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            text = "⏱️ Expires in 24 hours",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live pulse indicator for real-time pairing
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.35f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(StepsAccent.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(StepsAccent.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Waiting for partner to enter code...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = { authViewModel.checkManualPartnerLinkStatus(activeCoupleId, activeCode) },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Partner already entered? Tap here to sync",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Copy Code
                OutlinedButton(
                    onClick = {
                        if (inviteCode != null) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("CoupleFit Pairing Code", inviteCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Pairing code copied: $inviteCode", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = inviteCode != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Copy Code", fontWeight = FontWeight.SemiBold)
                }

                // Share App Link
                Button(
                    onClick = {
                        if (inviteCode != null) {
                            val shareText = "❤️ Let's sync our fitness on CoupleFit!\n\n" +
                                "Tap to pair directly:\n$inviteUrl\n\n" +
                                "Or open CoupleFit and enter 6-digit code: $inviteCode"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "CoupleFit Pairing Invite")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share CoupleFit Pairing Link"))
                        }
                    },
                    enabled = inviteCode != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StepsAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Share Link", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Diagnostic Setup Advice (if Cloud Firestore has not been activated)
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                uiState.errorMessage?.let { error ->
                    val isDatabaseNotCreated = error.contains("Cloud Firestore API has not been used", ignoreCase = true) ||
                            error.contains("is disabled", ignoreCase = true)

                    val isRulesDenied = error.contains("PERMISSION_DENIED", ignoreCase = true) ||
                            error.contains("insufficient permissions", ignoreCase = true) ||
                            error.contains("Security Rules", ignoreCase = true)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDatabaseNotCreated || isRulesDenied) MaterialTheme.colorScheme.surfaceVariant else SystemRed.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDatabaseNotCreated || isRulesDenied) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else SystemRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when {
                                    isRulesDenied -> "Firestore Security Rules Need 1-Click Update"
                                    isDatabaseNotCreated -> "Cloud Firestore Setup"
                                    else -> "Notice"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDatabaseNotCreated || isRulesDenied) MaterialTheme.colorScheme.primary else SystemRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    isRulesDenied -> {
                                        "Your Cloud Firestore database is active! However, its Security Rules currently deny mobile writes (default production mode).\n\n" +
                                        "To allow authenticated users to generate codes and pair:\n" +
                                        "1. Tap 'Open Rules in Firebase Console' below\n" +
                                        "2. In the Rules tab, paste:\n\n" +
                                        "   allow read, write: if request.auth != null;\n\n" +
                                        "3. Click 'Publish'\n" +
                                        "4. Return here and tap 'Retry Generating Code'!"
                                    }
                                    isDatabaseNotCreated -> {
                                        "Cloud Firestore has not been initialized yet in your Firebase project (couplefit-6053a).\n\n" +
                                        "To connect both physical devices over the internet, Cloud Firestore must be created once in Firebase Console:\n\n" +
                                        "1. Tap 'Open Firebase Console' below\n" +
                                        "2. Click 'Create database'\n" +
                                        "3. Select your region & choose 'Start in test mode'\n" +
                                        "4. Click 'Create' (takes ~30 seconds)\n\n" +
                                        "Once created, return to CoupleFit and tap 'Retry Generating Code'."
                                    }
                                    else -> error
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 17.sp
                            )
                            if (isRulesDenied) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.firebase.google.com/project/couplefit-6053a/firestore/rules"))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Text("Open Rules in Firebase Console", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        val standardRule = "rules_version = '2';\nservice cloud.firestore {\n  match /databases/{database}/documents {\n    match /{document=**} {\n      allow read, write: if request.auth != null;\n    }\n  }\n}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Firestore Rule", standardRule)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied rule to clipboard! Paste in Rules tab & Publish.", Toast.LENGTH_LONG).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                ) {
                                    Text("📋 Copy Rule to Clipboard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else if (isDatabaseNotCreated) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.firebase.google.com/project/couplefit-6053a/firestore"))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Text("Open Firebase Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToEnterCode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Did your partner send you a code? Enter Code",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { authViewModel.enterDemoDashboard() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "🚀 Explore Demo Dashboard",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
