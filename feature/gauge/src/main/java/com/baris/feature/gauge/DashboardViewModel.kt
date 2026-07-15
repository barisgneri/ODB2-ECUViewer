package com.baris.feature.gauge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baris.core.model.ObdConnectionState
import com.baris.core.model.ObdDataType
import com.baris.core.obd.domain.ConnectAndStartMetricsUseCase
import com.baris.core.obd.domain.GetAvailableDevicesUseCase
import com.baris.core.obd.domain.GetBluetoothStateUseCase
import com.baris.core.obd.domain.GetConnectionStateUseCase
import com.baris.core.obd.domain.GetLiveMetricsUseCase
import com.baris.core.obd.domain.GetPermissionStateUseCase
import com.baris.core.obd.domain.StopMetricsUseCase
import com.baris.feature.gauge.DashboardContract.UiAction
import com.baris.feature.gauge.DashboardContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getLiveMetricsUseCase: GetLiveMetricsUseCase,
    private val getConnectionStateUseCase: GetConnectionStateUseCase,
    private val getBluetoothStateUseCase: GetBluetoothStateUseCase,
    private val getPermissionStateUseCase: GetPermissionStateUseCase,
    private val getAvailableDevicesUseCase: GetAvailableDevicesUseCase,
    private val connectAndStartMetricsUseCase: ConnectAndStartMetricsUseCase,
    private val stopMetricsUseCase: StopMetricsUseCase
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<UiState> = combine(
        getConnectionStateUseCase(),
        getLiveMetricsUseCase(),
        getBluetoothStateUseCase(),
        getPermissionStateUseCase(),
        refreshTrigger
    ) { connectionState, liveData, isBluetoothEnabled, hasPermissions, _ ->
        when {
            !hasPermissions -> UiState.PermissionRequired
            connectionState is ObdConnectionState.Connected -> UiState.Connected(metrics = liveData)
            connectionState is ObdConnectionState.Reconnecting -> UiState.Connected(
                metrics = liveData,
                isReconnecting = true
            )
            connectionState is ObdConnectionState.Connecting -> UiState.Loading
            connectionState is ObdConnectionState.Error -> UiState.Error(connectionState.message)
            else -> UiState.Disconnected(
                pairedDevices = if (isBluetoothEnabled) getAvailableDevicesUseCase() else emptyList(),
                isBluetoothEnabled = isBluetoothEnabled
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.Connect -> connectToDevice(action.deviceAddress)
            is UiAction.Disconnect -> stopMetrics()
            is UiAction.RefreshDevices -> {
                refreshTrigger.tryEmit(Unit)
            }
        }
    }

    private fun connectToDevice(macAddress: String) {
        viewModelScope.launch {
            connectAndStartMetricsUseCase(macAddress)
        }
    }

    private fun stopMetrics() {
        viewModelScope.launch {
            stopMetricsUseCase()
        }
    }
}
