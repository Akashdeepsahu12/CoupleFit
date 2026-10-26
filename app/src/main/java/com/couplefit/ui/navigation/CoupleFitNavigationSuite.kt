package com.couplefit.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Adaptive Material 3 Navigation Container.
 *
 * Implements persistent 4-tab navigation adhering to `card-sort-analysis.md` §7.1:
 * - Compact screens (<600dp): Bottom [NavigationBar] (80dp height, thumb-zone optimized)
 * - Medium/Expanded screens (≥600dp): Leading [NavigationRail]
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
                            onClick = { onDestinationSelected(destination) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.contentDescription
                                )
                            },
                            label = {
                                Text(text = destination.label)
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
                        onClick = { onDestinationSelected(destination) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = {
                            Text(text = destination.label)
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
