package com.couplefit.ui.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.*

@Composable
fun EnterCodeScreen(
    authViewModel: AuthViewModel,
    onNavigateToInvite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by authViewModel.uiState.collectAsState()
    val enteredCode = uiState.enteredCode
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        VitalsAccent.copy(alpha = 0.08f),
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VitalsAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💌", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter Invite Code",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit pairing code your partner sent you to link your accounts.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Code input card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, VitalsAccent.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { authViewModel.updateEnteredCode(it) },
                        label = { Text("6-Digit Pairing Code") },
                        placeholder = { Text("e.g. 582914") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 4.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (enteredCode.length == 6) {
                                    authViewModel.redeemCode(enteredCode)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Paste from clipboard helper
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val item = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!item.isNullOrBlank()) {
                                    // Extract any 6-digit substring
                                    val clean = item.trim().filter { it.isDigit() }.take(6)
                                    authViewModel.updateEnteredCode(clean)
                                }
                            }
                        ) {
                            Text("📋 Paste from Clipboard", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Redeem / Connect Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    authViewModel.redeemCode(enteredCode)
                },
                enabled = enteredCode.length == 6 && !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VitalsAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Connect with Partner",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Error Message Banner with Smart Diagnostic Advice
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
                                    else -> "Pairing Error"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDatabaseNotCreated || isRulesDenied) MaterialTheme.colorScheme.primary else SystemRed
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    isRulesDenied -> {
                                        "Your Cloud Firestore database is active! However, its Security Rules currently deny mobile writes (default production mode).\n\n" +
                                        "To allow authenticated users to connect and link:\n" +
                                        "1. Tap 'Open Rules in Firebase Console' below\n" +
                                        "2. In the Rules tab, paste:\n\n" +
                                        "   allow read, write: if request.auth != null;\n\n" +
                                        "3. Click 'Publish'\n" +
                                        "4. Return here and tap 'Connect with Partner'!"
                                    }
                                    isDatabaseNotCreated -> {
                                        "Cloud Firestore has not been activated yet in Firebase project (couplefit-6053a).\n\n" +
                                        "To connect both physical devices over the internet, Cloud Firestore must be initialized once:\n\n" +
                                        "1. Tap 'Open Firebase Console' below\n" +
                                        "2. Click 'Create database'\n" +
                                        "3. Select your region & choose 'Start in test mode'\n" +
                                        "4. Click 'Create' (takes ~30 seconds)\n\n" +
                                        "Once created, enter the 6-digit code and tap 'Connect with Partner' to link."
                                    }
                                    else -> error
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                            if (isRulesDenied) {
                                Spacer(modifier = Modifier.height(14.dp))
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
                                Spacer(modifier = Modifier.height(14.dp))
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

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = onNavigateToInvite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have a code? Create an Invite",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
