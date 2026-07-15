package com.baris.core.obd.domain

import com.baris.core.model.ObdDataType
import com.baris.core.obd.ObdManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetLiveMetricsUseCase @Inject constructor(
    private val obdManager: ObdManager
) {
    operator fun invoke(): StateFlow<Map<ObdDataType, Float>> = obdManager.liveData
}
