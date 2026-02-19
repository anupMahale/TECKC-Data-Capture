package com.technikC.teckcdatacapture.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.technikC.teckcdatacapture.data.EnvironmentalData

class EnvironmentalViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("env_prefs", Context.MODE_PRIVATE)

    // Using Float for SharedPreferences as it doesn't support Double directly
    // Default values: 24.5 for temperature, 45.0 for humidity
    private val _temperature = MutableStateFlow(sharedPreferences.getFloat("temperature", 24.5f).toDouble())
    val temperature: StateFlow<Double> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow(sharedPreferences.getFloat("humidity", 45.0f).toDouble())
    val humidity: StateFlow<Double> = _humidity.asStateFlow()

    private val _history = MutableStateFlow<List<EnvironmentalData>>(emptyList())
    val history: StateFlow<List<EnvironmentalData>> = _history.asStateFlow()

    fun updateTemperature(value: Double) {
        _temperature.value = value
    }

    fun updateHumidity(value: Double) {
        _humidity.value = value
    }

    fun saveValues(srfId: Int, instrumentId: Int) {
        val currentTemp = _temperature.value
        val currentHum = _humidity.value
        
        // Save to SharedPreferences (Last Known)
        sharedPreferences.edit()
            .putFloat("temperature", currentTemp.toFloat())
            .putFloat("humidity", currentHum.toFloat())
            .apply()
            
        // Add to history
        val newData = EnvironmentalData(
            srfId = srfId,
            instrumentId = instrumentId,
            temperature = currentTemp,
            humidity = currentHum,
            timestamp = System.currentTimeMillis()
        )
        
        // Keep only last 3
        val currentList = _history.value.toMutableList()
        currentList.add(newData)
        if (currentList.size > 3) {
            currentList.removeAt(0)
        }
        _history.value = currentList
    }
}
