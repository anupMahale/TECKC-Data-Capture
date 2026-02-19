package com.technikC.teckcdatacapture.ui.instrument

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
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
import com.technikC.teckcdatacapture.data.Instrument
import com.technikC.teckcdatacapture.data.MockData
import com.technikC.teckcdatacapture.data.SrfStatus
import com.technikC.teckcdatacapture.ui.srf.FilterCard

@Composable
fun InstrumentListScreen(
    srfIdFilter: Int? = null,
    onBackClick: () -> Unit = {},
    onSrfClick: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    val darkBlue = Color(0xFF0D1B2A)
    val teal = Color(0xFF1B263B)
    val cardBackground = Color(0xFF2B3A42)
    val cyan = Color(0xFF00B4D8)
    val yellow = Color(0xFFFFC107)
    val green = Color(0xFF4CAF50)

    var selectedFilter by remember { mutableStateOf<SrfStatus?>(SrfStatus.OPEN) }

    // Logic to determine Instrument Status
    val instruments = MockData.instrumentList
    val srfMap = MockData.srfList.associateBy { it.id }

    // Filter by SRF if filter provided
    val instrumentsForList = if (srfIdFilter != null) {
        instruments.filter { it.associatedSrfIds.contains(srfIdFilter) }
    } else {
        instruments
    }

    fun getInstrumentStatus(instrument: Instrument): SrfStatus {
        val associatedSrfs = instrument.associatedSrfIds.mapNotNull { srfMap[it] }
        val allClosed = associatedSrfs.all { it.status == SrfStatus.CLOSED }
        return if (allClosed && associatedSrfs.isNotEmpty()) SrfStatus.CLOSED else SrfStatus.OPEN
    }

    fun isInstrumentUrgent(instrument: Instrument): Boolean {
        val associatedSrfs = instrument.associatedSrfIds.mapNotNull { srfMap[it] }
        // Urgent if any OPEN SRF is urgent
        return associatedSrfs.any { it.status == SrfStatus.OPEN && it.isUrgent }
    }

    val openInstruments = instrumentsForList.filter { getInstrumentStatus(it) == SrfStatus.OPEN }
    val closedInstruments = instrumentsForList.filter { getInstrumentStatus(it) == SrfStatus.CLOSED }
    val urgentOpenCount = openInstruments.count { isInstrumentUrgent(it) }

    val filteredList = when (selectedFilter) {
        SrfStatus.OPEN -> openInstruments
        SrfStatus.CLOSED -> closedInstruments
        null -> instrumentsForList
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
                        text = if (srfIdFilter != null) "Instruments for SRF" else "Instrument List",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (srfIdFilter == null) {
                    androidx.compose.material3.Button(
                        onClick = onSrfClick,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cardBackground)
                    ) {
                        Text("SRFs", color = cyan)
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
                    count = openInstruments.size,
                    isSelected = selectedFilter == SrfStatus.OPEN,
                    onClick = { selectedFilter = SrfStatus.OPEN },
                    modifier = Modifier.weight(1f),
                    urgentCount = urgentOpenCount,
                    accentColor = cyan
                )
                FilterCard(
                    title = "Closed",
                    count = closedInstruments.size,
                    isSelected = selectedFilter == SrfStatus.CLOSED,
                    onClick = { selectedFilter = SrfStatus.CLOSED },
                    modifier = Modifier.weight(1f),
                    accentColor = green
                )
                FilterCard(
                    title = "All",
                    count = instrumentsForList.size,
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
                items(filteredList) { instrument ->
                    val status = getInstrumentStatus(instrument)
                    val isUrgent = isInstrumentUrgent(instrument)
                    
                    InstrumentCard(
                        instrument = instrument,
                        status = status,
                        isUrgent = isUrgent,
                        cardColor = cardBackground,
                        cyan = cyan,
                        yellow = yellow,
                        onClick = { onCardClick(instrument.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun InstrumentCard(
    instrument: Instrument,
    status: SrfStatus,
    isUrgent: Boolean,
    cardColor: Color,
    cyan: Color,
    yellow: Color,
    onClick: () -> Unit
) {
    val srfMap = MockData.srfList.associateBy { it.id }
    val associatedSrfs = instrument.associatedSrfIds.mapNotNull { srfMap[it] }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = instrument.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgent",
                            tint = yellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = instrument.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                if (associatedSrfs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SRFs: ${associatedSrfs.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cyan
                    )
                    Text(
                        text = associatedSrfs.joinToString(", ") { it.srfNumber },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (status == SrfStatus.OPEN) cyan.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (status == SrfStatus.OPEN) cyan else Color.Gray
                )
            }
        }
    }
}

@Preview
@Composable
fun InstrumentListScreenPreview() {
    InstrumentListScreen()
}
