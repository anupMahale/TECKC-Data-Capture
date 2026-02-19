package com.technikC.teckcdatacapture.ui.srf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.technikC.teckcdatacapture.data.MockData
import com.technikC.teckcdatacapture.data.SrfStatus

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SrfDetailsScreen(
    srfId: Int,
    instrumentId: Int,
    onBackClick: () -> Unit = {},
    onStartWorkflow: (Int, Int) -> Unit = { _, _ -> },
    showStartButton: Boolean = true
) {
    val darkBlue = Color(0xFF0D1B2A)
    val teal = Color(0xFF1B263B)
    val cardBackground = Color(0xFF2B3A42)
    val cyan = Color(0xFF00B4D8)
    val yellow = Color(0xFFFFC107)

    val srf = MockData.srfList.find { it.id == srfId }
    val instrument = MockData.instrumentList.find { it.id == instrumentId }
    val specimen = srf?.let { MockData.specimenList.find { sp -> sp.id == it.specimenId } }

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
                Text(
                    text = "SRF Details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (srf != null) {
                // SRF Details Card
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = cyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SRF Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = cyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("SRF Number", srf.srfNumber)
                        DetailRow("Client Name", srf.clientName)
                        DetailRow("Delivery Chalan", srf.deliveryChalan)
                        if (specimen != null) {
                            DetailRow("Specimen Spec", "${specimen.make}, ${specimen.size}")
                            DetailRow("Specimen S.No", specimen.serialNumber)
                        }
                        DetailRow("Due Date", srf.dueDate)
                        DetailRow("Status", srf.status.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (instrument != null) {
                // Instrument Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = cyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instrument Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = cyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Name", instrument.name)
                        DetailRow("Number", instrument.number)
                    }
                }
            }

            //Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Start Workflow Button
            if (showStartButton) {
                Button(
                    onClick = { onStartWorkflow(srfId, instrumentId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cyan),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Workflow",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
