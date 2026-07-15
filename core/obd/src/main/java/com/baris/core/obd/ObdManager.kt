package com.baris.core.obd

import com.baris.core.model.ObdConnectionState
import com.baris.core.model.ObdDataType
import kotlinx.coroutines.flow.StateFlow

interface ObdManager {
    val liveData: StateFlow<Map<ObdDataType, Float>>
    val connectionState: StateFlow<ObdConnectionState>
    val isBluetoothEnabled: StateFlow<Boolean>
    val hasPermissions: StateFlow<Boolean>

    suspend fun connect(deviceAddress: String): Boolean
    suspend fun startReading(dataTypes: Set<ObdDataType>)
    suspend fun stopReading()

    fun getAvailableDevices(): List<Pair<String, String>>
    fun refreshPermissionStatus()
}