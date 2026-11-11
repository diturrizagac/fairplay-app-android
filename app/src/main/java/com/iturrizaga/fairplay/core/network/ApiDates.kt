package com.iturrizaga.fairplay.core.network

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * ISO-8601 helpers that work on API 24 (no java.time). The API sends
 * datetimes with optional fractional seconds and either "Z" or a "+hh:mm"
 * offset, and dates as "yyyy-MM-dd".
 */
object ApiDates {
    private val dateTimePattern =
        Regex("""^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(\.\d+)?(Z|[+-]\d{2}:\d{2})?$""")

    fun parseDateTime(raw: String): Long? {
        val match = dateTimePattern.matchEntire(raw.trim()) ?: return null
        val (base, fraction, zone) = match.destructured
        val utcMillis = utcFormat("yyyy-MM-dd'T'HH:mm:ss").parse(base)?.time ?: return null
        val millis = if (fraction.isEmpty()) 0L else fraction.drop(1).take(3).padEnd(3, '0').toLong()
        return utcMillis + millis - offsetMillis(zone)
    }

    fun formatDateTime(epochMillis: Long): String =
        utcFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(epochMillis)

    fun formatDate(epochMillis: Long): String = utcFormat("yyyy-MM-dd").format(epochMillis)

    private fun offsetMillis(zone: String): Long {
        if (zone.isEmpty() || zone == "Z") return 0L
        val sign = if (zone.startsWith("-")) -1 else 1
        val hours = zone.substring(1, 3).toLong()
        val minutes = zone.substring(4, 6).toLong()
        return sign * (hours * 60 + minutes) * 60_000
    }

    private fun utcFormat(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}
