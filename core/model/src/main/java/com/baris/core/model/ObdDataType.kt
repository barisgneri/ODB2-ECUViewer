package com.baris.core.model

sealed class ObdDataType(
    val id: String,
    val pid: String,
    val label: String,
    val maxValue: Float,
    val unit: String
) {
    object Rpm : ObdDataType(
        id = "ENGINE_RPM",
        pid = "01 0C",
        label = "RPM",
        maxValue = 7000f,
        unit = "rpm"
    )

    object Speed : ObdDataType(
        id = "VEHICLE_SPEED",
        pid = "01 0D",
        label = "Hız",
        maxValue = 260f,
        unit = "km/h"
    )

    object CoolantTemperature : ObdDataType(
        id = "COOLANT_TEMP",
        pid = "01 05",
        label = "Motor Sıcaklığı",
        maxValue = 150f,
        unit = "°C"
    )

}