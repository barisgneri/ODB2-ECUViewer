package com.baris.core.obd.domain

import com.baris.core.obd.ObdManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConnectAndStartMetricsUseCase @Inject constructor(
    private val obdManager: ObdManager,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(deviceAddress: String): Boolean {
        val isConnected = obdManager.connect(deviceAddress)
        if (isConnected) {
            // Kullanıcının diske kaydettiği güncel kadran setini ilk (first) değer olarak çekiyoruz
            val userSelectedGauges = settingsRepository.activeGauges.first()
            
            // Bluetooth döngüsüne bu dinamik seti paslıyoruz
            obdManager.startReading(userSelectedGauges)
        }
        return isConnected
    }
}
