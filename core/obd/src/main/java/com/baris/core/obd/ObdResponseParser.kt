package com.baris.core.obd

import com.baris.core.model.ObdDataType

object ObdResponseParser {

    fun parse(dataType: ObdDataType, rawResponse: String): Float {
        // Gelen verideki boşlukları, \r ve > karakterlerini temizle
        val cleanResponse = rawResponse.replace("\r", "").replace(">", "").replace(" ", "").trim()

        // Geçerlilik kontrolü: 01 0C (RPM) için cevap 41 0C ile başlamalıdır (01 + 40 = 41)
        val expectedHeader = dataType.pid.replace(" ", "").toInt(16) + 0x40
        val expectedHeaderHex = expectedHeader.toString(16).uppercase()

        if (!cleanResponse.uppercase().startsWith(expectedHeaderHex)) {
            return 0f // Hatalı veya geçersiz yanıt geldiyse güvenli değer dön
        }

        // String'i ikişerli gruplara (Byte'lara) ayır. Örn: "410C1AF8" -> ["41", "0C", "1A", "F8"]
        val bytes = cleanResponse.chunked(2)

        return when (dataType) {
            is ObdDataType.Rpm -> {
                // RPM Formülü: ((A * 256) + B) / 4
                val a = bytes.getOrNull(2)?.toInt(16) ?: 0
                val b = bytes.getOrNull(3)?.toInt(16) ?: 0
                ((a * 256) + b) / 4f
            }
            is ObdDataType.Speed -> {
                // Hız Formülü: A
                (bytes.getOrNull(2)?.toInt(16) ?: 0).toFloat()
            }
            is ObdDataType.CoolantTemperature -> {
                // Hararet Formülü: A - 40
                ((bytes.getOrNull(2)?.toInt(16) ?: 0) - 40).toFloat()
            }
        }
    }
}