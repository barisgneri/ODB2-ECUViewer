package com.baris.feature.gauge

import com.baris.core.model.ObdDataType

object DashboardContract {

    sealed interface UiState {
        data object Loading : UiState

        data object PermissionRequired : UiState
        
        data class Disconnected(
            val pairedDevices: List<Pair<String, String>> = emptyList(),
            val isBluetoothEnabled: Boolean = true
        ) : UiState

        data class Connected(
            val metrics: Map<ObdDataType, Float> = emptyMap(),
            val isReconnecting: Boolean = false
        ) : UiState
        
        data class Error(val message: String) : UiState
    }

    sealed interface UiAction {
        data class Connect(val deviceAddress: String) : UiAction
        data object Disconnect : UiAction
        data object RefreshDevices : UiAction
    }

    sealed interface UiEffect {
        data class ShowToast(val message: String) : UiEffect
    }
}
