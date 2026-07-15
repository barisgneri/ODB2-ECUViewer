package com.baris.core.model

sealed interface ObdConnectionState {
    data object Disconnected : ObdConnectionState
    data object Connecting : ObdConnectionState
    data object Reconnecting : ObdConnectionState
    data object Connected : ObdConnectionState
    data class Error(val message: String) : ObdConnectionState
}