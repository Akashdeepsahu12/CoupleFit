package com.couplefit.ui.trends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.AppSpacing

/**
 * Health & Trends Screen (Tab 3).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTimeHorizon by remember { mutableStateOf("Week") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Trends",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = AppSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Day", "Week", "Month", "Year").forEach { horizon ->
                            FilterChip(
                                selected = selectedTimeHorizon == horizon,
                                onClick = { selectedTimeHorizon = horizon },
                                label = { Text(horizon) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            item {
                Text(
                    text = "STEPS & ACTIVITY COMPARISON ($selectedTimeHorizon)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = AppSpacing.sm)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Combined Steps",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "92,400 / 100k",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { 0.92f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "You: 48,200 steps • Partner: 44,200 steps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "MEDICATION & SUPPLEMENT TRACKER",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        MedicationRow(name = "Vitamin D3", time = "08:00 AM", taken = true)
                        HorizontalDivider()
                        MedicationRow(name = "Omega-3", time = "01:00 PM", taken = true)
                        HorizontalDivider()
                        MedicationRow(name = "Magnesium Glycinate", time = "10:00 PM", taken = false)
                    }
                }
            }

            item {
                Text(
                    text = "SENSOR DIAGNOSTICS & SYNC STATUS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Health Connect Integration", style = MaterialTheme.typography.bodyMedium)
                            Text("🟢 Connected", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Background Sync", style = MaterialTheme.typography.bodyMedium)
                            Text("2 min ago", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationRow(
    name: String,
    time: String,
    taken: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Scheduled: $time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Badge(
            containerColor = if (taken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = if (taken) "Taken ✓" else "Pending ⏱️",
                color = if (taken) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
