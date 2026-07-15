package com.baris.core.obd.domain

import com.baris.core.model.ObdDataType
import javax.inject.Inject

class SaveActiveGaugesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(gauges: Set<ObdDataType>) {
        settingsRepository.saveActiveGauges(gauges)
    }
}
