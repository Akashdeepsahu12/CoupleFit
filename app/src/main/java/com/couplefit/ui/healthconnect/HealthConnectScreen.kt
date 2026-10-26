package com.couplefit.ui.healthconnect

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.data.healthconnect.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HealthConnectScreen(
    healthConnectManager: HealthConnectManager,
    viewModel: HealthConnectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        viewModel.checkStatusAndFetchData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is HealthConnectUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Syncing with Health Connect...")
            }
            
            is HealthConnectUiState.NotAvailable -> {
                Text(
                    text = "Health Connect Not Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please install or update Health Connect from the Play Store.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    healthConnectManager.openPlayStore(context)
                }) {
                    Text("Get Health Connect")
                }
            }
            
            is HealthConnectUiState.PermissionRevoked -> {
                Text(
                    text = "Connect / Grant Permissions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    try {
                        permissionLauncher.launch(healthConnectManager.permissions)
                    } catch (e: Exception) {
                        healthConnectManager.openHealthConnect(context)
                    }
                }) {
                    Text("Connect Health Connect")
                }
            }
            
            is HealthConnectUiState.Empty -> {
                Text(
                    text = "No Data Found in Range",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No step or heart rate data found for the last 24 hours.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.checkStatusAndFetchData() }) {
                    Text("Sync Now")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.writeMockData() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Write Mock Data")
                }
            }
            
            is HealthConnectUiState.Success -> {
                Text(
                    text = "Health Data Synced",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Steps (Last 24h): ${state.data.totalSteps}", style = MaterialTheme.typography.bodyLarge)
                        Text("Avg HR (Last 24h): ${String.format("%.1f", state.data.averageHeartRate)} BPM", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
                Text(
                    text = "Last Synced at ${formatter.format(state.lastSyncedAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.checkStatusAndFetchData() }) {
                    Text("Sync Now")
                }
            }
        }
    }
}
