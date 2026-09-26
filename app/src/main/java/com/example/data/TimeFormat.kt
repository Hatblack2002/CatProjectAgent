package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun formatTimeOfDay(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

internal fun formatRelativeActivity(millis: Long): String {
    if (millis <= 0L) return "Sin actividad"
    val dayOffset = dayOffset(System.currentTimeMillis(), millis)
    val time = formatTimeOfDay(millis)
    return when {
        dayOffset <= 0 -> "Hoy, $time"
        dayOffset == 1 -> "Ayer, $time"
        dayOffset < 7 -> "Hace $dayOffset días"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}

internal fun formatChatTimestamp(millis: Long): String =
    if (dayOffset(System.currentTimeMillis(), millis) <= 0) formatTimeOfDay(millis)
    else formatRelativeActivity(millis)

private fun startOfDay(millis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

private fun dayOffset(referenceMillis: Long, targetMillis: Long): Int =
    Math.round((startOfDay(referenceMillis) - startOfDay(targetMillis)) / 86_400_000.0).toInt()
