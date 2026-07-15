package com.baris.core.obd.domain

import com.baris.core.obd.ObdManager
import javax.inject.Inject

class GetAvailableDevicesUseCase @Inject constructor(
    private val obdManager: ObdManager
) {
    operator fun invoke(): List<Pair<String, String>> = obdManager.getAvailableDevices()
}
