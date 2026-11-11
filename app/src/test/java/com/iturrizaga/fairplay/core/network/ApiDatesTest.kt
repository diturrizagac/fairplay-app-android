package com.iturrizaga.fairplay.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiDatesTest {
    @Test fun parsesUtcWithFractionalSeconds() {
        assertEquals(1_794_580_832_890L, ApiDates.parseDateTime("2026-11-13T14:40:32.890150Z"))
    }

    @Test fun parsesExplicitOffset() {
        val utc = ApiDates.parseDateTime("2026-11-13T14:40:32Z")
        assertEquals(utc, ApiDates.parseDateTime("2026-11-13T09:40:32-05:00"))
        assertEquals(utc, ApiDates.parseDateTime("2026-11-13T14:40:32+00:00"))
    }

    @Test fun rejectsGarbage() {
        assertNull(ApiDates.parseDateTime("tomorrow"))
    }

    @Test fun formatRoundTripsInUtc() {
        val millis = 1_794_580_832_000L
        assertEquals("2026-11-13T14:40:32Z", ApiDates.formatDateTime(millis))
        assertEquals(millis, ApiDates.parseDateTime(ApiDates.formatDateTime(millis)))
        assertEquals("2026-11-13", ApiDates.formatDate(millis))
    }
}
