package com.technikC.teckcdatacapture.ui.capture

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.technikC.teckcdatacapture.viewmodel.EnvironmentalViewModel
import com.technikC.teckcdatacapture.data.EnvironmentalData
import com.technikC.teckcdatacapture.data.MockData
import java.text.DecimalFormat

@Composable
fun EnvironmentalCaptureScreen(
    srfId: Int,
    instrumentId: Int,
    onBackClick: () -> Unit = {},
    onSubmit: (Double, Double) -> Unit = { _, _ -> },
    onAutoSubmit: (Double, Double) -> Unit = { _, _ -> },
    viewModel: EnvironmentalViewModel = viewModel()
) {
    val darkBlue = Color(0xFF0D1B2A)
    val teal = Color(0xFF1B263B)
    val cardBackground = Color(0xFF2B3A42)
    val cyan = Color(0xFF00B4D8)
    
    val srf = MockData.srfList.find { it.id == srfId }
    val instrument = MockData.instrumentList.find { it.id == instrumentId }
    
    val temperature by viewModel.temperature.collectAsState()
    val humidity by viewModel.humidity.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth() // Changed from fillMaxSize
            //.wrapContentHeight() // Allow it to wrap content
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(darkBlue, teal)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        text = "Environmental Stat",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                     if (srf != null && instrument != null) {
                        Text(
                            text = "${srf.srfNumber} • ${instrument.number}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Temperature Card
            ControlCard(
                title = "Temperature",
                range = "Range: 18 - 30°C",
                value = temperature,
                unit = "°C",
                onValueChange = { viewModel.updateTemperature(it) },
                cardColor = cardBackground,
                cyan = cyan
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Humidity Card
            ControlCard(
                title = "Humidity",
                range = "Range: 30 - 60%",
                value = humidity,
                unit = "%",
                onValueChange = { viewModel.updateHumidity(it) },
                cardColor = cardBackground,
                cyan = cyan
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
//            // Sensor Check PlaceHolder
//             Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(containerColor = cardBackground.copy(alpha = 0.5f)), // slightly transparent
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Row(
//                    modifier = Modifier.padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                     Icon(
//                        imageVector = Icons.Default.Sensors,
//                        contentDescription = null,
//                        tint = cyan,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Column {
//                         Text(
//                            text = "Sensor Check",
//                            style = MaterialTheme.typography.titleSmall,
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = "Sensors calibrated on 10/24/2023",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }

            //Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = { 
                    viewModel.saveValues(srfId, instrumentId)
                    onSubmit(temperature, humidity) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cyan),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            // Submit to Auto Button
            Button(
                onClick = {
                    viewModel.saveValues(srfId, instrumentId)
                    onAutoSubmit(temperature, humidity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cyan),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save to Auto",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ControlCard(
    title: String,
    range: String,
    value: Double,
    unit: String,
    onValueChange: (Double) -> Unit,
    cardColor: Color,
    cyan: Color
) {
     val df = DecimalFormat("#0.0")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cyan,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                     Text(
                        text = range,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Decrease Button
                IconButton(
                    onClick = { onValueChange((value - 0.1)) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1E2830), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = cyan
                    )
                }
                
                // Value Display
                 Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${df.format(value)}$unit",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Increase Button
                IconButton(
                    onClick = { onValueChange((value + 0.1)) },
                    modifier = Modifier
                         .size(48.dp)
                        .background(Color(0xFF1E2830), RoundedCornerShape(12.dp))
                ) {
                     Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = cyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
//            Text(
//                text = "Last entry: ${df.format(value - 0.7)}$unit (10m ago)",
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
        }
    }
}
