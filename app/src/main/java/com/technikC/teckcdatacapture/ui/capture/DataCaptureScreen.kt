package com.technikC.teckcdatacapture.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.technikC.teckcdatacapture.data.MeasurementSheet
import com.technikC.teckcdatacapture.data.Job
import com.technikC.teckcdatacapture.viewmodel.ConnectionStatus
import com.technikC.teckcdatacapture.viewmodel.UsbViewModel
import androidx.compose.ui.window.Dialog
import com.technikC.teckcdatacapture.data.ParametersPlainPlugGauge
import com.technikC.teckcdatacapture.data.ParametersThreadPlugGauge
import com.technikC.teckcdatacapture.ui.srf.SrfDetailsScreen
import com.technikC.teckcdatacapture.ui.capture.EnvironmentalCaptureScreen
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import com.technikC.teckcdatacapture.util.Calculator
import com.technikC.teckcdatacapture.viewmodel.EnvironmentalViewModel
import com.technikC.teckcdatacapture.data.EnvironmentalData

data class CellData(
    val text: String,
    val color: Color = Color.White,
    val rawValue: Double? = null
)

@Composable
fun DataCaptureScreen(
    srfId: Int,
    instrumentId: Int,
    onBackClick: () -> Unit = {},
    onSaveSession: (Job) -> Unit = {},
    viewModel: UsbViewModel = viewModel(),
    envViewModel: EnvironmentalViewModel = viewModel()
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val currentReading by viewModel.currentReading.collectAsState()
    val envHistory by envViewModel.history.collectAsState()
    
    // UI Colors
    val darkBlue = Color(0xFF0D1B2A)
    val teal = Color(0xFF1B263B)
    val cardBackground = Color(0xFF2B3A42)
    val cyan = Color(0xFF00B4D8)
    val green = Color(0xFF4CAF50)
    val yellow = Color(0xFFFFC107)

    // State for selected gauge type
    var selectedGaugeType by remember { mutableStateOf<String>("Plain Plug Gauge") }
    var expanded by remember { mutableStateOf(false) }

    // State for selected cell
    var selectedCellKey by remember { mutableStateOf<String?>(null) }

    // State for captured values (Table Data)
    // We use a Map<String, CellData> to store cell values keyed by row-col identifier
    var capturedData by remember { mutableStateOf(mutableMapOf<String, CellData>()) }
    
    // History of raw readings
    var readingHistory by remember { mutableStateOf(listOf<Double>()) }

    // Dialog States
    var showSrfDialog by remember { mutableStateOf(false) }
    var showEnvDialog by remember { mutableStateOf(false) }
    var showPropsDialog by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(darkBlue, teal)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
             // Header
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Measurement Sheet",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SRF-$srfId • Instrument-$instrumentId",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.25f)) {
                        val (icon, color, text) = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Triple(Icons.Default.CheckCircle, green, "CONNECTED")
                            ConnectionStatus.CONNECTING -> Triple(Icons.Default.Usb, yellow, "CONNECTING...")
                            ConnectionStatus.DISCONNECTED -> Triple(Icons.Default.Warning, Color.Gray, "DISCONNECTED")
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = color)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(text = text, color = color, fontWeight = FontWeight.Bold)
                    }
                    Button(modifier = Modifier.weight(1f),
                        onClick = { 
                            if (connectionStatus == ConnectionStatus.CONNECTED) 
                                viewModel.disconnect() 
                            else 
                                viewModel.startConnection() 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (connectionStatus == ConnectionStatus.CONNECTED) Color.Red else cyan
                        )
                    ) {
                        Text(text = if (connectionStatus == ConnectionStatus.CONNECTED) "Disconnect" else "Connect")
                    }
                    Spacer(modifier = Modifier.weight(0.25f))
                    //Receive Button
                    Button(modifier = Modifier.weight(1f),
                        onClick = {
                            if (connectionStatus == ConnectionStatus.CONNECTED)
                                viewModel.sendSTX()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (connectionStatus == ConnectionStatus.CONNECTED) Color.Green else Color.Gray
                        )
                    ) {
                        Text(text = if (connectionStatus == ConnectionStatus.CONNECTED) "Receive" else "Receive")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Reading Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT READING",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentReading ?: "---",
                        style = MaterialTheme.typography.displayMedium,
                        color = cyan,
                        fontWeight = FontWeight.Bold
                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    if (currentReading != null) {
//                         Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = green, modifier = Modifier.size(16.dp))
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("Signal Stable", color = green, style = MaterialTheme.typography.bodySmall)
//                         }
//                    } else {
//                        Text("No Signal", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
//                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Gauge Type Selection
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedGaugeType, color = Color.White)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Plain Plug Gauge") },
                        onClick = { 
                            selectedGaugeType = "Plain Plug Gauge"
                            expanded = false
                            capturedData = mutableMapOf() // Reset data on type change
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Thread Plug Gauge") },
                        onClick = { 
                            selectedGaugeType = "Thread Plug Gauge"
                            expanded = false
                            capturedData = mutableMapOf()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Table Header and Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Measurements",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickAccessButton(text = "SRF", onClick = { showSrfDialog = true }, color = cyan)
                    QuickAccessButton(text = "T&H", onClick = { showEnvDialog = true }, color = cyan)
                    QuickAccessButton(text = "P", onClick = { showPropsDialog = true }, color = cyan)
               }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Dynamic Table
            if (selectedGaugeType == "Plain Plug Gauge") {
                PlainPlugGaugeTable(
                    data = capturedData,
                    onCellClick = { key ->
                        selectedCellKey = key
                        if (currentReading != null) { 
                            val readingVal = currentReading!!.toDoubleOrNull() ?: 0.0
                            val result = Calculator.calculate(srfId, readingVal, key, selectedGaugeType)
                            val newCellData = CellData(
                                text = result.displayString,
                                color = if (result.isWithinRange) green else Color.Red,
                                rawValue = readingVal
                            )
                            capturedData = capturedData.toMutableMap().apply { put(key, newCellData) }
                            
                            // History update
                            readingHistory = readingHistory + readingVal
                        }
                    }
                )
            } else {
                ThreadPlugGaugeTableNew(
                    data = capturedData,
                    onCellClick = { key ->
                        selectedCellKey = key
                        if (currentReading != null) {
                            val readingVal = currentReading!!.toDoubleOrNull() ?: 0.0
                            val result = Calculator.calculate(srfId, readingVal, key, selectedGaugeType)
                            val newCellData = CellData(
                                text = result.displayString,
                                color = if (result.isWithinRange) green else Color.Red,
                                rawValue = readingVal
                            )
                            capturedData = capturedData.toMutableMap().apply { put(key, newCellData) }
                            readingHistory = readingHistory + readingVal
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Session
            Button(
                onClick = { showSaveConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cyan),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            // Captured Readings History (Mini List)
            Text(
                text = "Reading History (${readingHistory.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                 LazyColumn(contentPadding = PaddingValues(8.dp)) {
                     items(readingHistory.size) { index ->
                         val reading = readingHistory[readingHistory.size - 1 - index] // Reverse order
                         Row(
                             modifier = Modifier.fillMaxWidth().padding(4.dp),
                             horizontalArrangement = Arrangement.SpaceBetween
                         ) {
                             Text("Reading #${readingHistory.size - index}", color = Color.Gray)
                             Text("$reading", color = Color.White, fontWeight = FontWeight.Bold)
                         }
                         HorizontalDivider(color = Color.Gray.copy(alpha=0.2f))
                     }
                 }
            }

            Spacer(modifier = Modifier.height(24.dp))


        }

        // Dialogs
        if (showSrfDialog) {
            Dialog(onDismissRequest = { showSrfDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f), // Dynamic height for landscape
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                   SrfDetailsScreen(
                       srfId = srfId,
                       instrumentId = instrumentId,
                       onBackClick = { showSrfDialog = false },
                       onStartWorkflow = { _, _ -> showSrfDialog = false }, // Just close on action
                       showStartButton = false
                   )
                }
            }
        }

        if (showEnvDialog) {
            Dialog(onDismissRequest = { showEnvDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    EnvironmentalCaptureScreen(
                        srfId = srfId,
                        instrumentId = instrumentId,
                        onBackClick = { showEnvDialog = false },
                        onSubmit = { _, _ -> showEnvDialog = false } 
                    )
                }
            }
        }

        if (showPropsDialog) {
             Dialog(onDismissRequest = { showPropsDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gauge Properties",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        PropertiesTable(type = selectedGaugeType)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPropsDialog = false },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = cyan)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
        
        if (showSaveConfirmation) {
            SaveConfirmationDialog(
                srfId = srfId,
                instrumentId = instrumentId,
                envHistory = envHistory,
                capturedData = capturedData,
                gaugeType = selectedGaugeType,
                onDismiss = { showSaveConfirmation = false },
                onConfirm = {
                    isSaving = true
                    // Mock API Call simulation
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isSaving = false
                        showSaveConfirmation = false
                        
                        // Construct Job
                        val job = Job(
                            // jobId generated by default
                            srfId = srfId,
                            instrumentId = instrumentId,
                            calibrationDate = System.currentTimeMillis(),
                            environmentalConditions = envHistory.takeLast(3),
                            capturedValues = getSortedCapturedValuesRaw(capturedData, selectedGaugeType)
                        )
                        
                        onSaveSession(job)
                    }, 2000)
                },
                isSaving = isSaving
            )
        }
    }
}

@Composable
fun PlainPlugGaugeTable(
    data: Map<String, CellData>,
    onCellClick: (String) -> Unit
) {
    val sides = listOf("Go", "No Go")
    val planes = listOf("A-A", "B-B")
    val positions = listOf("Top", "Bottom") // Excluding 'Plane' col for simplicity in values

    Column(modifier = Modifier.border(1.dp, Color.Gray)) {
        // Header
        //Row(modifier = Modifier.background(Color.DarkGray)) {
        Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
            TableCell(text = "Side", weight = 0.5f, isHeader = true)
            TableCell(text = "Plane", weight = 0.5f, isHeader = true)
            TableCell(text = "Top", weight = 1f, isHeader = true)
            TableCell(text = "Bottom", weight = 1f, isHeader = true)
        }

        
        sides.forEach { side ->
            planes.forEachIndexed { index, plane ->
                Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                    // Side cell only for first plane
                    if (index == 0) {
                         TableCell(text = side, weight = 0.5f, isHeader = true)//, modifier = Modifier.height(100.dp)) // Approximate height for spanning
                    } else {
                         // Placeholder for layout consistency if not using rowspan logic
                         // In Compose standard Layouts, rowspan needs custom implementation.
                         // For simplicity, we create a simplified grid or repeat label.
                         // Let's repeat label for MVP simplicity or leave empty.
                         TableCell(text = side, weight = 0.5f, isHeader = true)
                    }

                    TableCell(text = plane, weight = 0.5f, isHeader = true)

                    positions.forEach { pos ->
                        val key = "$side-$plane-$pos"
                        val cellData = data[key] ?: CellData("")
                        TableCell(
                            text = cellData.text,
                            textColor = cellData.color,
                            weight = 1f,
                            isClickable = true,
                            onClick = { onCellClick(key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadPlugGaugeTableNew(
    data: Map<String, CellData>,
    onCellClick: (String) -> Unit
) {
    val sides = listOf("Go", "No Go")
    val planes = listOf("A-A", "B-B")
    val positions = listOf("Major_1", "Major_2", "Eff._1", "Eff._2") // Excluding 'Plane' col for simplicity in values

    Column(modifier = Modifier.border(1.dp, Color.Gray)) {
        // Header
        //Row(modifier = Modifier.background(Color.DarkGray)) {
        Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
            TableCell(text = "Side", weight = 0.75f, isHeader = true)
            TableCell(text = "Plane", weight = 0.75f, isHeader = true)
            TableCell(text = "Major_1", weight = 1f, isHeader = true)
            TableCell(text = "Major_2", weight = 1f, isHeader = true)
            TableCell(text = "Eff._1", weight = 1f, isHeader = true)
            TableCell(text = "Eff._2", weight = 1f, isHeader = true)
        }


        sides.forEach { side ->
            planes.forEachIndexed { index, plane ->
                Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                    // Side cell only for first plane
                    if (index == 0) {
                        TableCell(text = side, weight = 0.75f, isHeader = true)//, modifier = Modifier.height(100.dp)) // Approximate height for spanning
                    } else {
                        // Placeholder for layout consistency if not using rowspan logic
                        // In Compose standard Layouts, rowspan needs custom implementation.
                        // For simplicity, we create a simplified grid or repeat label.
                        // Let's repeat label for MVP simplicity or leave empty.
                        TableCell(text = side, weight = 0.75f, isHeader = true)
                    }

                    TableCell(text = plane, weight = 0.75f, isHeader = true)

                    positions.forEach { pos ->
                        val key = "$side-$plane-$pos"
                        val cellData = data[key] ?: CellData("")
                        TableCell(
                            text = cellData.text,
                            textColor = cellData.color,
                            weight = 1f,
                            isClickable = true,
                            onClick = { onCellClick(key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadPlugGaugeTable(
    data: Map<String, CellData>,
    onCellClick: (String) -> Unit
) {
     val planes = listOf("A-A", "B-B")
     // Logic is slightly more complex with Go/No Go columns spanning. 
     // Implementing a simplified flat view for now.
     
     Column(modifier = Modifier.border(1.dp, Color.Gray)) {
         // Header Row 1
         Row(modifier = Modifier.background(Color.DarkGray)) {
             TableCell(text = "", weight = 1f, isHeader = true)
             TableCell(text = "Go", weight = 2f, isHeader = true)
             TableCell(text = "No Go", weight = 2f, isHeader = true)
         }
         // Header Row 2
         Row(modifier = Modifier.background(Color.DarkGray)) {
             TableCell(text = "Plane", weight = 1f, isHeader = true)
             TableCell(text = "Major", weight = 1f, isHeader = true)
             TableCell(text = "Eff.", weight = 1f, isHeader = true)
             TableCell(text = "Major", weight = 1f, isHeader = true)
             TableCell(text = "Eff.", weight = 1f, isHeader = true)
         }

         planes.forEach { plane ->
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = plane, weight = 1f, isHeader = true)
                 
                  // Go - Major
                  val k1 = "Go-$plane-Major"
                  val d1 = data[k1] ?: CellData("")
                  TableCell(text = d1.text, textColor = d1.color, weight = 1f, isClickable = true, onClick = { onCellClick(k1) })
                  
                  // Go - Effective
                  val k2 = "Go-$plane-Effective"
                  val d2 = data[k2] ?: CellData("")
                  TableCell(text = d2.text, textColor = d2.color, weight = 1f, isClickable = true, onClick = { onCellClick(k2) })
                  
                  // No Go - Major
                  val k3 = "NoGo-$plane-Major"
                  val d3 = data[k3] ?: CellData("")
                  TableCell(text = d3.text, textColor = d3.color, weight = 1f, isClickable = true, onClick = { onCellClick(k3) })
                  
                  // No Go - Effective
                  val k4 = "NoGo-$plane-Effective"
                  val d4 = data[k4] ?: CellData("")
                  TableCell(text = d4.text, textColor = d4.color, weight = 1f, isClickable = true, onClick = { onCellClick(k4) })
             }
         }
     }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    textColor: Color = Color.White,
    isHeader: Boolean = false,
    isClickable: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(weight)
            .height(50.dp)
            .background(if (isHeader) Color.Transparent else Color.Black.copy(alpha=0.3f))
            .border(0.5.dp, Color.DarkGray)
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isHeader) Color.Gray else textColor,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuickAccessButton(text: String, onClick: () -> Unit, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, androidx.compose.foundation.shape.CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PropertiesTable(type: String) {
    Column(modifier = Modifier.border(1.dp, Color.Gray)) {
         if (type == "Plain Plug Gauge") {
             // Mock Data for Demo
             val params = ParametersPlainPlugGauge(
                 nominalValueGo = 10.000f, rangeGo = 0.010f, wearGo = 0.005f,
                 nominalValueNoGo = 10.020f, rangeNoGo = 0.010f, wearNoGo = 0.000f
             )
             
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Parameter", weight = 1f, isHeader = true)
                 TableCell(text = "Go", weight = 1f, isHeader = true)
                 TableCell(text = "No Go", weight = 1f, isHeader = true)
             }
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Nominal", weight = 1f, isHeader = true)
                 TableCell(text = "${params.nominalValueGo}", weight = 1f)
                 TableCell(text = "${params.nominalValueNoGo}", weight = 1f)
             }
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Range", weight = 1f, isHeader = true)
                 TableCell(text = "${params.rangeGo}", weight = 1f)
                 TableCell(text = "${params.rangeNoGo}", weight = 1f)
             }
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Wear", weight = 1f, isHeader = true)
                 TableCell(text = "${params.wearGo}", weight = 1f)
                 TableCell(text = "${params.wearNoGo}", weight = 1f)
             }
         } else {
             // Thread Plug Gauge Mock
             val params = ParametersThreadPlugGauge(
                 majorDiaNominalValueGo = 12.000f, majorDiaRangeGo = 0.02f,
                 effectiveDiaNominalValueGo = 11.500f, effectiveDiaRangeGo = 0.02f, wearGo = 0.005f,
                 majorDiaNominalValueNoGo = 11.800f, majorDiaRangeNoGo = 0.02f,
                 effectiveDiaNominalValueNoGo = 11.600f, effectiveDiaRangeNoGo = 0.02f, wearNoGo = 0.0f
             )
             
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Parameter", weight = 1f, isHeader = true)
                 TableCell(text = "Go Major", weight = 1f, isHeader = true)
                 TableCell(text = "Go Eff.", weight = 1f, isHeader = true)
             }
             Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Nominal", weight = 1f, isHeader = true)
                 TableCell(text = "${params.majorDiaNominalValueGo}", weight = 1f)
                 TableCell(text = "${params.effectiveDiaNominalValueGo}", weight = 1f)
             }
              Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Range", weight = 1f, isHeader = true)
                 TableCell(text = "${params.majorDiaRangeGo}", weight = 1f)
                 TableCell(text = "${params.effectiveDiaRangeGo}", weight = 1f)
             }
              Row(modifier = Modifier.border(0.5.dp, Color.Gray)) {
                 TableCell(text = "Wear", weight = 1f, isHeader = true)
                 TableCell(text = "${params.wearGo}", weight = 1f)
                 TableCell(text = "-", weight = 1f)
             }
         }
    }
}

@Composable
fun SaveConfirmationDialog(
    srfId: Int,
    instrumentId: Int,
    envHistory: List<EnvironmentalData>,
    capturedData: Map<String, CellData>,
    gaugeType: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean
) {
    val cyan = Color(0xFF00B4D8)
    val cardBackground = Color(0xFF2B3A42)
    val sortedValues = getSortedCapturedValues(capturedData, gaugeType)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Confirm Job Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Job Details
                Text("SRF ID: $srfId", color = Color.White)
                Text("Instrument ID: $instrumentId", color = Color.White)
                Text("Gauge Type: $gaugeType", color = Color.White)
                val params = if (gaugeType == "Plain Plug Gauge") "Plain Plug Parameters" else "Thread Plug Parameters"
                Text("Specs: $params", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                // Environmental Data
                Text("Environmental Data (Last ${envHistory.size} entries)", color = cyan, fontWeight = FontWeight.Bold)
                if (envHistory.isEmpty()) {
                    Text("No environmental data captured.", color = Color.Gray)
                } else {
                    envHistory.takeLast(3).forEachIndexed { index, env ->
                        Text("#${index + 1}: ${env.temperature}°C / ${env.humidity}%", color = Color.White)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                // Captured Values
                Text("Captured Values (${sortedValues.size})", color = cyan, fontWeight = FontWeight.Bold)
                if (sortedValues.isEmpty()) {
                    Text("No readings captured.", color = Color.Gray)
                } else {
                    val keyNames = getSortedKeys(gaugeType)
                    sortedValues.forEachIndexed { index, value ->
                         val label = if (index < keyNames.size) keyNames[index] else "Reading #${index+1}"
                        Text("$label: $value", color = Color.White)
                    }
                }
                
                // Warning if incomplete
                val expectedCount = if (gaugeType == "Plain Plug Gauge") 8 else 16
                if (sortedValues.size < expectedCount) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Warning: Incomplete readings! Expected $expectedCount, got ${sortedValues.size}.", 
                        color = Color.Yellow, 
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isSaving) {
                    CircularProgressIndicator(color = cyan, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Saving...", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = cyan)
                        ) {
                            Text("Confirm & Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun getSortedKeys(gaugeType: String): List<String> {
    val keys = mutableListOf<String>()
    if (gaugeType == "Plain Plug Gauge") {
        val sides = listOf("Go", "No Go")
        val planes = listOf("A-A", "B-B")
        val positions = listOf("Top", "Bottom")
        
        sides.forEach { side ->
            planes.forEach { plane ->
                positions.forEach { pos ->
                    keys.add("$side-$plane-$pos")
                }
            }
        }
    } else {
        val sides = listOf("Go", "No Go")
        val planes = listOf("A-A", "B-B")
        val positions = listOf("Major_1", "Major_2", "Eff._1", "Eff._2")
        
        sides.forEach { side ->
            planes.forEach { plane ->
                positions.forEach { pos ->
                    keys.add("$side-$plane-$pos")
                }
            }
        }
    }
    return keys
}

fun getSortedCapturedValues(data: Map<String, CellData>, gaugeType: String): List<String> {
    val keys = getSortedKeys(gaugeType)
    // Return values in order. If key missing, ignore or put placeholder?
    // We filter map to only include keys present in our expected list?
    // Or just iterate keys and get value.
    val result = mutableListOf<String>()
    keys.forEach { key ->
        if (data.containsKey(key)) {
            result.add(data[key]?.text ?: "")
        }
    }
    return result
}

fun getSortedCapturedValuesRaw(data: Map<String, CellData>, gaugeType: String): List<Double> {
    val keys = getSortedKeys(gaugeType)
    val result = mutableListOf<Double>()
    keys.forEach { key ->
        if (data.containsKey(key)) {
            val raw = data[key]?.rawValue
            if (raw != null) {
                result.add(raw)
            } else {
                 // Try parsing text if raw is missing (fallback)
                 val text = data[key]?.text ?: ""
                 // Text might be "10.005 (+0.005)"
                 val valuePart = text.substringBefore(" (").toDoubleOrNull() ?: 0.0
                 result.add(valuePart)
            }
        } else {
             // If key is missing, add 0.0 or skip? 
             // Requirement: "capturedValues should have all the values in the dynamic table"
             // If we miss values, the lists won't align. Let's add 0.0 for missing values to keep index alignment if needed,
             // or just add what we have. 
             // Given the context of "8 values" or "16 values", we should probably stick to the keys order.
             result.add(0.0)
        }
    }
    return result
}
