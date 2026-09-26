package com.example.model

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT,
    val colorHex: String? = null
)

enum class LineType {
    PROMPT,
    INPUT,
    OUTPUT,
    ERROR,
    SYSTEM,
    ASCII_ART
}

data class SystemStats(
    val cpuUsagePercent: Int,
    val ramUsedPercent: Int,
    val ramTotalMb: Long,
    val ramUsedMb: Long,
    val ramFreeMb: Long,
    val storageUsedPercent: Int,
    val storageTotalGb: Double,
    val storageUsedGb: Double,
    val osVersion: String = "N/D",
    val architecture: String = "N/D",
    val rootfsSizeMb: Double = 0.0,
    val processCount: Int = 1,
    val pid: Int = 0,
    val kernelVersion: String = "N/D",
    val uptimeMinutes: Long = 0,
    val packagesCount: Int = 0
)

data class PackageItem(
    val name: String,
    val version: String,
    val description: String,
    val isInstalled: Boolean = true,
    val size: String = "N/D"
)
