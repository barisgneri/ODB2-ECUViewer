package com.baris.core.model

sealed interface ObdConnectionState {
    object Disconnected : ObdConnectionState
    object Connecting : ObdConnectionState
    object Connected : ObdConnectionState
    data class Error(val message: String) : ObdConnectionState
}