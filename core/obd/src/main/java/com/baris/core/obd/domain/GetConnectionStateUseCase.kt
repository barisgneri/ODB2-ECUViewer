package com.baris.core.obd.domain

import com.baris.core.model.ObdConnectionState
import com.baris.core.obd.ObdManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetConnectionStateUseCase @Inject constructor(
    private val obdManager: ObdManager
) {
    operator fun invoke(): StateFlow<ObdConnectionState> = obdManager.connectionState
}
