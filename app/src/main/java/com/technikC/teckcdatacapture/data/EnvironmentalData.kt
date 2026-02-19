package com.technikC.teckcdatacapture.data

data class EnvironmentalData(
    val srfId: Int,
    val instrumentId: Int,
    val temperature: Double,
    val humidity: Double,
    val timestamp: Long = System.currentTimeMillis()
)
