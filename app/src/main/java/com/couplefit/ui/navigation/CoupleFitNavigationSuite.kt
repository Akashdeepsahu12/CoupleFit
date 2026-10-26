package com.couplefit.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Adaptive Material 3 Navigation Container.
 *
 * Implements persistent 4-tab navigation adhering to `card-sort-analysis.md` §7.1
 * and multi-modal active state indication (NAV-01 & NAV-03):
 * - Compact screens (<600dp): Bottom [NavigationBar] (80dp height, thumb-zone optimized)
 * - Medium/Expanded screens (≥600dp): Leading [NavigationRail]
 * - Multi-modal active indicator: Filled vs Outlined icon vector + bold typography + container pill.
 */
@Composable
fun CoupleFitNavigationSuite(
    windowSizeClass: WindowSizeClass,
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val haptic = LocalHapticFeedback.current

    if (isCompact) {
        // Compact Viewport: Phones (<600dp)
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.height(80.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    TopLevelDestination.allDestinations.forEach { destination ->
                        val isSelected = destination == selectedDestination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    } else {
        // Expanded Viewport: Tablets and Foldables (≥600dp)
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.width(80.dp),
                containerColor = MaterialTheme.colorScheme.surface,
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
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                content(PaddingValues(0.dp))
            }
        }
    }
}
