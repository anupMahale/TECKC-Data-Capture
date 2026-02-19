package com.technikC.teckcdatacapture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.technikC.teckcdatacapture.ui.theme.TeckCDataCaptureTheme
import com.technikC.teckcdatacapture.ui.login.LoginScreen
import com.technikC.teckcdatacapture.ui.dashboard.DashboardScreen
import com.technikC.teckcdatacapture.ui.srf.SrfListScreen
import com.technikC.teckcdatacapture.ui.instrument.InstrumentListScreen
import com.technikC.teckcdatacapture.ui.capture.EnvironmentalCaptureScreen
import com.technikC.teckcdatacapture.ui.capture.DataCaptureScreen
import com.technikC.teckcdatacapture.ui.srf.SrfDetailsScreen

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.toMutableStateList

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import com.technikC.teckcdatacapture.ui.capture.DataCaptureScreenAuto

sealed class Screen : Parcelable {
    data object Dashboard : Screen() {
        override fun writeToParcel(dest: Parcel, flags: Int) {}
        override fun describeContents(): Int = 0
        @JvmField val CREATOR = object : Parcelable.Creator<Dashboard> {
            override fun createFromParcel(source: Parcel): Dashboard = Dashboard
            override fun newArray(size: Int): Array<Dashboard?> = arrayOfNulls(size)
        }
    }

    data class SrfList(val instrumentId: Int? = null) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readValue(Int::class.java.classLoader) as? Int)
        override fun writeToParcel(parcel: Parcel, flags: Int) { parcel.writeValue(instrumentId) }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<SrfList> {
            override fun createFromParcel(parcel: Parcel) = SrfList(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SrfList>(size)
        }
    }

    data class InstrumentList(val srfId: Int? = null) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readValue(Int::class.java.classLoader) as? Int)
        override fun writeToParcel(parcel: Parcel, flags: Int) { parcel.writeValue(srfId) }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<InstrumentList> {
            override fun createFromParcel(parcel: Parcel) = InstrumentList(parcel)
            override fun newArray(size: Int) = arrayOfNulls<InstrumentList>(size)
        }
    }

    data class EnvironmentalCapture(val srfId: Int, val instrumentId: Int) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(srfId)
            parcel.writeInt(instrumentId)
        }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<EnvironmentalCapture> {
            override fun createFromParcel(parcel: Parcel) = EnvironmentalCapture(parcel)
            override fun newArray(size: Int) = arrayOfNulls<EnvironmentalCapture>(size)
        }
    }

    data class DataCapture(val srfId: Int, val instrumentId: Int) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(srfId)
            parcel.writeInt(instrumentId)
        }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<DataCapture> {
            override fun createFromParcel(parcel: Parcel) = DataCapture(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DataCapture>(size)
        }
    }

    data class DataCaptureAuto(val srfId: Int, val instrumentId: Int) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(srfId)
            parcel.writeInt(instrumentId)
        }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<DataCapture> {
            override fun createFromParcel(parcel: Parcel) = DataCapture(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DataCapture>(size)
        }
    }

    data class SrfDetails(val srfId: Int, val instrumentId: Int) : Screen() {
        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(srfId)
            parcel.writeInt(instrumentId)
        }
        override fun describeContents() = 0
        companion object CREATOR : Parcelable.Creator<SrfDetails> {
            override fun createFromParcel(parcel: Parcel) = SrfDetails(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SrfDetails>(size)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeckCDataCaptureTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
                    
                    // Back stack to manage navigation history, persisted across rotation
                    val backStack = rememberSaveable(
                        saver = listSaver<MutableList<Screen>, Screen>(
                            save = { it.toList() },
                            restore = { it.toMutableStateList() }
                        )
                    ) { 
                        mutableStateListOf<Screen>(Screen.Dashboard) 
                    }

                    // Helper to push a new screen
                    fun navigateTo(screen: Screen) {
                        backStack.add(screen)
                    }

                    // Helper to pop the last screen
                    fun navigateBack() {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }

                    // Handle system back button
                    BackHandler(enabled = isLoggedIn && backStack.size > 1) {
                        navigateBack()
                    }

                    if (isLoggedIn) {
                        // Display the top screen in the stack
                        when (val screen = backStack.last()) {
                            Screen.Dashboard -> DashboardScreen(
                                onSrfClick = { navigateTo(Screen.SrfList()) },
                                onInstrumentsClick = { navigateTo(Screen.InstrumentList()) }
                            )
                            is Screen.SrfList -> SrfListScreen(
                                instrumentIdFilter = screen.instrumentId,
                                onBackClick = { navigateBack() },
                                onInstrumentClick = { navigateTo(Screen.InstrumentList()) },
                                onCardClick = { srfId -> 
                                    if (screen.instrumentId != null) {
                                        navigateTo(Screen.SrfDetails(srfId = srfId, instrumentId = screen.instrumentId))
                                    } else {
                                        navigateTo(Screen.InstrumentList(srfId = srfId)) 
                                    }
                                }
                            )
                            is Screen.InstrumentList -> InstrumentListScreen(
                                srfIdFilter = screen.srfId,
                                onBackClick = { navigateBack() },
                                onSrfClick = { navigateTo(Screen.SrfList()) },
                                onCardClick = { instrumentId -> 
                                    if (screen.srfId != null) {
                                        navigateTo(Screen.SrfDetails(srfId = screen.srfId, instrumentId = instrumentId))
                                    } else {
                                        navigateTo(Screen.SrfList(instrumentId = instrumentId))
                                    }
                                }
                            )
                            is Screen.EnvironmentalCapture -> EnvironmentalCaptureScreen(
                                srfId = screen.srfId,
                                instrumentId = screen.instrumentId,
                                onBackClick = { navigateBack() },
                                onSubmit = { temp, humidity ->
                                    println("Submitted: Temp=$temp, Humidity=$humidity")
                                    // Navigate to Data Capture after Env Capture
                                    navigateTo(Screen.DataCapture(srfId = screen.srfId, instrumentId = screen.instrumentId))
                                },
                                onAutoSubmit = { temp, humidity ->
                                    println("Submitted: Temp=$temp, Humidity=$humidity")
                                    // Navigate to Data Capture after Env Capture
                                    navigateTo(Screen.DataCaptureAuto(srfId = screen.srfId, instrumentId = screen.instrumentId))
                                }
                            )
                            is Screen.DataCapture -> DataCaptureScreen(
                                srfId = screen.srfId,
                                instrumentId = screen.instrumentId,
                                onBackClick = { navigateBack() },
                                onSaveSession = { job ->
                                    println("Saving Job: $job")
                                    // Navigate back to Dashboard after saving
                                    while(backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    }
                                }
                            )
                            is Screen.DataCaptureAuto -> DataCaptureScreenAuto(
                                srfId = screen.srfId,
                                instrumentId = screen.instrumentId,
                                onBackClick = { navigateBack() },
                                onSaveSession = { job ->
                                    println("Saving Job: $job")
                                    // Navigate back to Dashboard after saving
                                    while(backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    }
                                }
                            )
                            is Screen.SrfDetails -> SrfDetailsScreen(
                                srfId = screen.srfId,
                                instrumentId = screen.instrumentId,
                                onBackClick = { navigateBack() },
                                onStartWorkflow = { srfId, instrumentId ->
                                    navigateTo(Screen.EnvironmentalCapture(srfId, instrumentId))
                                }
                            )
                        }
                    } else {
                        LoginScreen(onLoginClick = { isLoggedIn = true })
                    }
                }
            }
        }
    }
}
