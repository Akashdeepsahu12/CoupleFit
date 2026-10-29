package com.couplefit.ui.navigation

import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.R
import com.couplefit.ui.components.AmbientVideoBackground
import com.couplefit.ui.components.AmbientVideoRegistry
import com.couplefit.ui.theme.*

/**
 * Adaptive Material 3 Navigation Container with Floating Glass Navigation Dock (GLS-02, APL-03, DCK-01)
 * and Ambient Living Aurora Video Background (BGV-01, BGV-04, BGV-06).
 *
 * Implements persistent 4-tab navigation adhering to `card-sort-analysis.md` §7.1,
 * multi-modal active state indication (NAV-01 & NAV-03), and Liquid Glass architecture:
 * - Living Ambient Video Background: Hardware-accelerated dynamic shuffle video motion
 * - Compact screens (<600dp): True Floating Hover Glass Navigation Dock (DCK-01) positioned
 *   as an overlay at Alignment.BottomCenter without restrictive Scaffold bottom partitioning,
 *   allowing content, cards, and video to flow continuously underneath.
 * - Medium/Expanded screens (≥600dp): Frosted Glass NavigationRail
 * - ANM-09: Fluid spring-morphing active tab capsule pill indicator with bouncy spring physics
 *   (`Spring.DampingRatioMediumBouncy`, stiffness = 420f) and elastic directional elongation.
 */
@Composable
fun CoupleFitNavigationSuite(
    windowSizeClass: WindowSizeClass,
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    @RawRes ambientVideoResId: Int = AmbientVideoRegistry.defaultAsset.resId,
    includeVideoBackground: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val haptic = LocalHapticFeedback.current
    val destinations = TopLevelDestination.allDestinations
    val selectedIndex = destinations.indexOf(selectedDestination).coerceAtLeast(0)

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Living Ambient Aurora Video Background (BGV-04 / BGV-06: Hoisted to MainActivity root)
        if (includeVideoBackground) {
            AmbientVideoBackground(
                modifier = Modifier.fillMaxSize(),
                videoResId = ambientVideoResId,
                reduceMotion = reduceMotion
            )
        }

        // 2. Navigation Surface (Hover Dock / NavigationRail)
        if (isCompact) {
            // Compact Viewport: Phones (<600dp) with True Floating Hover Glass Navigation Dock (DCK-01)
            Box(modifier = Modifier.fillMaxSize()) {
                // Content renders full-bleed edge-to-edge under the floating dock
                Box(modifier = Modifier.fillMaxSize()) {
                    content(PaddingValues(bottom = 96.dp))
                }

                // Floating Glass Navigation Dock hovering over content with optical glass & blur
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
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
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            val tabWidth = maxWidth / destinations.size

                            // ANM-09: Spring-driven animated index with bouncy settling
                            val animatedIndex by animateFloatAsState(
                                targetValue = selectedIndex.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = 420f
                                ),
                                label = "dockIndicatorIndex"
                            )

                            // Dynamic elastic elongation in travel direction
                            val delta = selectedIndex.toFloat() - animatedIndex
                            val stretchFactor = 1f + (kotlin.math.abs(delta) * 0.16f).coerceAtMost(0.32f)

                            // Sliding active indicator capsule
                            Box(
                                modifier = Modifier
                                    .offset(x = tabWidth * animatedIndex)
                                    .width(tabWidth)
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .graphicsLayer {
                                        scaleX = stretchFactor
                                    }
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.25f),
                                                CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.10f)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(1.dp, CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.40f)),
                                        RoundedCornerShape(22.dp)
                                    )
                            )

                            // Tab destination items
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                destinations.forEachIndexed { index, destination ->
                                    val isSelected = index == selectedIndex
                                    val animatedColor by animateColorAsState(
                                        targetValue = if (isSelected) CoupleFitIdentityTokens.UserPrimary else Color(0xFF8E95A5),
                                        animationSpec = tween(180),
                                        label = "tabColor"
                                    )
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.08f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
                                        label = "tabIconScale"
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(22.dp))
                                            .iosMomentumClickable(
                                                pressedScale = 0.94f,
                                                onClickLabel = destination.contentDescription
                                            ) {
                                                if (!isSelected) {
                                                    CoupleFitHaptics.performSubtleTick(null, haptic)
                                                }
                                                onDestinationSelected(destination)
                                            }
                                            .semantics {
                                                role = Role.Tab
                                                selected = isSelected
                                                contentDescription = "${destination.label} tab, ${if (isSelected) "selected" else "not selected"}"
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = null,
                                            tint = animatedColor,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = destination.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = animatedColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                        destinations.forEach { destination ->
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
