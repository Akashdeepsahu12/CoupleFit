package com.couplefit.ui.hub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.ui.network.NetworkViewModel
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Couple Hub Screen (Tab 4).
 * Enriched with full personal & partner profile dossiers (CPX-07):
 * - User name, Partner name
 * - Total days paired counter
 * - Birthdays and Relationship status
 * - Interactive heart ripple haptics (ANM-14)
 * - Harmonized 4-layer Apple Liquid Glass styling (CPX-08)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleHubScreen(
    modifier: Modifier = Modifier,
    onNavigateToPrivacySettings: () -> Unit = {},
    reduceMotion: Boolean = false,
    networkViewModel: NetworkViewModel = hiltViewModel(),
    hubViewModel: CoupleHubViewModel = hiltViewModel()
) {
    val isConnected by networkViewModel.isConnected.collectAsState()
    val uiState by hubViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var editType by remember { mutableStateOf("") }
    var editCurrentValue by remember { mutableStateOf("") }

    // ANM-14: Heartbeat pulse scale & ripple animation states
    val heartScale = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(0f) }
    var heartbeatBannerVisible by remember { mutableStateOf(false) }

    // REL-01 & AUD-P0-03 & AUD-P1-04: Living Partner Presence Breathing Radial Glow Halo
    val isPartnerActive = isConnected && uiState.partnerName != "Partner"
    val presenceGlowAlpha = if (reduceMotion) {
        0.35f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "livingPresenceTransition")
        infiniteTransition.animateFloat(
            initialValue = if (isPartnerActive) 0.25f else 0.10f,
            targetValue = if (isPartnerActive) 0.65f else 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isPartnerActive) 2200 else 4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "presenceGlowAlpha"
        ).value
    }
    val presenceRadiusMultiplier = if (reduceMotion) {
        1.0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "livingPresenceRadiusTransition")
        infiniteTransition.animateFloat(
            initialValue = if (isPartnerActive) 0.88f else 0.94f,
            targetValue = if (isPartnerActive) 1.05f else 1.01f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isPartnerActive) 2200 else 4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "presenceRadiusMultiplier"
        ).value
    }

    fun triggerHeartbeat() {
        CoupleFitHaptics.performHeartbeat(context, haptic)
        heartbeatBannerVisible = true
        coroutineScope.launch {
            heartScale.animateTo(1.24f, tween(90, easing = FastOutSlowInEasing))
            heartScale.animateTo(1.06f, tween(60, easing = LinearEasing))
            heartScale.animateTo(1.32f, tween(110, easing = FastOutSlowInEasing))
            heartScale.animateTo(1.0f, spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMedium))
        }
        coroutineScope.launch {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
        }
        coroutineScope.launch {
            delay(2200)
            heartbeatBannerVisible = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Couple Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Couple Milestone & Relationship Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm)
                        .iosGlass(shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Interactive Couple Avatar with Heartbeat Ripple Aura
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Partner profile avatar. Tap to send live heartbeat ripple."
                                            onClick(label = "Send heartbeat to ${uiState.partnerName}") {
                                                triggerHeartbeat()
                                                true
                                            }
                                        }
                                ) {
                                    // REL-01: Living Partner Presence Breathing Halo
                                    if (isConnected) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val maxRadius = size.minDimension / 2f
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = presenceGlowAlpha * 0.40f),
                                                        CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = presenceGlowAlpha * 0.12f),
                                                        Color.Transparent
                                                    ),
                                                    radius = maxRadius * presenceRadiusMultiplier
                                                ),
                                                radius = maxRadius * presenceRadiusMultiplier
                                            )
                                        }
                                    }
                                    if (rippleProgress.value > 0f && rippleProgress.value < 1f) {
                                        val r = rippleProgress.value
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val maxRadius = size.minDimension / 2f
                                            drawCircle(
                                                color = CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = (1f - r) * 0.75f),
                                                radius = maxRadius * (0.65f + r * 0.35f),
                                                style = Stroke(width = (3.5f * (1f - r)).dp.toPx())
                                            )
                                            if (r > 0.15f) {
                                                val r2 = (r - 0.15f) / 0.85f
                                                drawCircle(
                                                    color = CoupleFitIdentityTokens.UserPrimary.copy(alpha = (1f - r2) * 0.55f),
                                                    radius = maxRadius * (0.55f + r2 * 0.40f),
                                                    style = Stroke(width = (2.5f * (1f - r2)).dp.toPx())
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .graphicsLayer {
                                                scaleX = heartScale.value
                                                scaleY = heartScale.value
                                            }
                                            .iosClickable(pressedScale = 0.94f) {
                                                triggerHeartbeat()
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "❤️",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "${uiState.userName} & ${uiState.partnerName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "💍 Paired for ${uiState.totalDaysPaired} Days",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val statusText = if (isConnected) "🟢 In Sync • Live Presence ⚡" else "🟡 Cached Profile"
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isConnected) SystemGreen else SystemOrange
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    editType = "RELATION"
                                    editCurrentValue = uiState.relationshipType
                                    showEditDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Relationship Info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Relationship quote pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 ${uiState.relationshipType}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Heartbeat feedback toast
                        AnimatedVisibility(
                            visible = heartbeatBannerVisible,
                            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                            exit = fadeOut(tween(220)) + shrinkVertically(tween(220))
                        ) {
                            Text(
                                text = "❤️ Live heartbeat sent to ${uiState.partnerName}!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CoupleFitIdentityTokens.PartnerPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // 2. MY DETAILS CARD (CPX-07: User Profile Dossier)
            item {
                Text(
                    text = "MY PROFILE & DETAILS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MotionCyanStart
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .iosGlass(shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = null
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileDetailRow(
                            label = "Name",
                            value = uiState.userName,
                            icon = "👤",
                            onEdit = {
                                editType = "USER_NAME"
                                editCurrentValue = uiState.userName
                                showEditDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ProfileDetailRow(
                            label = "Birthday",
                            value = uiState.userBirthday,
                            icon = "🎂",
                            onEdit = {
                                editType = "USER_BDAY"
                                editCurrentValue = uiState.userBirthday
                                showEditDialog = true
                            }
                        )
                    }
                }
            }

            // 3. PARTNER'S DETAILS CARD (CPX-07: Partner Profile Dossier)
            item {
                Text(
                    text = "${uiState.partnerName.uppercase()}'S PROFILE",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MotionPartnerStart
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .iosGlass(shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = null
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileDetailRow(
                            label = "Partner Name",
                            value = uiState.partnerName,
                            icon = "💖",
                            onEdit = {
                                editType = "PARTNER_NAME"
                                editCurrentValue = uiState.partnerName
                                showEditDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ProfileDetailRow(
                            label = "Birthday",
                            value = uiState.partnerBirthday,
                            icon = "🎂",
                            onEdit = {
                                editType = "PARTNER_BDAY"
                                editCurrentValue = uiState.partnerBirthday
                                showEditDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ProfileDetailRow(
                            label = "Connected Days",
                            value = "${uiState.totalDaysPaired} Days in CoupleFit",
                            icon = "📅",
                            onEdit = null
                        )
                    }
                }
            }

            // 4. SHARED STREAKS & MEMORIES
            item {
                Text(
                    text = "SHARED STREAKS & MEMORIES",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .iosGlass(shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = null
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        val streakText = if (uiState.sharedGoalStreak > 0) {
                            "🔥 ${uiState.sharedGoalStreak} Days Shared Goal Streak"
                        } else {
                            "🌱 Day 1: Start your shared streak today!"
                        }
                        Text(
                            text = streakText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "🎉 ${365 - (uiState.totalDaysPaired % 365)} Days until Next Fitness Anniversary",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. PRIVACY & SHARING RULES
            item {
                Text(
                    text = "SHARING & VISIBILITY WITH ${uiState.partnerName.uppercase()}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .iosGlass(shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = null
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Steps & Activity Rings", style = MaterialTheme.typography.bodyMedium)
                            Text("Visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Heart Rate & Vitals", style = MaterialTheme.typography.bodyMedium)
                            Text("Visible", style = MaterialTheme.typography.labelMedium, color = SystemGreen)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Medication & Sleep", style = MaterialTheme.typography.bodyMedium)
                            Text("Visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onNavigateToPrivacySettings()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Manage Privacy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Privacy Rules")
                        }
                    }
                }
            }
        }
    }

    // Edit Detail Dialog
    if (showEditDialog) {
        var inputValue by remember { mutableStateOf(editCurrentValue) }
        val dialogTitle = when (editType) {
            "USER_NAME" -> "Edit My Name"
            "PARTNER_NAME" -> "Edit Partner's Name"
            "USER_BDAY" -> "Edit My Birthday"
            "PARTNER_BDAY" -> "Edit Partner's Birthday"
            "RELATION" -> "Edit Relationship Description"
            else -> "Edit Profile Info"
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        when (editType) {
                            "USER_NAME" -> hubViewModel.updateUserName(inputValue)
                            "PARTNER_NAME" -> hubViewModel.updatePartnerName(inputValue)
                            "USER_BDAY" -> hubViewModel.updateUserBirthday(inputValue)
                            "PARTNER_BDAY" -> hubViewModel.updatePartnerBirthday(inputValue)
                            "RELATION" -> hubViewModel.updateRelationshipType(inputValue)
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    icon: String,
    onEdit: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        if (onEdit != null) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onEdit()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
