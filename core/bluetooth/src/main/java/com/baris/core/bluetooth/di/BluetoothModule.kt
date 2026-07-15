package com.baris.core.bluetooth.di

import com.baris.core.bluetooth.AndroidPermissionChecker
import com.baris.core.bluetooth.BluetoothObdManager
import com.baris.core.bluetooth.data.DataStoreSettingsRepository
import com.baris.core.obd.ObdManager
import com.baris.core.obd.PermissionChecker
import com.baris.core.obd.domain.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    @Singleton
    abstract fun bindObdManager(
        bluetoothObdManager: BluetoothObdManager
    ): ObdManager

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(
        androidPermissionChecker: AndroidPermissionChecker
    ): PermissionChecker

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        dataStoreSettingsRepository: DataStoreSettingsRepository
    ): SettingsRepository
}
