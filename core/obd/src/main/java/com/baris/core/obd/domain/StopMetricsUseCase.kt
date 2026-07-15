package com.baris.core.obd.domain

import com.baris.core.obd.ObdManager
import javax.inject.Inject

class StopMetricsUseCase @Inject constructor(
    private val obdManager: ObdManager
) {
    suspend operator fun invoke() {
        obdManager.stopReading()
    }
}
