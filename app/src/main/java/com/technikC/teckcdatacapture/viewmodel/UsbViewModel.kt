package com.technikC.teckcdatacapture.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class UsbViewModel(application: Application) : AndroidViewModel(application) {

    private val usbManager: UsbManager = application.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbSerialDevice: UsbSerialDevice? = null
    private var connection: UsbDeviceConnection? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentReading = MutableStateFlow<String?>(null)
    val currentReading: StateFlow<String?> = _currentReading.asStateFlow()

    private val ACTION_USB_PERMISSION = "com.technikC.teckcdatacapture.USB_PERMISSION"

    private val dataBuffer = StringBuilder()

    private val TAG = "DataParser"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            connectToDevice(this)
                        }
                    } else {
                        _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                application,
                usbReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(usbReceiver)
        disconnect()
    }

    fun startConnection() {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) return

        _connectionStatus.value = ConnectionStatus.CONNECTING
        val usbManager = getApplication<Application>().getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = usbManager.getDeviceList().values.toList()
        if (availableDrivers.isNotEmpty()) {
            val device = availableDrivers[0]
            if (usbManager.hasPermission(device)) {
                 connectToDevice(device)
            } else {
                val permissionIntent = PendingIntent.getBroadcast(
                    getApplication(), 
                    0, 
                    Intent(ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE
                )
                usbManager.requestPermission(device, permissionIntent)
            }
        } else {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    private fun connectToDevice(device: UsbDevice) {
        usbDevice = device
        connection = usbManager.openDevice(device)
        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)

        if (usbSerialDevice != null) {
            if (usbSerialDevice!!.open()) {
                usbSerialDevice!!.setBaudRate(9600)
                //usbSerialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                usbSerialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_7)
                usbSerialDevice!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                usbSerialDevice!!.setParity(UsbSerialInterface.PARITY_NONE)
                usbSerialDevice!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                
                usbSerialDevice!!.read(mCallback)
                _connectionStatus.value = ConnectionStatus.CONNECTED
            } else {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
        } else {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    fun disconnect() {
        usbSerialDevice?.close()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _currentReading.value = null
    }

    private val mCallback = object : UsbSerialInterface.UsbReadCallback {
        override fun onReceivedData(data: ByteArray?) {
            Log.d(TAG, "Inside onReceivedData")
            data?.let {
                val dataStr = String(it, Charsets.UTF_8).trim()
                Log.d(TAG, "Raw data received: $dataStr")
                dataBuffer.append(dataStr)
                while (true) {
                    val index = dataBuffer.indexOfAny(charArrayOf('\r', '\n', 'q'))
                    Log.d(TAG, "Newline found at index: $index")
                    if (index == -1) break
                    var fullMeasurement = dataBuffer.substring(0, index).trim()
                    fullMeasurement = fullMeasurement.replace(Regex("[^\\d.-]"), "")
                    fullMeasurement = fullMeasurement.toFloat().toString()
                    Log.d(TAG, "Extracted measurement: '$fullMeasurement'")
                    dataBuffer.delete(0, index + 1)
                    if (fullMeasurement.isNotEmpty()) {
                        viewModelScope.launch {
                            _currentReading.value = fullMeasurement
                        }
                    }
                }
            }
        }
    }

    fun sendSTX(){
        if(_connectionStatus.value == ConnectionStatus.CONNECTED){
            val command = byteArrayOf(0x02)
            usbSerialDevice!!.write(command)
        }
    }

}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}
