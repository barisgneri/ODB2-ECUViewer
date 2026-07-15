package com.baris.core.obd.domain

import com.baris.core.model.ObdDataType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // UI'ın anlık olarak aktif kadranları dinleyebileceği Flow stream
    val activeGauges: Flow<Set<ObdDataType>>

    // Kullanıcı ekrandan kadran ekleyip çıkardığında tetiklenecek fonksiyon
    suspend fun saveActiveGauges(gauges: Set<ObdDataType>)
}
