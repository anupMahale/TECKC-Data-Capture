package com.technikC.teckcdatacapture.data

enum class SrfStatus {
    OPEN, CLOSED
}

data class Srf(
    val id: Int,
    val srfNumber: String,
    val clientName: String,
    val dueDate: String,
    val status: SrfStatus,
    val isUrgent: Boolean = false,
    val deliveryChalan: String = "DC-2024-001",
    val specimenId: Int
)

object MockData {
    val specimenList = listOf(
        Specimen(1, "Mitutoyo", "SN-998877", "0-150mm"),
        Specimen(2, "Fluke", "SN-554433", "N/A"),
        Specimen(3, "Starrett", "SN-112233", "0-25mm"),
        Specimen(4, "Testo", "SN-667788", "N/A"),
        Specimen(5, "Bosch", "SN-445566", "0-100m"),
        Specimen(6, "Leica", "SN-332211", "0-30m")
    )

    val srfList = listOf(
        Srf(1, "SRF-2024-001", "Acme Corp", "2024-02-20", SrfStatus.OPEN, true, "DC-001", 1),
        Srf(2, "SRF-2024-002", "Globex Inc", "2024-02-22", SrfStatus.OPEN, false, "DC-002", 2),
        Srf(3, "SRF-2024-003", "Soylent Corp", "2024-02-18", SrfStatus.CLOSED, false, "DC-003", 3),
        Srf(4, "SRF-2024-004", "Initech", "2024-02-25", SrfStatus.OPEN, true, "DC-004", 4),
        Srf(5, "SRF-2024-005", "Umbrella Corp", "2024-02-15", SrfStatus.CLOSED, true, "DC-005", 5),
        Srf(6, "SRF-2024-006", "Stark Ind", "2024-02-28", SrfStatus.OPEN, false, "DC-006", 6),
        Srf(7, "SRF-2024-007", "Wayne Ent", "2024-03-01", SrfStatus.OPEN, false, "DC-007", 1),
        Srf(8, "SRF-2024-008", "Cyberdyne", "2024-01-30", SrfStatus.CLOSED, false, "DC-008", 2)
    )

    val instrumentList = listOf(
        Instrument(1, "Pressure Gauge", "INS-001", listOf(1, 2)), 
        Instrument(2, "Thermometer", "INS-002", listOf(3)), 
        Instrument(3, "Multimeter", "INS-003", listOf(4, 5)), 
        Instrument(4, "Flow Meter", "INS-004", listOf(6)), 
        Instrument(5, "Caliper", "INS-005", listOf(7)), 
        Instrument(6, "Micrometer", "INS-006", listOf(8)), 
        Instrument(7, "Humidity Sensor", "INS-007", listOf(1, 5)), 
        Instrument(8, "Barometer", "INS-008", listOf(3, 8)) 
    )
}
