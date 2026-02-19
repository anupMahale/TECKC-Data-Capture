package com.technikC.teckcdatacapture.ui.srf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.technikC.teckcdatacapture.data.MockData
import com.technikC.teckcdatacapture.data.Srf
import com.technikC.teckcdatacapture.data.SrfStatus

@Composable
fun SrfListScreen(
    instrumentIdFilter: Int? = null,
    onBackClick: () -> Unit = {},
    onInstrumentClick: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    val darkBlue = Color(0xFF0D1B2A)
    val teal = Color(0xFF1B263B)
    val cardBackground = Color(0xFF2B3A42)
    val cyan = Color(0xFF00B4D8)
    val yellow = Color(0xFFFFC107)
    val green = Color(0xFF4CAF50)

    var selectedFilter by remember { mutableStateOf<SrfStatus?>(SrfStatus.OPEN) }
    
    val allSrfs = MockData.srfList
    
    // Filter by Instrument if filter provided
    val srfsForInstrument = if (instrumentIdFilter != null) {
        val instrument = MockData.instrumentList.find { it.id == instrumentIdFilter }
        allSrfs.filter { instrument?.associatedSrfIds?.contains(it.id) == true }
    } else {
        allSrfs
    }

    val openSrfs = srfsForInstrument.filter { it.status == SrfStatus.OPEN }
    val closedSrfs = srfsForInstrument.filter { it.status == SrfStatus.CLOSED }
    val urgentOpenCount = openSrfs.count { it.isUrgent }

    val filteredList = when (selectedFilter) {
        SrfStatus.OPEN -> openSrfs
        SrfStatus.CLOSED -> closedSrfs
        null -> srfsForInstrument
    }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (instrumentIdFilter != null) "SRFs for Instrument" else "SRF List",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (instrumentIdFilter == null) {
                    androidx.compose.material3.Button(
                        onClick = onInstrumentClick,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cardBackground)
                    ) {
                        Text("Instruments", color = cyan)
                    }
                }
            }

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterCard(
                    title = "Open",
                    count = openSrfs.size,
                    isSelected = selectedFilter == SrfStatus.OPEN,
                    onClick = { selectedFilter = SrfStatus.OPEN },
                    modifier = Modifier.weight(1f),
                    urgentCount = urgentOpenCount,
                    accentColor = cyan
                )
                FilterCard(
                    title = "Closed",
                    count = closedSrfs.size,
                    isSelected = selectedFilter == SrfStatus.CLOSED,
                    onClick = { selectedFilter = SrfStatus.CLOSED },
                    modifier = Modifier.weight(1f),
                    accentColor = green
                )
                FilterCard(
                    title = "All",
                    count = srfsForInstrument.size,
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    modifier = Modifier.weight(1f),
                    accentColor = Color.White
                )
            }

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { srf ->
                    SrfCard(
                        srf = srf,
                        cardColor = cardBackground,
                        cyan = cyan,
                        yellow = yellow,
                        onClick = { onCardClick(srf.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterCard(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    urgentCount: Int = 0,
    accentColor: Color
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, accentColor) else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                if (urgentCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Urgent",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = urgentCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SrfCard(
    srf: Srf,
    cardColor: Color,
    cyan: Color,
    yellow: Color,
    onClick: () -> Unit
) {
    val associatedInstruments = MockData.instrumentList.filter { it.associatedSrfIds.contains(srf.id) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = srf.srfNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (srf.isUrgent && srf.status == SrfStatus.OPEN) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgent",
                            tint = yellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = srf.clientName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
                
                if (associatedInstruments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instruments: ${associatedInstruments.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cyan
                    )
                    Text(
                        text = associatedInstruments.joinToString(", ") { it.number },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Due: ${srf.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (srf.status == SrfStatus.OPEN) cyan.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = srf.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (srf.status == SrfStatus.OPEN) cyan else Color.Gray
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SrfListScreenPreview() {
    SrfListScreen()
}
