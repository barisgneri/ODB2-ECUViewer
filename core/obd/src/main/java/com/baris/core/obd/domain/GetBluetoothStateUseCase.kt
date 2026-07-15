package com.baris.core.obd.domain

import com.baris.core.obd.ObdManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetBluetoothStateUseCase @Inject constructor(
    private val obdManager: ObdManager
) {
    operator fun invoke(): StateFlow<Boolean> = obdManager.isBluetoothEnabled
}
