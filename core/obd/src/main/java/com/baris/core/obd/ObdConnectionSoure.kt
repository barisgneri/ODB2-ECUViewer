package com.baris.core.obd

import com.baris.core.model.ObdConnectionState
import kotlinx.coroutines.flow.StateFlow

interface ObdConnectionSource {
    val connectionState: StateFlow<ObdConnectionState>

    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun sendCommand(command: String): String
}