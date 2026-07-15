package com.baris.core.obd

interface PermissionChecker {
    fun hasBluetoothPermissions(): Boolean
}
