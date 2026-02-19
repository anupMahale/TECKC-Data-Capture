package com.technikC.teckcdatacapture.data

import java.util.UUID

data class Job(
    val jobId: String = UUID.randomUUID().toString(),
    val srfId: Int,
    val instrumentId: Int,
    val calibrationDate: Long = System.currentTimeMillis(),
    val environmentalConditions: List<EnvironmentalData> = emptyList(), // Start, Middle, End
    val capturedValues: List<Double> = emptyList()
)
