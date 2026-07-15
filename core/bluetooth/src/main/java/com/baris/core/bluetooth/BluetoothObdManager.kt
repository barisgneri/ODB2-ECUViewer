package com.baris.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.baris.core.model.ObdConnectionState
import com.baris.core.model.ObdDataType
import com.baris.core.obd.ObdManager
import com.baris.core.obd.ObdResponseParser
import com.baris.core.obd.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class BluetoothObdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: PermissionChecker
) : ObdManager {

    //evrensel obd 2 uid
    private val obdUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isReading = false
    private var lastDeviceAddress: String? = null
    private var activeDataTypes: Set<ObdDataType> = emptySet()

    private val _connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    override val connectionState: StateFlow<ObdConnectionState> = _connectionState.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _hasPermissions = MutableStateFlow(permissionChecker.hasBluetoothPermissions())
    override val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _liveData = MutableStateFlow<Map<ObdDataType, Float>>(emptyMap())
    override val liveData: StateFlow<Map<ObdDataType, Float>> = _liveData.asStateFlow()

    init {
        updateBluetoothState()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    updateBluetoothState()
                }
            }
        }, filter)
    }

    private fun updateBluetoothState() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        _isBluetoothEnabled.value = adapter?.isEnabled == true
        refreshPermissionStatus()
    }

    override fun refreshPermissionStatus() {
        _hasPermissions.value = permissionChecker.hasBluetoothPermissions()
    }

    override fun getAvailableDevices(): List<Pair<String, String>> {
        if (!permissionChecker.hasBluetoothPermissions()) return emptyList()

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return emptyList()

        if (!adapter.isEnabled) return emptyList()

        return adapter.bondedDevices.map { device ->
            device.name to device.address
        }
    }

    override suspend fun connect(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        if (!permissionChecker.hasBluetoothPermissions()) {
            _connectionState.value = ObdConnectionState.Error("Permissions missing")
            return@withContext false
        }

        lastDeviceAddress = deviceAddress
        _connectionState.value = ObdConnectionState.Connecting

        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return@withContext false

            if (!adapter.isEnabled) {
                _connectionState.value = ObdConnectionState.Error("Bluetooth is disabled")
                return@withContext false
            }

            val remoteDevice = adapter.getRemoteDevice(deviceAddress) ?: return@withContext false

            bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(obdUuid)
            bluetoothSocket?.connect()

            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream

            sendRawCommand("AT Z\r")
            delay(500)
            sendRawCommand("AT SP 0\r")
            delay(500)

            _connectionState.value = ObdConnectionState.Connected
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = ObdConnectionState.Error(e.message ?: "Connection failed")
            disconnect()
            return@withContext false
        }
    }

    override suspend fun startReading(dataTypes: Set<ObdDataType>) = withContext(Dispatchers.IO) {
        if (_connectionState.value != ObdConnectionState.Connected) return@withContext
        activeDataTypes = dataTypes
        isReading = true

        while (isReading) {
            if (_connectionState.value != ObdConnectionState.Connected) {
                // If not connected, attempt reconnection
                if (lastDeviceAddress != null) {
                    _connectionState.value = ObdConnectionState.Reconnecting
                    val reconnected = connect(lastDeviceAddress!!)
                    if (!reconnected) {
                        delay(2000) // Wait before next retry
                        continue
                    }
                } else {
                    break
                }
            }

            activeDataTypes.forEach { dataType ->
                if (!isReading) return@forEach
                try {
                    val rawResponse = sendRawCommand("${dataType.pid}\r")
                    val parsedValue = ObdResponseParser.parse(dataType, rawResponse)

                    val currentMap = _liveData.value.toMutableMap()
                    currentMap[dataType] = parsedValue
                    _liveData.value = currentMap

                } catch (e: Exception) {
                    e.printStackTrace()
                    _connectionState.value = ObdConnectionState.Disconnected
                    // Break inner loop to trigger reconnection in outer loop
                    return@forEach 
                }
                delay(100)
            }
        }
    }

    override suspend fun stopReading() {
        isReading = false
    }

    private fun sendRawCommand(command: String): String {
        val out = outputStream ?: return ""
        val input = inputStream ?: return ""

        out.write(command.toByteArray(Charsets.US_ASCII))
        out.flush()

        val buffer = StringBuilder()
        var char: Char
        while (true) {
            val byteRead = input.read()
            if (byteRead == -1) break
            char = byteRead.toChar()
            buffer.append(char)
            if (char == '>') {
                break
            }
        }
        return buffer.toString()
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        stopReading()
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bluetoothSocket = null
            inputStream = null
            outputStream = null
            if (_connectionState.value != ObdConnectionState.Reconnecting) {
                _connectionState.value = ObdConnectionState.Disconnected
            }
        }
    }
}