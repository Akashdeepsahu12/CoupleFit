package com.couplefit.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.couplefit.ui.components.ActivityRing
import com.couplefit.ui.components.ChartDataPoint
import com.couplefit.ui.components.InteractiveChart
import com.couplefit.ui.components.NudgeButton
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {}
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                refreshing = true
                delay(1500) // Simulate sync
                refreshing = false
            }
        }
    )

    Box(modifier = modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // iOS HIG Navigation Header with Date Overline & Large Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TUESDAY, SEPTEMBER 22",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        letterSpacing = 0.8.sp
                    )

                    // iOS Style Mode Capsule Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDarkTheme) "Dark" else "Light",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SystemRed,
                                checkedTrackColor = SystemRed.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "ACTIVITY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pager between My Stats and Girlfriend's Stats
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        beyondBoundsPageCount = 1
                    ) { page ->
                        val isPageActive = pagerState.currentPage == page
                        if (page == 0) {
                            StatCard(
                                title = "My Stats",
                                subtitle = "Swipe left for Girlfriend's stats →",
                                stepsProgress = 0.8f,
                                exerciseProgress = 0.5f,
                                animTrigger = isPageActive && pagerState.currentPage == 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        } else {
                            StatCard(
                                title = "Girlfriend's Stats",
                                subtitle = "← Swipe right for your stats",
                                stepsProgress = 0.65f,
                                exerciseProgress = 0.9f,
                                animTrigger = isPageActive && pagerState.currentPage == 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) SystemRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "VITALS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "Heart Rate & ECG",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                val chartData = listOf(
                    ChartDataPoint("12 AM", 58f),
                    ChartDataPoint("4 AM", 52f),
                    ChartDataPoint("8 AM", 76f),
                    ChartDataPoint("12 PM", 88f),
                    ChartDataPoint("4 PM", 72f),
                    ChartDataPoint("8 PM", 64f),
                    ChartDataPoint("Now", 69f)
                )
                InteractiveChart(
                    data = chartData,
                    lineColor = SystemRed,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NudgeButton(
                text = "Send Nudge to Partner",
                onClick = { /* Send Nudge */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = SystemRed
        )
    }
}

@Composable
fun StatCard(
    title: String,
    subtitle: String = "",
    stepsProgress: Float,
    exerciseProgress: Float,
    animTrigger: Any? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val haptic = LocalHapticFeedback.current
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "cardScale")

    Card(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                        showMenu = true
                    }
                )
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Proportional rings container centered in card
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring (Steps)
                    ActivityRing(
                        progress = stepsProgress,
                        gradientStart = StepsGradientStart,
                        gradientEnd = StepsGradientEnd,
                        modifier = Modifier.fillMaxSize(),
                        thickness = 10.dp,
                        animTrigger = animTrigger
                    )
                    // Middle ring (Exercise)
                    ActivityRing(
                        progress = exerciseProgress,
                        gradientStart = ExerciseGradientStart,
                        gradientEnd = ExerciseGradientEnd,
                        modifier = Modifier.fillMaxSize(0.74f),
                        thickness = 10.dp,
                        animTrigger = animTrigger
                    )
                    // Inner ring (Calories)
                    ActivityRing(
                        progress = (stepsProgress * 0.75f).coerceIn(0f, 1f),
                        gradientStart = CaloriesGradientStart,
                        gradientEnd = CaloriesGradientEnd,
                        modifier = Modifier.fillMaxSize(0.48f),
                        thickness = 10.dp,
                        animTrigger = animTrigger
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Stat metrics preview below rings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(stepsProgress * 10000).toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StepsGradientStart
                        )
                        Text("Steps", style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(exerciseProgress * 60).toInt()}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExerciseGradientStart
                        )
                        Text("Active", style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${((stepsProgress * 0.75f) * 600).toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaloriesGradientStart
                        )
                        Text("Kcal", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = menuOffset
            ) {
                DropdownMenuItem(
                    text = { Text("Hide from Dashboard") },
                    onClick = { showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Share via...") },
                    onClick = { showMenu = false }
                )
            }
        }
    }
}
