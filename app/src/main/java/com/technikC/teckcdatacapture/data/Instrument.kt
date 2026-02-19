package com.technikC.teckcdatacapture.data

data class Instrument(
    val id: Int,
    val name: String,
    val number: String,
    val associatedSrfIds: List<Int>
)
