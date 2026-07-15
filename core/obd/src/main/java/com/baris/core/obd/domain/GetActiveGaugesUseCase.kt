package com.baris.core.obd.domain

import com.baris.core.model.ObdDataType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveGaugesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Set<ObdDataType>> {
        return settingsRepository.activeGauges
    }
}
