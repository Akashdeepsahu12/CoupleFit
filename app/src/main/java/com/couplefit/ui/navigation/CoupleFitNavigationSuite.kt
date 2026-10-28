package com.couplefit.ui.navigation

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.couplefit.R
import com.couplefit.ui.components.AmbientVideoBackground
import com.couplefit.ui.components.AmbientVideoRegistry
import com.couplefit.ui.theme.*

/**
 * Adaptive Material 3 Navigation Container with Floating Glass Navigation Dock (GLS-02, APL-03)
 * and Ambient Living Aurora Video Background (BGV-01, BGV-04).
 *
 * Implements persistent 4-tab navigation adhering to `card-sort-analysis.md` §7.1,
 * multi-modal active state indication (NAV-01 & NAV-03), and Liquid Glass architecture:
 * - Living Ambient Video Background: Hardware-accelerated, dynamic shuffle video motion (`green1.mp4`, `blue.mp4`)
 *   with CenterCrop scaling, automatic lifecycle pause/resume, and reduceMotion awareness.
 * - Compact screens (<600dp): Floating Glass Navigation Dock (16dp elevated pill, 32dp corners, 28dp frosted blur)
 * - Medium/Expanded screens (≥600dp): Frosted Glass NavigationRail
 * - Multi-modal active indicator: Filled vs Outlined icon vector + bold typography + cyan indicator capsule.
 */
@Composable
fun CoupleFitNavigationSuite(
    windowSizeClass: WindowSizeClass,
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    @RawRes ambientVideoResId: Int = AmbientVideoRegistry.defaultAsset.resId,
    content: @Composable (PaddingValues) -> Unit
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Living Ambient Aurora Video Background (BGV-04: dynamic shuffle R.raw.green1 / R.raw.blue)
        AmbientVideoBackground(
            modifier = Modifier.fillMaxSize(),
            videoResId = ambientVideoResId,
            reduceMotion = reduceMotion
        )

        // 2. Navigation Surface (Scaffold / NavigationRail)
        if (isCompact) {
            // Compact Viewport: Phones (<600dp) with Floating Glass Navigation Dock
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .glassDock(),
                            color = Color.Transparent,
                            shape = GlassComponentTokens.DockCornerShape,
                            shadowElevation = GlassPrimitives.ElevationDock
                        ) {
                            val navItemColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CoupleFitIdentityTokens.UserPrimary,
                                selectedTextColor = CoupleFitIdentityTokens.UserPrimary,
                                unselectedIconColor = Color(0xFF8E95A5),
                                unselectedTextColor = Color(0xFF8E95A5),
                                indicatorColor = CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.22f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TopLevelDestination.allDestinations.forEach { destination ->
                                    val isSelected = destination == selectedDestination
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isSelected) {
                                                CoupleFitHaptics.performSubtleTick(null, haptic)
                                            }
                                            onDestinationSelected(destination)
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                                contentDescription = destination.contentDescription
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = navItemColors
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                content(innerPadding)
            }
        } else {
            // Expanded Viewport: Tablets and Foldables (≥600dp) with Glass Rail
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    val railItemColors = NavigationRailItemDefaults.colors(
                        selectedIconColor = CoupleFitIdentityTokens.UserPrimary,
                        selectedTextColor = CoupleFitIdentityTokens.UserPrimary,
                        unselectedIconColor = Color(0xFF8E95A5),
                        unselectedTextColor = Color(0xFF8E95A5),
                        indicatorColor = CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.22f)
                    )
                    NavigationRail(
                        modifier = Modifier
                            .width(84.dp)
                            .fillMaxHeight()
                            .glassDock(shape = RoundedCornerShape(24.dp)),
                        containerColor = Color.Transparent,
                        header = {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    ) {
                        TopLevelDestination.allDestinations.forEach { destination ->
                            val isSelected = destination == selectedDestination
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        CoupleFitHaptics.performSubtleTick(null, haptic)
                                    }
                                    onDestinationSelected(destination)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.contentDescription
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = railItemColors
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    content(PaddingValues(0.dp))
                }
            }
        }
    }
}
