package com.baris.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.baris.core.model.ObdDataType
import com.baris.core.obd.ObdManager
import com.baris.core.obd.ObdResponseParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothObdManager(private val context: Context) : ObdManager {

    //evrensel obd 2 uid
    private val obdUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isReading = false

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _liveData = MutableStateFlow<Map<ObdDataType, Float>>(emptyMap())
    override val liveData: StateFlow<Map<ObdDataType, Float>> = _liveData.asStateFlow()

    fun getPairedDevices(): List<Pair<String, String>> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return emptyList()

        if (!adapter.isEnabled) return emptyList()

        return adapter.bondedDevices.map { device ->
            device.name to device.address
        }
    }

    override suspend fun connect(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return@withContext false

            if (!adapter.isEnabled) return@withContext false

            val remoteDevice = adapter.getRemoteDevice(deviceAddress) ?: return@withContext false

            bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(obdUuid)
            bluetoothSocket?.connect()

            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream

            sendRawCommand("AT Z\r")
            delay(500)
            sendRawCommand("AT SP 0\r")
            delay(500)

            _isConnected.value = true
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
            return@withContext false
        }
    }

    override suspend fun startReading(dataTypes: Set<ObdDataType>) = withContext(Dispatchers.IO) {
        if (!_isConnected.value) return@withContext
        isReading = true

        while (isReading) {
            dataTypes.forEach { dataType ->
                if (!isReading) return@forEach
                try {
                    val rawResponse = sendRawCommand("${dataType.pid}\r")
                    val parsedValue = ObdResponseParser.parse(dataType, rawResponse)

                    val currentMap = _liveData.value.toMutableMap()
                    currentMap[dataType] = parsedValue
                    _liveData.value = currentMap

                } catch (e: Exception) {
                    e.printStackTrace()
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
            _isConnected.value = false
        }
    }
}