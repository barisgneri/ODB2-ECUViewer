package com.baris.feature.gauge

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baris.feature.gauge.DashboardContract.UiAction
import com.baris.feature.gauge.DashboardContract.UiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    } else {
        null
    }

    LaunchedEffect(bluetoothPermissions?.allPermissionsGranted) {
        if (bluetoothPermissions?.allPermissionsGranted == true) {
            viewModel.onAction(UiAction.RefreshDevices)
        }
    }

    LaunchedEffect(Unit) {
        bluetoothPermissions?.launchMultiplePermissionRequest()
    }

    DashboardScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}

@Composable
fun DashboardScreen(
    uiState: UiState,
    onAction: (UiAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is UiState.PermissionRequired -> {
                PermissionRequiredContent()
            }

            is UiState.Disconnected -> {
                ConnectionContent(
                    pairedDevices = uiState.pairedDevices,
                    isBluetoothEnabled = uiState.isBluetoothEnabled,
                    onConnect = { onAction(UiAction.Connect(it)) }
                )
            }

            is UiState.Connected -> {
                DashboardContent(
                    metrics = uiState.metrics,
                    isReconnecting = uiState.isReconnecting,
                    onDisconnect = { onAction(UiAction.Disconnect) }
                )
            }

            is UiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This app needs Bluetooth permissions to connect to your OBD2 device.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ConnectionContent(
    pairedDevices: List<Pair<String, String>>,
    isBluetoothEnabled: Boolean,
    onConnect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Device",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!isBluetoothEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Bluetooth is turned off. Please turn it on to see devices.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        LazyColumn {
            items(pairedDevices) { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConnect(device.second) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = device.first)
                    Text(text = device.second, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    metrics: Map<com.baris.core.model.ObdDataType, Float>,
    isReconnecting: Boolean,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Live Metrics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isReconnecting) {
            Text(
                text = "Reconnecting...",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(metrics.toList()) { (type, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = type.label)
                    Text(text = String.format("%.2f %s", value, type.unit))
                }
            }
        }

        Button(
            onClick = onDisconnect,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Disconnect")
        }
    }
}
