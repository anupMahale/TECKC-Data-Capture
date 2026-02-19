package com.technikC.teckcdatacapture.util

import com.technikC.teckcdatacapture.data.ParametersPlainPlugGauge
import com.technikC.teckcdatacapture.data.ParametersThreadPlugGauge
import java.util.Locale
import kotlin.math.abs

object Calculator {

    data class CalculationResult(
        val value: Double,
        val deviation: Double,
        val isWithinRange: Boolean,
        val displayString: String
    )

    // Mock parameters until we have a real data source fetching based on SRF ID
    private val plainParams = ParametersPlainPlugGauge(
        nominalValueGo = 10.000f, rangeGo = 0.010f, wearGo = 0.005f,
        nominalValueNoGo = 10.020f, rangeNoGo = 0.010f, wearNoGo = 0.000f
    )

    private val threadParams = ParametersThreadPlugGauge(
        majorDiaNominalValueGo = 12.000f, majorDiaRangeGo = 0.02f,
        effectiveDiaNominalValueGo = 11.500f, effectiveDiaRangeGo = 0.02f, wearGo = 0.005f,
        majorDiaNominalValueNoGo = 11.800f, majorDiaRangeNoGo = 0.02f,
        effectiveDiaNominalValueNoGo = 11.600f, effectiveDiaRangeNoGo = 0.02f, wearNoGo = 0.0f
    )

    fun calculate(srfId: Int, reading: Double, key: String, gaugeType: String): CalculationResult {
        // 1. Identify context from key
        // Keys: "Side-Plane-Position" e.g., "Go-A-A-Top", "Go-A-A-Major_1"
        
        val isGo = key.startsWith("Go")
        
        var nominal = 0.0
        var range = 0.0

        if (gaugeType == "Plain Plug Gauge") {
            if (isGo) {
                nominal = plainParams.nominalValueGo.toDouble()
                range = plainParams.rangeGo.toDouble()
            } else {
                nominal = plainParams.nominalValueNoGo.toDouble()
                range = plainParams.rangeNoGo.toDouble()
            }
        } else {
            // Thread Plug Gauge
            val isMajor = key.contains("Major")
            val isEffective = key.contains("Eff")
            
            if (isGo) {
                if (isMajor) {
                    nominal = threadParams.majorDiaNominalValueGo.toDouble()
                    range = threadParams.majorDiaRangeGo.toDouble()
                } else if (isEffective) {
                    nominal = threadParams.effectiveDiaNominalValueGo.toDouble()
                    range = threadParams.effectiveDiaRangeGo.toDouble()
                }
            } else {
                // No Go
                if (isMajor) {
                    nominal = threadParams.majorDiaNominalValueNoGo.toDouble()
                    range = threadParams.majorDiaRangeNoGo.toDouble()
                } else if (isEffective) {
                    nominal = threadParams.effectiveDiaNominalValueNoGo.toDouble()
                    range = threadParams.effectiveDiaRangeNoGo.toDouble()
                }
            }
        }

        // 2. Perform Mock Calculation (Just passing raw reading for now)
        val result = reading-0.02

        // 3. Calculate Deviation
        val lowerBound = nominal - range // Assuming range is +/- logic? Or total tolerance? 
        // "range" typically means tolerance band or max-min. 
        // Based on typical gauge specs, "range" often implies the tolerance. 
        // Context says "nominalValueGo = 10.000", "rangeGo = 0.010". Usually implies +/- or +T.
        // Let's assume symmetric +/- range for this demo unless specified.
        // User said: "0 if it is within the nominal range else the number by which it is beyond the range"
        
        var diff = 0.0
        // Assuming range is a single value, e.g. 0.010 -> nominal +/- (range/2) OR nominal + range?
        // Given existing code uses "nominalValue" and "range", let's assume range is the total tolerance? OR +/- tolerance?
        // Let's assume it's the half-tolerance (+/-) for simplicity in demo.
        
        val margin = range // Treating 'range' as the tolerance value (+/- this value)
        
        if (result > nominal + margin) {
            diff = result - (nominal + margin)
        } else if (result < nominal - margin) {
            diff = result - (nominal - margin)
        } else {
            diff = 0.0
        }

        val isWithinRange = diff == 0.0
        
        // Format: "10.005 (+0.005)" or "10.000 (0)"
        val diffString = if (diff == 0.0) "0" else String.format(Locale.getDefault(), "%+.3f", diff)
        var displayString = String()
        if(isWithinRange){
            displayString = String.format(Locale.getDefault(), "%.3f", result)
        }else{
            displayString = String.format(Locale.getDefault(), "%.3f (%s)", result, diffString)
        }
        //val displayString = String.format(Locale.getDefault(), "%.3f (%s)", result, diffString)

        return CalculationResult(result, diff, isWithinRange, displayString)
    }
}
