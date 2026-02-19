package com.technikC.teckcdatacapture.data

sealed class MeasurementSheet {
    abstract val readings: MutableMap<String, Double>
    abstract val typeName: String

    data class PlainPlugGauge(
        override val readings: MutableMap<String, Double> = mutableMapOf()
    ) : MeasurementSheet() {
        override val typeName: String = "Plain Plug Gauge"
    }

    data class ThreadPlugGauge(
        override val readings: MutableMap<String, Double> = mutableMapOf()
    ) : MeasurementSheet() {
        override val typeName: String = "Thread Plug Gauge"
    }
}
