package com.couplefit.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.data.local.MetricSharingPreferences
import com.couplefit.ui.theme.SystemBlue
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.SystemRed
import com.couplefit.ui.theme.VitalsAccent
import com.couplefit.ui.theme.dialogMeasure
import com.couplefit.ui.theme.readableMeasure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyTransparencyScreen(
    onNavigateBack: () -> Unit,
    sharingPrefs: MetricSharingPreferences = MetricSharingPreferences(),
    partnerName: String = "Partner",
    onUnlinkPartner: () -> Unit = {},
    onDeleteMyData: () -> Unit = {},
    isUnlinking: Boolean = false,
    isDeleting: Boolean = false
) {
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    val pName = partnerName.ifBlank { "Partner" }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy & Data Transparency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .readableMeasure(640.dp)
                    .fillMaxWidth()
            ) {
                // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Zero-Knowledge Fitness Sharing",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "CoupleFit connects only you and your partner. We never sell your health metrics or use them for advertising.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: What is shared with your partner
            val pName = partnerName.ifBlank { "Partner" }
            Text(
                "SHARED WITH ${pName.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            PrivacyDataCard(
                icon = Icons.Default.Share,
                iconTint = SystemBlue,
                title = "$pName Visible Metrics (Only If Toggled ON)",
                items = listOf(
                    PrivacyItem(
                        "Daily Step Count",
                        "Aggregated daily sum to power your side-by-side activity rings.",
                        sharingPrefs.shareSteps
                    ),
                    PrivacyItem(
                        "Active Minutes & Streak",
                        "Daily exercise minutes used for active streaks and challenges.",
                        sharingPrefs.shareWorkouts
                    ),
                    PrivacyItem(
                        "Active Calories Burned",
                        "Daily energy expenditure aggregate for ring progress.",
                        sharingPrefs.shareCalories
                    ),
                    PrivacyItem(
                        "Resting & Average Heart Rate",
                        "Single daily aggregate value (no continuous raw ECG waveforms).",
                        sharingPrefs.shareHr
                    ),
                    PrivacyItem(
                        "Blood Oxygen (SpO2)",
                        "Daily average saturation percentage.",
                        sharingPrefs.shareSpo2
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: What stays strictly on your device
            Text(
                "STAYS ON YOUR DEVICE (NEVER SHARED)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SystemGreen,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            PrivacyDataCard(
                icon = Icons.Default.Lock,
                iconTint = SystemGreen,
                title = "Confidential Device-Local Records",
                items = listOf(
                    PrivacyItem(
                        "Raw Sensor Waveforms",
                        "Minute-by-minute PPG, raw accelerometer signals, and raw ECG tracks.",
                        isPrivate = true
                    ),
                    PrivacyItem(
                        "Precise GPS Locations & Routes",
                        "Workout maps, coordinates, and routes remain in Health Connect only.",
                        isPrivate = true
                    ),
                    PrivacyItem(
                        "Intraday Sleep Staging Data",
                        "Deep, REM, and light sleep epoch breakdowns are never uploaded.",
                        isPrivate = true
                    ),
                    PrivacyItem(
                        "Toggled-OFF Metrics",
                        "Any metric you turn OFF in Settings is strictly excluded from sync.",
                        isPrivate = true
                    ),
                    PrivacyItem(
                        "Health Connect Auth Tokens",
                        "System permission tokens are kept inside private app storage.",
                        isPrivate = true
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: Account Actions (Unlink & Delete)
            Text(
                "MANAGE DATA & CONNECTIONS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Unlink Partner Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Unlink $pName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sever connection with $pName. Live data sharing will immediately stop, and ${pName}'s cached data will be removed from your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showUnlinkDialog = true },
                        enabled = !isUnlinking && !isDeleting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUnlinking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Unlink From $pName")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delete My Data Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SystemRed.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        SystemRed.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = SystemRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Delete My Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SystemRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Permanently erase all your Firestore cloud documents, daily health aggregates, active challenges, nudges, and local database cache. This action is irreversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        enabled = !isDeleting && !isUnlinking,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Permanently Delete My Data & Account", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Unlink Dialog
    if (showUnlinkDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = { showUnlinkDialog = false },
            icon = { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Unlink $pName?") },
            text = {
                Text("This will disconnect your account from $pName. Neither of you will see each other's live health rings, charts, or challenges until you pair again.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkDialog = false
                        onUnlinkPartner()
                    }
                ) {
                    Text("Confirm Unlink")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account & Data Dialog
    if (showDeleteDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = {
                showDeleteDialog = false
                deleteConfirmationText = ""
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = SystemRed) },
            title = { Text("Delete All Data Permanently?") },
            text = {
                Column {
                    Text(
                        "This will immediately invoke our Cloud Function to delete all Firestore documents under couples/{coupleId} and users/{uid}. Your local Room cache will be completely erased.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Type DELETE below to confirm:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it },
                        singleLine = true,
                        placeholder = { Text("DELETE") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMyData()
                    },
                    enabled = deleteConfirmationText.trim() == "DELETE",
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed)
                ) {
                    Text("Delete Forever", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteConfirmationText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class PrivacyItem(
    val title: String,
    val description: String,
    val isEnabled: Boolean = true,
    val isPrivate: Boolean = false
)

@Composable
private fun PrivacyDataCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    items: List<PrivacyItem>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (item.isPrivate || item.isEnabled) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (item.isPrivate) SystemGreen else if (item.isEnabled) VitalsAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (!item.isPrivate) {
                                Text(
                                    if (item.isEnabled) "ACTIVE" else "OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isEnabled) VitalsAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    )
                }
            }
        }
    }
}
