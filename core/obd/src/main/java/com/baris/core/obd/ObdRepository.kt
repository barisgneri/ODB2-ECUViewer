package com.baris.core.obd

import com.baris.core.model.ObdDataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ObdRepository(private val connectionSource: ObdConnectionSource) {

    // Ekranda o an hangi göstergelerin aktif olduğunu tutan dinamik set
    private val _activeDataTypes = MutableStateFlow<Set<ObdDataType>>(setOf(ObdDataType.Rpm))

    // UI katmanının (Compose) dinleyeceği anlık veriler haritası (Map)
    private val _liveData = MutableStateFlow<Map<ObdDataType, Float>>(emptyMap())
    val liveData = _liveData.asStateFlow()

    // Bağlantı durumunu yukarıya (UI'a) paslar
    val connectionState = connectionSource.connectionState

    suspend fun connect(): Boolean = connectionSource.connect()

    suspend fun disconnect() = connectionSource.disconnect()

    // Verileri sürekli sorgulayan (Polling) döngü fonksiyonu
    suspend fun startPolling() = withContext(Dispatchers.IO) {
        while (true) {
            // Eğer cihaz bağlıysa sırayla aktif göstergeleri sorgula
            if (connectionSource.connectionState.value.toString() == "Connected") { // Basit kontrol
                _activeDataTypes.value.forEach { dataType ->
                    try {
                        // 1. Ham veriyi gönder ve cevabı al
                        val rawResponse = connectionSource.sendCommand("${dataType.pid}\r")

                        // 2. Parser motorunu kullanarak veriyi sayıya çevir
                        val processedValue = ObdResponseParser.parse(dataType, rawResponse)

                        // 3. Canlı veri haritasını güncelle
                        val currentMap = _liveData.value.toMutableMap()
                        currentMap[dataType] = processedValue
                        _liveData.value = currentMap
                    } catch (e: Exception) {
                        // Hata durumunda döngünün patlamaması için loglayıp devam ediyoruz
                    }
                }
            }
            delay(100) // Arabayı ve Bluetooth hattını yormamak için 100ms bekleme süresi
        }
    }

    // Kullanıcı ekrandaki kadranları düzenlediğinde (örn: hız kadranını kaldırdığında) çağrılır
    fun updateActiveIndicators(newSet: Set<ObdDataType>) {
        _activeDataTypes.value = newSet
    }
}