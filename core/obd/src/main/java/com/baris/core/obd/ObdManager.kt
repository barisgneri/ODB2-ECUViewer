package com.baris.core.obd

import com.baris.core.model.ObdDataType
import kotlinx.coroutines.flow.StateFlow

interface ObdManager {
    val liveData: StateFlow<Map<ObdDataType, Float>>
    val isConnected: StateFlow<Boolean>

    suspend fun connect(deviceAddress: String): Boolean
    suspend fun startReading(dataTypes: Set<ObdDataType>)
    suspend fun stopReading()
}